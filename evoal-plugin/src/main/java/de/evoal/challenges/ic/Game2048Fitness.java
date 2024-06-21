package de.evoal.challenges.ic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.evoal.core.api.optimisation.OptimisationFunction;
import de.evoal.core.api.properties.Properties;
import de.evoal.core.api.properties.PropertiesSpecification;
import de.evoal.core.api.properties.PropertySpecification;
import de.evoal.core.api.utils.InitializationException;
import de.evoal.core.api.utils.LanguageHelper;
import de.evoal.languages.model.base.Instance;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Fitness function for the game 2048.
 */
@Dependent
@Named("de.evoal.challenges.ic.optimisation.2048-fitness")
@Slf4j
public class Game2048Fitness implements OptimisationFunction {
    /**
     * Helper for looking up configuration values from .ol file
     */
    @Inject
    private LanguageHelper helper;

    /**
     * Number of games to run for each individual for calculating an average.
     */
    private int gameCount;

    /**
     * Number of parallel processes
     */
    private final static Semaphore semaphore = new Semaphore(1);

    /**
     * Output specification
     */
    @Inject @Named("optimisation-space-specification")
    private PropertiesSpecification specification;

    /**
     * Counter for enumerating the programs
     */
    private final static AtomicInteger counter = new AtomicInteger(1);

    @Override
    public double[] evaluate(final Properties properties) {
        final List<String> propertyNames =
                specification.getProperties()
                        .stream()
                        .map(PropertySpecification::name)
                        .filter(p -> p.contains("(") && p.endsWith(")"))
                        .map(p -> p.substring(p.indexOf("(") + 1, p.length() - 1))
                        .collect(Collectors.toUnmodifiableList());
        try {
            final int pid = counter.getAndIncrement();

            // 1. create a temporary directory for the generated code
            final Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"));
            final Path currentTempDir = Files.createTempDirectory(tempPath, "ic-program-" + pid + "-");

            // 2. generate Python code
            generate(currentTempDir, properties);

            // 3. Prepare result lists
            List<List<Double>> fitnessValues = new ArrayList<>();
            for(int i = 0; i < propertyNames.size(); ++i) {
                fitnessValues.add(new ArrayList<>());
            }

            // 4. Starting game
            log.info("Starting game for pid {}.", pid);
            start(pid, currentTempDir);

            for(int rep = 0; rep < gameCount; ++rep) {
                final Optional<List<Map<String, Object>>> gameProtocol = parse(currentTempDir, rep);

                if(gameProtocol.isPresent() && !gameProtocol.get().isEmpty()) {
                    final Map<String, Object> entry = gameProtocol.get().get(gameProtocol.get().size() - 1);

                    for (int p = 0; p < propertyNames.size(); ++p) {
                        final String propertyName = propertyNames.get(p);
                        final Object value = entry.get(propertyName);

                        fitnessValues.get(p).add(((Number) value).doubleValue());
                    }
                } else {
                    for (int p = 0; p < propertyNames.size(); ++p) {
                        fitnessValues.get(p).add(0.0);
                    }
                }
            }

            double [] result = new double[specification.size()];
            for(int pIndex = 0; pIndex < specification.size(); ++pIndex) {
                final PropertySpecification ps = specification.getProperties().get(pIndex);

                if("program-id".equals(ps.name())) {
                    result[pIndex] = pid;
                    continue;
                } else if("policy-size".equals(ps.name())) {
                    final Instance policy = (Instance)properties.get(0);
                    final TreeIterator<EObject> iterator = policy.eAllContents();
                    int size = 0;
                    while(iterator.hasNext()) {
                        iterator.next();
                         size += 1;
                    }
                    result[pIndex] = size;
                    continue;
                }

                int dataIndex = 0;
                for(; dataIndex < propertyNames.size(); ++dataIndex) {
                    if(ps.name().contains(propertyNames.get(dataIndex))) {
                        break;
                    }
                }

                if(dataIndex == propertyNames.size()) {
                    log.error("Could not find: {}", ps.name());
                }

                final List<Double> data = fitnessValues.get(dataIndex);
                final double average = data.stream().mapToDouble(Double.class::cast).average().getAsDouble();
                if(ps.name().startsWith("average(")) {
                    result[pIndex] = average;
                } else if(ps.name().startsWith("sd(")) {
                    result[pIndex] = -Math.sqrt(data.stream().mapToDouble(d -> Math.pow(d - average, 2.0)).sum() / (data.size() - 1));
                } else if(ps.name().startsWith("max(")) {
                    result[pIndex] = data.stream().mapToDouble(Double.class::cast).max().getAsDouble();
                } else if(ps.name().startsWith("min(")) {
                    result[pIndex] = data.stream().mapToDouble(Double.class::cast).min().getAsDouble();
                }
            }

            return result;
        } catch(final IOException e) {
            log.error("Failed to execute simulation.", e);
            throw new RuntimeException("Failed to execute simulation.", e);
        }
    }

    @SneakyThrows
    private Optional<List<Map<String, Object>>> parse(final Path currentTempDir, final int gameIndex) {
        final ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<Map<String,Object>>> typeRef
                = new TypeReference<List<Map<String,Object>>>() {};

        final File protocolFile = new File(currentTempDir.toFile(), "protocol-" + gameIndex + ".json");

        if(!protocolFile.exists()) {
            return Optional.empty();
        }

        try {
            return Optional.of(mapper.readValue(protocolFile, typeRef));
        } catch(final Exception e) {
            log.error("Failed to read protocol {}.", protocolFile, e);
            return Optional.empty();
        } finally {
            // protocolFile.delete();
            // new File(protocolFile.getParent(), "stdout.log").delete();
            deleteDir(new File(protocolFile.getParent(), "generated/__pycache__"));
        }
    }

    private void start(int pid, final Path directory) {
        try {
            final File simulationPath = new File(System.getProperty("user.dir") + "/simulation");
            final File policyPath = directory.toFile();

            log.info("Preparing simulation {} ...", pid);
            final ProcessBuilder builder = new ProcessBuilder();

            // set execution path
            builder.directory(policyPath);

            // set environment
            builder.environment().put("PYTHONPATH", simulationPath + File.pathSeparator + policyPath);


            // redirect output
  //          builder.inheritIO();
            builder.redirectOutput(ProcessBuilder.Redirect.to(new File(policyPath,"stdout.log")));
            builder.redirectError(ProcessBuilder.Redirect.to(new File(policyPath, "stderr.log")));

            // create parameter list
            final List<String> parameters = new LinkedList<>();
            parameters.add("python3");
            parameters.add(new File(simulationPath, "evoal/controller.py").getAbsolutePath());
            parameters.add(Integer.toString(gameCount));

            // find shell to use
            String shell = System.getenv("SHELL");
            if(shell == null) {
                shell = "bash";
                log.warn("Could not find shell in environment. Falling back to bash.");
            }

            builder.command(shell, "-c", String.join(" ", parameters));

            log.info("Starting simulation {} ...", pid);
            log.debug("Trying to acquire access in thread: {}", Thread.currentThread().getName());
            semaphore.acquireUninterruptibly();

            log.debug("Received access in thread: {}", Thread.currentThread().getName());

            final Process p = builder.start();

            do {
                //log.info("Waiting for simulation {} to finish...", pid);
                p.waitFor(200, TimeUnit.MILLISECONDS);
            } while (p.isAlive());

            log.debug("Collecting simulation {} ...", pid);
            p.destroy();
            log.debug("Collected simulation {} ...", pid);
            log.info("Releasing access in thread: {}", Thread.currentThread().getName());
            semaphore.release();
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    @SneakyThrows(IOException.class)
    private void generate(final Path directory, final Properties properties) {
        final Map<String, Instance> model = new HashMap<>();
        for(PropertySpecification spec : properties.getSpecification().getProperties()) {
            model.put(spec.name(), (Instance)properties.get(spec));

            // write policy
            /* TODO Write policy file

             */
        }

        final Configuration cfg = new Configuration(new Version(2, 3, 20));

        // Where FileTemplateLoader we load the templates from:
        final TemplateLoader loader = new ClassTemplateLoader(getClass().getClassLoader(), "/de/evoal/challenges/ic");

        //cfg.setClassForTemplateLoading(Template.class, "templates");
        cfg.setTemplateLoader(loader);
        // Some other recommended settings:
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.GERMAN);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);


        // 2. Proccess template(s)
        // 2.1. Prepare the template input:
        final Map<String, Object> input = new HashMap<>();
        input.put("model", model);
        input.put("access", new ModelAccess());

        // 2.2. Get the template
        final freemarker.template.Template template = cfg.getTemplate("python-policy.ftl");

        // 2.3. Generate the output

        // 2.3.1 Generate directory
        final File outputFile = new File(directory.toFile(), "generated/policy.py");
        outputFile.getParentFile().mkdirs();

        // 2.3.2 Touch __init__.py
        try(final OutputStream os = new FileOutputStream(new File(directory.toFile(), "generated/__init__.py"))) {
        }

        try(final Writer fileWriter = new FileWriter(outputFile)) {
            // For the sake of example, also write output into a file:
            template.process(input, fileWriter);
        }
        catch (final TemplateException e) {
            log.error("Failed to process template.", e);
        }
    }

    @Override
    public OptimisationFunction init(final Instance configuration) throws InitializationException {
        gameCount = helper.lookup(configuration, "game-count");

        return this;
    }

    private void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (! Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f);
                }
            }
        }
        file.delete();
    }
}
