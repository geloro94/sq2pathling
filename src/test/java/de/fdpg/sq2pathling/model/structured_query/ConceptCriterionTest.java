package de.fdpg.sq2pathling.model.structured_query;

import static de.fdpg.sq2pathling.model.common.Comparator.LESS_THAN;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fdpg.sq2pathling.PrintContext;
import de.fdpg.sq2pathling.model.AttributeMapping;
import de.fdpg.sq2pathling.model.Mapping;
import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.TermCodeNode;
import de.fdpg.sq2pathling.model.common.TermCode;
import de.fdpg.sq2pathling.model.structured_query.CodeModifier;
import de.fdpg.sq2pathling.model.structured_query.CodingModifier;
import de.fdpg.sq2pathling.model.structured_query.Concept;
import de.fdpg.sq2pathling.model.structured_query.ConceptCriterion;
import de.fdpg.sq2pathling.model.structured_query.ContextualConcept;
import de.fdpg.sq2pathling.model.structured_query.ContextualTermCode;
import de.fdpg.sq2pathling.model.structured_query.Criterion;
import de.fdpg.sq2pathling.model.structured_query.NumericAttributeFilter;
import de.fdpg.sq2pathling.model.structured_query.RangeAttributeFilter;
import de.fdpg.sq2pathling.model.structured_query.ValueSetAttributeFilter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConceptCriterionTest {

  static final TermCode CONTEXT = TermCode.of("context", "context", "context");
  static final ContextualTermCode C71 = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71",
          "Malignant neoplasm of brain"));
  static final TermCode C71_1_TC = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.1",
      "Frontal lobe");
  static final ContextualTermCode C71_1 = ContextualTermCode.of(CONTEXT, C71_1_TC);
  static final TermCode C71_2_TC = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.2",
      "Temporal lobe");
  static final ContextualTermCode C71_2 = ContextualTermCode.of(CONTEXT, C71_2_TC);
  static final ContextualTermCode BLOOD_PRESSURE = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://loinc.org", "85354-9",
          "Blood pressure panel with all children optional"));
  static final TermCode DIASTOLIC_BLOOD_PRESSURE = TermCode.of("http://loinc.org", "8462-4",
      "Diastolic blood pressure");
  static final TermCode CONFIRMED = TermCode.of(
      "http://terminology.hl7.org/CodeSystem/condition-ver-status", "confirmed", "Confirmed");
  static final ContextualTermCode THERAPEUTIC_PROCEDURE = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://snomed.info/sct", "277132007", "Therapeutic procedure (procedure)"));

  static final TermCode VERIFICATION_STATUS = TermCode.of("hl7.org", "verificationStatus",
      "verificationStatus");

  static final Map<String, String> CODE_SYSTEM_ALIASES = Map.of(
      "http://fhir.de/CodeSystem/bfarm/icd-10-gm", "icd10", "http://snomed.info/sct", "snomed",
      "http://loinc.org", "loinc", "http://terminology.hl7.org/CodeSystem/condition-ver-status",
      "ver_status");

  @Test
  void fromJson() throws Exception {
    var mapper = new ObjectMapper();

    var criterion = (ConceptCriterion) mapper.readValue("""
        {
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
            "code": "C71",
            "display": "Malignant neoplasm of brain"
          }]
        }
        """, Criterion.class);

    assertEquals(ContextualConcept.of(C71), criterion.getConcept());
  }

  @Test
  void fromJson_WithMultipleTermCodes() throws Exception {
    var mapper = new ObjectMapper();

    var criterion = (ConceptCriterion) mapper.readValue("""
        {
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
            "code": "C71.1",
            "display": "Frontal lobe"
          }, {
            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
            "code": "C71.2",
            "display": "Temporal lobe"
          }]
        }
        """, Criterion.class);

    assertEquals(List.of(C71_1, C71_2), criterion.getConcept().contextualTermCodes());
  }

  @Test
  void fromJson_AdditionalPropertyIsIgnored() throws Exception {
    var mapper = new ObjectMapper();

    var criterion = (ConceptCriterion) mapper.readValue("""
        {
          "foo-151633": "bar-151639",
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
            "code": "C71",
            "display": "Malignant neoplasm of brain"
          }]
        }
        """, Criterion.class);

    assertEquals(ContextualConcept.of(C71), criterion.getConcept());
  }

  @Test
  void fromJson_BloodPressure() throws Exception {
    var mapper = new ObjectMapper();

    var criterion = (ConceptCriterion) mapper.readValue("""
        {
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "http://loinc.org",
            "code": "85354-9",
            "display": "Blood pressure panel with all children optional"
          }],
          "attributeFilters": [
            {
              "attributeCode": {
                "system": "http://loinc.org",
                "code": "8462-4",
                "display": "Diastolic blood pressure"
              },
              "type": "quantity-comparator",
              "comparator": "lt",
              "value": 80,
              "unit": {
                "code": "mm[Hg]"
              }
            }
          ]
        }
        """, Criterion.class);

    assertEquals(ContextualConcept.of(BLOOD_PRESSURE), criterion.getConcept());
    assertEquals(List.of(
        NumericAttributeFilter.of(DIASTOLIC_BLOOD_PRESSURE, LESS_THAN, BigDecimal.valueOf(80),
            "mm[Hg]")), criterion.attributeFilters);
  }

  @Test
  void fromJson_BloodPressureRange() throws Exception {
    var mapper = new ObjectMapper();

    var criterion = (ConceptCriterion) mapper.readValue("""
        {
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "http://loinc.org",
            "code": "85354-9",
            "display": "Blood pressure panel with all children optional"
          }],
          "attributeFilters": [
            {
              "attributeCode": {
                "system": "http://loinc.org",
                "code": "8462-4",
                "display": "Diastolic blood pressure"
              },
              "type": "quantity-range",
              "minValue": 60,
              "maxValue": 100,
              "unit": {
                "code": "mm[Hg]"
              }
            }
          ]
        }
        """, Criterion.class);

    assertEquals(ContextualConcept.of(BLOOD_PRESSURE), criterion.getConcept());
    assertEquals(List.of(RangeAttributeFilter.of(DIASTOLIC_BLOOD_PRESSURE, BigDecimal.valueOf(60),
        BigDecimal.valueOf(100), "mm[Hg]")), criterion.attributeFilters);
  }

  @Test
  void toPathling() {
    var criterion = ConceptCriterion.of(ContextualConcept.of(C71));
    var mappingContext = MappingContext.of(Map.of(C71, Mapping.of(C71, "Condition", "code.coding")),
        TermCodeNode.of(C71));

    var expression = criterion.toFhirPathFilter(mappingContext);

    assertEquals("""
            reverseResolve(Condition.subject).exists(code.coding.where(system = 'http://fhir.de/CodeSystem/bfarm/icd-10-gm').exists(code = 'C71'))""",
        expression.print(PrintContext.ZERO));
  }

  @Test
  void toPathling_WithMultipleTermCodes() {
    var criterion = ConceptCriterion.of(
        ContextualConcept.of(CONTEXT, Concept.of(C71_1_TC, C71_2_TC)));
    var mappings = Map.of(C71_1, Mapping.of(C71_1, "Condition", "code.coding"), C71_2,
        Mapping.of(C71_2, "Condition", "code.coding"));
    var mappingContext = MappingContext.of(mappings, TermCodeNode.of(C71));

    var expression = criterion.toFhirPathFilter(mappingContext);

    assertEquals("""
            reverseResolve(Condition.subject).exists(code.coding.where(system = 'http://fhir.de/CodeSystem/bfarm/icd-10-gm').exists(code = 'C71.1')) or
            reverseResolve(Condition.subject).exists(code.coding.where(system = 'http://fhir.de/CodeSystem/bfarm/icd-10-gm').exists(code = 'C71.2'))""",
        PrintContext.ZERO.print(expression));
  }

  @Test
  void toPathling_WithAttributeFilter() {
    var criterion = ConceptCriterion.of(ContextualConcept.of(C71));
    criterion.appendAttributeFilter(ValueSetAttributeFilter.of(VERIFICATION_STATUS, CONFIRMED));
    var mapping = Mapping.of(C71, "Condition", "code.coding", null, null, List.of(),
        List.of(AttributeMapping.of("Coding", VERIFICATION_STATUS, "verificationStatus")));
    var mappingContext = MappingContext.of(Map.of(C71, mapping), TermCodeNode.of(C71));

    var expression = criterion.toFhirPathFilter(mappingContext);

    assertEquals("""
            reverseResolve(Condition.subject).exists(code.coding.where(system = 'http://fhir.de/CodeSystem/bfarm/icd-10-gm').exists(code = 'C71') and
            (verificationStatus.coding.where(system = 'http://terminology.hl7.org/CodeSystem/condition-ver-status').exists(code = 'confirmed')))""",
        PrintContext.ZERO.print(expression));
  }

  @Test
  void toPathling_Expanded_WithAttributeFilter() {
    var criterion = ConceptCriterion.of(ContextualConcept.of(C71));
    criterion.appendAttributeFilter(ValueSetAttributeFilter.of(VERIFICATION_STATUS, CONFIRMED));
    var mapping1 = Mapping.of(C71_1, "Condition", "code.coding", null, null, List.of(),
        List.of(AttributeMapping.of("Coding", VERIFICATION_STATUS, "verificationStatus")));
    var mapping2 = Mapping.of(C71_2, "Condition", "code.coding", null, null, List.of(),
        List.of(AttributeMapping.of("Coding", VERIFICATION_STATUS, "verificationStatus")));
    var mappingContext = MappingContext.of(Map.of(C71_1, mapping1, C71_2, mapping2),
        TermCodeNode.of(C71, TermCodeNode.of(C71_1), TermCodeNode.of(C71_2)));

    var expression = criterion.toFhirPathFilter(mappingContext);

    assertEquals("""
            reverseResolve(Condition.subject).exists(code.coding.where(system = 'http://fhir.de/CodeSystem/bfarm/icd-10-gm').exists(code = 'C71.1') and
            (verificationStatus.coding.where(system = 'http://terminology.hl7.org/CodeSystem/condition-ver-status').exists(code = 'confirmed'))) or
            reverseResolve(Condition.subject).exists(code.coding.where(system = 'http://fhir.de/CodeSystem/bfarm/icd-10-gm').exists(code = 'C71.2') and
            (verificationStatus.coding.where(system = 'http://terminology.hl7.org/CodeSystem/condition-ver-status').exists(code = 'confirmed')))""",
        PrintContext.ZERO.print(expression));
  }

  @Test
  void toPathling_WithDiastolicBloodPressureAttributeFilter() {
    var criterion = ConceptCriterion.of(ContextualConcept.of(BLOOD_PRESSURE));
    criterion.appendAttributeFilter(
        NumericAttributeFilter.of(DIASTOLIC_BLOOD_PRESSURE, LESS_THAN, BigDecimal.valueOf(80),
            "mm[Hg]"));
    var mappingContext = MappingContext.of(Map.of(BLOOD_PRESSURE,
        Mapping.of(BLOOD_PRESSURE, "Observation", "code.coding", "value", null, List.of(), List.of(
            AttributeMapping.of("", DIASTOLIC_BLOOD_PRESSURE, format(
                "component.where(code.coding.where(system = '%s').exists(code = '%s')).value.first()",
                DIASTOLIC_BLOOD_PRESSURE.system(), DIASTOLIC_BLOOD_PRESSURE.code()))))), null);

    var expression = criterion.toFhirPathFilter(mappingContext);

    assertEquals("""
            reverseResolve(Observation.subject).exists(code.coding.where(system = 'http://loinc.org').exists(code = '85354-9') and
            component.where(code.coding.where(system = 'http://loinc.org').exists(code = '8462-4')).value.first() < 80 'mm[Hg]')""",
        PrintContext.ZERO.print(expression));
  }

  @Test
  void toPathling_FixedCriteria_Code() {
    var criterion = ConceptCriterion.of(ContextualConcept.of(THERAPEUTIC_PROCEDURE));
    var mappingContext = MappingContext.of(Map.of(THERAPEUTIC_PROCEDURE,
        Mapping.of(THERAPEUTIC_PROCEDURE, "Procedure", "code.coding", null, null,
            List.of(CodeModifier.of("status", "completed", "in-progress")), List.of())), null);

    var expression = criterion.toFhirPathFilter(mappingContext);

    assertEquals("""
            reverseResolve(Procedure.subject).exists(code.coding.where(system = 'http://snomed.info/sct').exists(code = '277132007') and
            (status = 'completed' or
            status = 'in-progress'))""",
        PrintContext.ZERO.print(expression));
  }

  @Test
  void toPathling_FixedCriteria_Coding() {
    var criterion = ConceptCriterion.of(ContextualConcept.of(C71));
    var mappingContext = MappingContext.of(
        Map.of(C71, Mapping.of(C71, "Condition", "code.coding", null, null,
            List.of(CodingModifier.of("verificationStatus", CONFIRMED)), List.of())),
        TermCodeNode.of(C71));

    var expression = criterion.toFhirPathFilter(mappingContext);

    assertEquals("""
            reverseResolve(Condition.subject).exists(code.coding.where(system = 'http://fhir.de/CodeSystem/bfarm/icd-10-gm').exists(code = 'C71') and
            (verificationStatus.coding.where(system = 'http://terminology.hl7.org/CodeSystem/condition-ver-status').exists(code = 'confirmed')))""",
        PrintContext.ZERO.print(expression));
  }
}