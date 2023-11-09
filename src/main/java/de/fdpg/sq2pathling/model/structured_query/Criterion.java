package de.fdpg.sq2pathling.model.structured_query;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.common.Comparator;
import de.fdpg.sq2pathling.model.common.TermCode;
import de.fdpg.sq2pathling.model.fhirpath.BooleanExpression;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * A single, atomic criterion in Structured Query.
 *
 * @author Alexander Kiel
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public interface Criterion {

    /**
     * A criterion that always evaluates to {@code true}.
     */
    Criterion TRUE = new Criterion() {

        @Override
        public ContextualConcept getConcept() {
            return null;
        }

        @Override
        public BooleanExpression toFhirPathFilter(MappingContext mappingContext) {
            return BooleanExpression.TRUE;
        }

        @Override
        public TimeRestriction timeRestriction() {
            return null;
        }
    };

    /**
     * A criterion that always evaluates to {@code false}.
     */
    Criterion FALSE = new Criterion() {

        @Override
        public ContextualConcept getConcept() {
            return null;
        }

        @Override
        public BooleanExpression toFhirPathFilter(MappingContext mappingContext) {
            return BooleanExpression.FALSE;
        }

        @Override
        public TimeRestriction timeRestriction() {
            return null;
        }
    };

    @JsonCreator
    static Criterion create(@JsonProperty("context") TermCode context,
                            @JsonProperty("termCodes") List<TermCode> termCodes,
                            @JsonProperty("valueFilter") ObjectNode valueFilter,
                            @JsonProperty("timeRestriction") TimeRestriction conceptTimeRestriction,
                            @JsonProperty("attributeFilters") List<ObjectNode> attributeFilters) {
        var concept = ContextualConcept.of(requireNonNull(context, "missing JSON property: context"),
            Concept.of(requireNonNull(termCodes, "missing JSON property: termCodes")));

        var attributes = (attributeFilters == null ? List.<ObjectNode>of() : attributeFilters).stream()
                .map(AttributeFilter::fromJsonNode)
                .flatMap(Optional::stream)
                .toArray(AttributeFilter[]::new);

        if (valueFilter == null) {
            return ConceptCriterion.of(concept, conceptTimeRestriction, attributes);
        }

        var type = valueFilter.get("type").asText();
        if ("quantity-comparator".equals(type)) {
            var comparator = Comparator.fromJson(valueFilter.get("comparator").asText());
            var value = valueFilter.get("value").decimalValue();
            var unit = valueFilter.get("unit");
            if (unit == null) {
                return NumericCriterion.of(concept, comparator, value, conceptTimeRestriction, attributes);
            } else {
                return NumericCriterion.of(concept, comparator, value, unit.get("code").asText(), conceptTimeRestriction, attributes);
            }
        }
        if ("quantity-range".equals(type)) {
            var lowerBound = valueFilter.get("minValue").decimalValue();
            var upperBound = valueFilter.get("maxValue").decimalValue();
            var unit = valueFilter.get("unit");
            if (unit == null) {
                return RangeCriterion.of(concept, lowerBound, upperBound, conceptTimeRestriction, attributes);
            } else {
                return RangeCriterion.of(concept, lowerBound, upperBound, unit.get("code").asText(), conceptTimeRestriction, attributes);
            }
        }
        if ("concept".equals(type)) {
            var selectedConcepts = valueFilter.get("selectedConcepts");
            if (selectedConcepts == null || selectedConcepts.isEmpty()) {
                throw new IllegalArgumentException("Missing or empty `selectedConcepts` key in concept criterion.");
            }
            return ValueSetCriterion.of(concept, StreamSupport.stream(selectedConcepts.spliterator(), false)
                    .map(TermCode::fromJsonNode).toList(), conceptTimeRestriction, attributes);
        }
        throw new IllegalArgumentException("unknown valueFilter type: " + type);
    }

    /**
     * Translates this criterion into a FHIR Path filter expression.
     *
     * @param mappingContext contains the mappings needed to create the CQL expression
     * @return a {@link BooleanExpression}
     */
    BooleanExpression toFhirPathFilter(MappingContext mappingContext);

    TimeRestriction timeRestriction();

    ContextualConcept getConcept();
}
