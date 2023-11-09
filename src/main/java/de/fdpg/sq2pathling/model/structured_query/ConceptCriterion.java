package de.fdpg.sq2pathling.model.structured_query;

import de.fdpg.sq2pathling.model.Mapping;
import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.fhirpath.BooleanExpression;
import java.util.List;

/**
 * A {@code ConceptCriterion} will select all patients that have at least one resource represented by that concept.
 * <p>
 * Examples are {@code Condition} resources representing the concept of a particular disease.
 */
public final class ConceptCriterion extends AbstractCriterion {

    private ConceptCriterion(ContextualConcept concept, List<AttributeFilter> attributeFilters, TimeRestriction timeRestriction) {
        super(concept, attributeFilters, timeRestriction);
    }

    /**
     * Returns a {@code ConceptCriterion}.
     *
     * @param concept          the concept the criterion represents
     * @param attributeFilters additional filters on particular attributes
     * @return the {@code ConceptCriterion}.
     */
    public static ConceptCriterion of(ContextualConcept concept, AttributeFilter... attributeFilters) {
        return new ConceptCriterion(concept, List.of(attributeFilters), null);
    }


    /**
     * Returns a {@code ConceptCriterion}.
     *
     * @param concept          the concept the criterion represents
     * @param timeRestriction  the time restriction on the critieria
     * @param attributeFilters additional filters on particular attributes
     * @return the {@code ConceptCriterion}.
     */
    public static ConceptCriterion of(ContextualConcept concept,
        TimeRestriction timeRestriction, AttributeFilter... attributeFilters) {
        return new ConceptCriterion(concept, List.of(attributeFilters), timeRestriction);
    }

    @Override
    BooleanExpression valueExpr(MappingContext mappingContext,
                                           Mapping mapping) {
        return BooleanExpression.TRUE;
    }
}
