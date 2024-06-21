package de.evoal.challenges.ic;

import de.evoal.languages.model.base.*;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;

public class ModelAccess {
    public @NonNull List<Instance> entries(final @NonNull Instance instance) {
        return instanceArray(instance, "entries");
    }

    public @NonNull Instance condition(final @NonNull Instance instance) {
        final OrExpression expression = (OrExpression) instance.findAttribute("condition").getValue();

        return (Instance) expression.getSubExpressions().get(0)
                .getSubExpressions().get(0)
                .getSubExpressions().get(0)
                .getOperand()
                .getLeftOperand()
                .getLeftOperand()
                .getLeftOperand()
                .getLeftOperand()
                .getSubExpression();
    }

    public @NonNull List<Instance> subexpressions(final @NonNull Instance instance) {
        return instanceArray(instance, "subexpressions");
    }

    public @NonNull List<Instance> comparisons(final @NonNull Instance instance) {
        return instanceArray(instance, "comparisons");
    }

    public @NonNull List<Instance> literals(final @NonNull Instance instance) {
        return instanceArray(instance, "literals");
    }

    public @NonNull Integer action(final @NonNull Instance instance) {
        final OrExpression expression = (OrExpression) instance.findAttribute("action").getValue();

        return ((IntegerLiteral)expression.getSubExpressions().get(0)
                .getSubExpressions().get(0)
                .getSubExpressions().get(0)
                .getOperand()
                .getLeftOperand()
                .getLeftOperand()
                .getLeftOperand()
                .getLeftOperand()
                .getSubExpression())
                .getLiteral();
    }

    private @NonNull String lop(final @NonNull Instance instance) {
        final OrExpression lop = (OrExpression) instance.findAttribute("leftOperand").getValue();

        try {
            return literal(lop);
        } catch (final NullPointerException e) {
            return variable(lop);
        }
    }

    public @NonNull String rop(final @NonNull Instance instance) {
        final OrExpression rop = (OrExpression) instance.findAttribute("rightOperand").getValue();

        return variable(rop);
    }

    private @NonNull String literal(final @NonNull OrExpression expression) {
        final Instance literal = (Instance)expression.getSubExpressions().get(0)
                .getSubExpressions().get(0)
                .getSubExpressions().get(0)
                .getOperand()
                .getLeftOperand()
                .getLeftOperand()
                .getLeftOperand()
                .getLeftOperand()
                .getSubExpression();

        return value(literal);
    }

    public @NonNull String value(final @NonNull Instance instance) {
        final OrExpression expression = (OrExpression) instance.findAttribute("value").getValue();

        final Literal value = ((Literal) expression.getSubExpressions().get(0)
                .getSubExpressions().get(0)
                .getSubExpressions().get(0)
                .getOperand()
                .getLeftOperand()
                .getLeftOperand()
                .getLeftOperand()
                .getLeftOperand()
                .getSubExpression());

        if (value instanceof BooleanLiteral bLiteral) {
            return bLiteral.getValue() ? "True" : "False";
        } else if (value instanceof IntegerLiteral iLiteral) {
            return iLiteral.getValue().toString();
        }

        throw new IllegalStateException("Nono");
    }

    public @NonNull String operator(final @NonNull Instance instance) {
        final OrExpression expression = (OrExpression) instance.findAttribute("operator").getValue();

        int value = ((IntegerLiteral)expression.getSubExpressions().get(0)
                .getSubExpressions().get(0)
                .getSubExpressions().get(0)
                .getOperand()
                .getLeftOperand()
                .getLeftOperand()
                .getLeftOperand()
                .getLeftOperand()
                .getSubExpression())
                .getLiteral();

        if(instance.getDefinition().getName().contains("Boolean")) {
            switch (value) {
                case 0: case 2:
                    return "==";
                case 1: case 3:
                    return "!=";
            }
        } else {
            switch (value) {
                case 0: case 2:
                    return "<";
                case 1: case 3:
                    return ">";
            }
        }
        return "unknown";
    }

    public @NonNull String variable(final @NonNull OrExpression expression) {
        final Instance inst = (Instance)expression.getSubExpressions().get(0)
                .getSubExpressions().get(0)
                .getSubExpressions().get(0)
                .getOperand()
                .getLeftOperand()
                .getLeftOperand()
                .getLeftOperand()
                .getLeftOperand()
                .getSubExpression();



        String parameters = inst.getAttributes()
                                .stream()
                                .map(a -> {
                                    return ", " + a.getDefinition().getName() + " = " + baseLiteral(a.getValue());
                                })
                                .collect(Collectors.joining());

        return "game." + inst.getDefinition().getName() + "(observation" + parameters + ")";
    }

    public @NonNull String baseLiteral(final @NonNull Expression expression) {
        final Value value = ((OrExpression) expression).getSubExpressions()
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

        return ((Literal)value).getValue().toString();
    }

    private @NonNull List<Instance> instanceArray(final @NonNull Instance instance, final @NonNull String name) {
        final OrExpression expression = (OrExpression) instance.findAttribute(name).getValue();

        return (List<Instance>)(List)((Array) expression.getSubExpressions().get(0)
                .getSubExpressions().get(0)
                .getSubExpressions().get(0)
                .getOperand()
                .getLeftOperand()
                .getLeftOperand()
                .getLeftOperand()
                .getLeftOperand()
                .getSubExpression()).getValues();
    }

    private boolean toBooleanLiteral(final OrExpression expression) {
        final Instance literalInstance = (Instance) expression.getSubExpressions()
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

        final OrExpression literalExpression = (OrExpression) literalInstance.findAttribute("value").getValue();

        final BooleanLiteral literal = (BooleanLiteral) literalExpression.getSubExpressions()
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

        return literal.isValue();
    }

    public @NonNull String toExpression(final @NonNull Instance instance) {
        final String operator = operator(instance);

        if ("BooleanComparison".equals(instance.getDefinition().getName())) {
            final OrExpression lop = (OrExpression) instance.findAttribute("leftOperand").getValue();
            final Boolean value = toBooleanLiteral(lop);

            if (value && "==".equals(operator) || !value && "!=".equals(operator)) {
                return rop(instance);
            } else {
                return "not " + rop(instance);
            }
        } else {
            return lop(instance) + " " + operator + " " + rop(instance);
        }
    }

    private boolean isNegated(final @NonNull Instance instance) {
        final OrExpression expression = (OrExpression) instance.findAttribute("isNegated").getValue();

        final BooleanLiteral literal = (BooleanLiteral) expression.getSubExpressions()
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

        return literal.isValue();
    }

    private @NonNull String toCall(final @NonNull Instance instance) {
        String parameters = instance.getAttributes()
                .stream()
                .filter(a -> !"isNegated".equals(a.getDefinition().getName()))
                .map(a -> {
                    return ", " + a.getDefinition().getName() + " = " + baseLiteral(a.getValue());
                })
                .collect(Collectors.joining());

        return "game." + instance.getDefinition().getName() + "(observation" + parameters + ")";
    }


    public @NonNull String toLiteral(final @NonNull Instance instance) {
        final boolean isNegated = isNegated(instance);

        return (isNegated ? "not " : "") + toCall(instance);
    }
}