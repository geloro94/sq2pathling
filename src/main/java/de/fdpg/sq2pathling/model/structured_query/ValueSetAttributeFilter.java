package de.fdpg.sq2pathling.model.structured_query;

import static java.util.Objects.requireNonNull;

import de.fdpg.sq2pathling.model.AttributeMapping;
import de.fdpg.sq2pathling.model.common.TermCode;
import java.util.List;

public record ValueSetAttributeFilter(TermCode attributeCode, List<TermCode> selectedConcepts)
        implements AttributeFilter {

    public ValueSetAttributeFilter {
        requireNonNull(attributeCode);
        selectedConcepts = List.copyOf(selectedConcepts);
    }

    /**
     * Returns a {@code ValueSetCriterion}.
     *
     * @param attributeCode    the code identifying the attribute
     * @param selectedConcepts at least one selected value concept
     * @return the {@code ValueSetCriterion}
     * @throws IllegalArgumentException if {@code selectedConcepts} are empty
     * @throws NullPointerException     if any of the {@code selectedConcepts} is null
     */
    public static ValueSetAttributeFilter of(TermCode attributeCode, TermCode... selectedConcepts) {
        if (selectedConcepts == null || selectedConcepts.length == 0) {
            throw new IllegalArgumentException("empty selected concepts");
        }
        return new ValueSetAttributeFilter(attributeCode, List.of(selectedConcepts));
    }

    @Override
    public Modifier toModifier(AttributeMapping attributeMapping) {
        return switch (attributeMapping.type()) {
            case "Code" -> CodeModifier.of(attributeMapping.path(), selectedConcepts.stream().map(TermCode::code).toArray(String[]::new));
            case "Coding" -> CodingModifier.of(attributeMapping.path(), selectedConcepts.toArray(TermCode[]::new));
            default -> throw new IllegalStateException("unknown attribute mapping type: %s".formatted(attributeMapping.type()));
        };
    }
}
