package de.fdpg.sq2pathling.model.structured_query;

import static de.fdpg.sq2pathling.model.common.Comparator.GREATER_THAN;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fdpg.sq2pathling.PrintContext;
import de.fdpg.sq2pathling.model.Mapping;
import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.TermCodeNode;
import de.fdpg.sq2pathling.model.common.TermCode;
import de.fdpg.sq2pathling.model.structured_query.ContextualConcept;
import de.fdpg.sq2pathling.model.structured_query.ContextualTermCode;
import de.fdpg.sq2pathling.model.structured_query.Criterion;
import de.fdpg.sq2pathling.model.structured_query.NumericCriterion;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;


public class NumericCriterionTest {

  static final TermCode CONTEXT = TermCode.of("context", "context", "context");
  static final ContextualTermCode BODY_WEIGHT = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://loinc.org", "29463-7","Body Weight"));

  public static final MappingContext MAPPING_CONTEXT = MappingContext.of(
      Map.of(BODY_WEIGHT,
          Mapping.of(BODY_WEIGHT, "Observation", "code", "valueQuantity")),
      TermCodeNode.of(BODY_WEIGHT));

  @Test
  void fromJson() throws Exception {
    var mapper = new ObjectMapper();

    var criterion = (NumericCriterion) mapper.readValue("""
        {
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "http://loinc.org",
            "code": "29463-7",
            "display": "Body Weight"
          }],
          "valueFilter": {
            "type": "quantity-comparator",
            "comparator": "gt",
            "unit": {
              "code": "kg"
            },
            "value": 50
          }
        }
        """, Criterion.class);

    assertEquals(ContextualConcept.of(BODY_WEIGHT), criterion.getConcept());
    assertEquals(GREATER_THAN, criterion.getComparator());
    assertEquals(BigDecimal.valueOf(50), criterion.getValue());
    assertEquals(Optional.of("kg"), criterion.getUnit());
  }

  @Test
  void toPathling() throws JsonProcessingException {
    var mapper = new ObjectMapper();

    var criterion = (NumericCriterion) mapper.readValue("""
        {
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "http://loinc.org",
            "code": "29463-7",
            "display": "Body Weight"
          }],
          "valueFilter": {
            "type": "quantity-comparator",
            "comparator": "gt",
            "unit": {
              "code": "kg"
            },
            "value": 50
          }
        }
        """, Criterion.class);
    var pathling = criterion.toFhirPathFilter(MAPPING_CONTEXT);
    assertEquals("""
        reverseResolve(Observation.subject).exists(code.coding.where(system = 'http://loinc.org').exists(code = '29463-7') and
        valueQuantity > 50 'kg')""", pathling.print(PrintContext.ZERO));

    System.out.println(pathling.print(PrintContext.ZERO));
  }

}
