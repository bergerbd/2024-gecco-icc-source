package de.evoal.challenges.ic.genotype;

import de.evoal.core.api.utils.LanguageHelper;
import de.evoal.core.ea.api.alterer.EvoAlMutator;
import de.evoal.core.ea.api.codec.model.ModelChromosome;
import de.evoal.core.ea.api.codec.model.ModelGene;
import de.evoal.core.ea.api.operators.AltererComponent;
import de.evoal.languages.model.base.*;
import io.jenetics.Chromosome;
import io.jenetics.MutatorResult;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import javax.inject.Inject;
import java.util.*;
import java.util.random.RandomGenerator;
import java.util.stream.StreamSupport;

@Slf4j
public class PolicyRotationMutator<A extends Comparable<? super A>> extends EvoAlMutator implements AltererComponent {
    private String [] attributeNames;

    @Inject
    private LanguageHelper helper;

    public PolicyRotationMutator(double probability) {
        super(probability);
    }

    @Override
    public AltererComponent init(final Instance configuration) {
        Object [] data = helper.lookup(configuration, "attributes");
        attributeNames = new String [data.length];
        System.arraycopy(data, 0, attributeNames, 0, data.length);

        Arrays.sort(attributeNames);

        return this;
    }

    protected MutatorResult<Chromosome<ModelGene>> mutate(
            final Chromosome chromosome,
            final double p,
            final RandomGenerator random
    ) {
        final int P = toInt(p);
        int rotation = random.nextInt(1, 4);
        log.info("Rotating policy by {}", rotation);

        final ModelChromosome mc = (ModelChromosome) chromosome;
        final Instance copy = EcoreUtil.copy(mc.getModel());

        final TreeIterator<EObject> iterator = copy.eAllContents();
        final List<Attribute> attributes = StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                        false)
                .filter(Instance.class::isInstance)
                .map(Instance.class::cast)
                .flatMap(i -> i.getAttributes().stream().filter(a -> Arrays.binarySearch(attributeNames, a.getDefinition().getName()) >= 0))
                .toList();

        int numberOfChanges = 0;

        final Set<Integer> indices = new HashSet<>();
        do {
            final int index = random.nextInt(0, attributes.size());
            if(!indices.add(index) ) {
                break;
            }

            final Attribute attribute = attributes.get(index);
            numberOfChanges += 1;
            rotate((OrExpression) attribute.getValue(), rotation);
        } while (random.nextInt() < P);

        log.info("Number of changes are {}.", numberOfChanges);
        return new MutatorResult<>(
                ModelChromosome.of(mc.builder(), copy),
                numberOfChanges
        );
    }

    private void rotate(final OrExpression expression, int rotation) {
        final IntegerLiteral literal = (IntegerLiteral)
                expression.getSubExpressions()
                        .get(0)
                        .getSubExpressions()
                        .get(0)
                        .getSubExpressions()
                        .get(0)
                        .getOperand()
                        .getLeftOperand()
                        .getLeftOperand()
                        .getLeftOperand()
                        .getLeftOperand()
                        .getSubExpression();

        literal.setLiteral((literal.getLiteral() + rotation) % 4);
    }
}
