package de.fdpg.sq2pathling.model.structured_query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fdpg.sq2pathling.PrintContext;
import de.fdpg.sq2pathling.model.AttributeMapping;
import de.fdpg.sq2pathling.model.Mapping;
import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.common.TermCode;
import de.fdpg.sq2pathling.model.structured_query.CodeModifier;
import de.fdpg.sq2pathling.model.structured_query.ContextualConcept;
import de.fdpg.sq2pathling.model.structured_query.ContextualTermCode;
import de.fdpg.sq2pathling.model.structured_query.Criterion;
import de.fdpg.sq2pathling.model.structured_query.RangeCriterion;
import de.fdpg.sq2pathling.model.structured_query.ValueSetAttributeFilter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RangeCriterionTest {

  static final TermCode CONTEXT = TermCode.of("context", "context", "context");
  static final ContextualTermCode PLATELETS = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://loinc.org", "26515-7", "Platelets"));
  static final ContextualTermCode OTHER_VALUE_PATH = ContextualTermCode.of(CONTEXT,
      TermCode.of("foo", "other-value-path", ""));
  static final TermCode STATUS = TermCode.of("http://hl7.org/fhir", "observation-status",
      "observation-status");
  static final TermCode FINAL = TermCode.of("http://hl7.org/fhir/observation-status", "final",
      "final");


  static final MappingContext MAPPING_CONTEXT = MappingContext.of(Map.of(
      PLATELETS, Mapping.of(PLATELETS, "Observation", "code", "value", null, List.of(),
          List.of(AttributeMapping.of("Code", STATUS, "status"))),
      OTHER_VALUE_PATH, Mapping.of(OTHER_VALUE_PATH, "Observation", "code", "other")
  ), null);


  @Test
  void fromJson() throws Exception {
    var mapper = new ObjectMapper();

    var criterion = (RangeCriterion) mapper.readValue("""
        {
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "http://loinc.org",
            "code": "26515-7",
            "display": "Platelets"
          }],
          "valueFilter": {
            "type": "quantity-range",
            "unit": {
              "code": "g/dl"
            },
            "minValue": 20,
            "maxValue": 30
          }
        }
        """, Criterion.class);

    assertEquals(ContextualConcept.of(PLATELETS), criterion.getConcept());
    assertEquals(BigDecimal.valueOf(20), criterion.getLowerBound());
    assertEquals(BigDecimal.valueOf(30), criterion.getUpperBound());
    assertEquals(Optional.of("g/dl"), criterion.getUnit());
  }

  @Test
  void fromJson_withoutUnit() throws Exception {
    var mapper = new ObjectMapper();

    var criterion = (RangeCriterion) mapper.readValue("""
        {
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "system-140946",
            "code": "code-140950",
            "display": ""
          }],
          "valueFilter": {
            "type": "quantity-range",
            "minValue": 0,
            "maxValue": 1
          }
        }
        """, Criterion.class);

    assertEquals(BigDecimal.valueOf(0), criterion.getLowerBound());
    assertEquals(BigDecimal.valueOf(1), criterion.getUpperBound());
    assertEquals(Optional.empty(), criterion.getUnit());
  }

  @Test
  void toCql() {
    var criterion = RangeCriterion.of(ContextualConcept.of(PLATELETS), BigDecimal.valueOf(20),
        BigDecimal.valueOf(30), "g/dl");

    var expression = criterion.toFhirPathFilter(MAPPING_CONTEXT);

    assertEquals("""
            reverseResolve(Observation.subject).exists(code.coding.where(system = 'http://loinc.org').exists(code = '26515-7') and
            value >= 20 'g/dl' and
            value <= 30 'g/dl')""",
        PrintContext.ZERO.print(expression));
  }

  @Test
  void toCql_WithOtherFhirValuePath() {
    var criterion = RangeCriterion.of(ContextualConcept.of(OTHER_VALUE_PATH), BigDecimal.valueOf(1),
        BigDecimal.valueOf(2));

    var expression = criterion.toFhirPathFilter(MAPPING_CONTEXT);

    assertEquals("""
            reverseResolve(Observation.subject).exists(code.coding.where(system = 'foo').exists(code = 'other-value-path') and
            other >= 1 and
            other <= 2)""",
        PrintContext.ZERO.print(expression));
  }

  @Test
  void toCql_WithAttributeFilter() {
    var criterion = RangeCriterion.of(ContextualConcept.of(PLATELETS), BigDecimal.valueOf(20),
        BigDecimal.valueOf(30),
        "g/dl");
    criterion.appendAttributeFilter(ValueSetAttributeFilter.of(STATUS, FINAL));

    var expression = criterion.toFhirPathFilter(MAPPING_CONTEXT);

    assertEquals("""
            reverseResolve(Observation.subject).exists(code.coding.where(system = 'http://loinc.org').exists(code = '26515-7') and
            value >= 20 'g/dl' and
            value <= 30 'g/dl' and
            status = 'final')""",
        PrintContext.ZERO.print(expression));
  }

  @Test
  void toCql_WithFixedCriteria() {
    var criterion = RangeCriterion.of(ContextualConcept.of(PLATELETS), BigDecimal.valueOf(20),
        BigDecimal.valueOf(30), "g/dl");
    var mappingContext = MappingContext.of(Map.of(
        PLATELETS, Mapping.of(PLATELETS, "Observation", "code", "value", null,
            List.of(CodeModifier.of("status", "final")),
            List.of())
    ), null);

    var container = criterion.toFhirPathFilter(mappingContext);

    assertEquals("""
            reverseResolve(Observation.subject).exists(code.coding.where(system = 'http://loinc.org').exists(code = '26515-7') and
            value >= 20 'g/dl' and
            value <= 30 'g/dl' and
            status = 'final')""",
        PrintContext.ZERO.print(container));
  }
}