package de.evoal.challenges.ic.genotype;

import de.evoal.core.api.cdi.BeanFactory;
import de.evoal.core.api.utils.LanguageHelper;
import de.evoal.core.ea.api.operators.AltererComponentProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

@ApplicationScoped
public class OperatorFactory {
    @Inject
    private LanguageHelper helper;

    @Produces @Dependent
    @Named("de.evoal.challenges.ic.optimisation.policy-rotation-mutator")
    public AltererComponentProvider createPolicyRotationMutator() {
        return instance -> {
            double probability = helper.lookup(instance, "probability");
            final PolicyRotationMutator<?> mutator = new PolicyRotationMutator<>(probability);
            BeanFactory.injectFields(mutator);
            mutator.init(instance);

            return mutator;

        };
    }

    @Produces @Dependent
    @Named("de.evoal.challenges.ic.optimisation.plus-one-mutator")
    public AltererComponentProvider createPlusOneMutator() {
        return instance -> {
            double probability = helper.lookup(instance, "probability");
            final PlusOneMutator<?> mutator = new PlusOneMutator<>(probability);
            BeanFactory.injectFields(mutator);
            mutator.init(instance);

            return mutator;
        };
    }
}
