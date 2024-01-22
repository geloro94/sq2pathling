package de.fdpg.sq2pathling.model.structured_query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fdpg.sq2pathling.PrintContext;
import de.fdpg.sq2pathling.model.AttributeMapping;
import de.fdpg.sq2pathling.model.Mapping;
import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.common.TermCode;
import de.fdpg.sq2pathling.model.structured_query.CodeModifier;
import de.fdpg.sq2pathling.model.structured_query.Concept;
import de.fdpg.sq2pathling.model.structured_query.ContextualConcept;
import de.fdpg.sq2pathling.model.structured_query.ContextualTermCode;
import de.fdpg.sq2pathling.model.structured_query.Criterion;
import de.fdpg.sq2pathling.model.structured_query.ValueSetAttributeFilter;
import de.fdpg.sq2pathling.model.structured_query.ValueSetCriterion;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ValueSetCriterionTest {

  static final TermCode CONTEXT = TermCode.of("context", "context", "context");
  static final ContextualTermCode COVID = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://loinc.org", "94500-6", "COVID"));
  static final ContextualTermCode SEX = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://loinc.org", "76689-9", "Sex assigned at birth"));
  static final TermCode POSITIVE = TermCode.of("http://snomed.info/sct", "positive", "positive");
  static final TermCode MALE = TermCode.of("http://hl7.org/fhir/administrative-gender", "male",
      "Male");
  static final TermCode FEMALE = TermCode.of("http://hl7.org/fhir/administrative-gender", "female",
      "Female");
  static final ContextualTermCode FINDING = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://snomed.info/sct", "404684003", "Clinical finding (finding)"));
  static final TermCode SEVERE = TermCode.of("http://snomed.info/sct", "24484000",
      "Severe (severity modifier)");
  static final TermCode TNM_C_TC = TermCode.of("http://loinc.org", "21908-9",
      "Tumor size.clinical Cancer");
  static final TermCode TNM_P_TC = TermCode.of("http://loinc.org", "21902-2",
      "Tumor size.pathology Cancer");
  static final ContextualTermCode TNM_C = ContextualTermCode.of(CONTEXT, TNM_C_TC);
  static final ContextualTermCode TNM_P = ContextualTermCode.of(CONTEXT, TNM_P_TC);
  static final TermCode LA3649_6 = TermCode.of("http://loinc.org", "LA3649-6", "Stage IVB");
  static final TermCode STATUS = TermCode.of("http://hl7.org/fhir", "observation-status",
      "observation-status");
  static final TermCode FINAL = TermCode.of("http://hl7.org/fhir/observation-status", "final",
      "final");
  static final ContextualTermCode ETHNIC_GROUP = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://snomed.info/sct", "372148003", "Ethnic group (ethnic group)"));
  static final TermCode MIXED = TermCode.of("http://snomed.info/sct", "26242008",
      "Mixed (qualifier value)");
  static final ContextualTermCode GENDER = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://snomed.info/sct", "263495000", "Gender"));

  static final Map<String, String> CODE_SYSTEM_ALIASES = Map.of(
      "http://loinc.org", "loinc",
      "http://snomed.info/sct", "snomed",
      "http://hl7.org/fhir/administrative-gender", "gender");


  static final MappingContext MAPPING_CONTEXT = MappingContext.of(Map.of(
      COVID, Mapping.of(COVID, "Observation", "code.coding", "valueCodeableConcept.coding", "Coding", List.of(),
          List.of(AttributeMapping.of("Code", STATUS, "status")), null),
      SEX, Mapping.of(SEX, "Observation", "code.coding", "value.coding", "Coding"),
      FINDING, Mapping.of(FINDING, "Condition", "code.coding", "severity.coding", "Coding"),
      TNM_C, Mapping.of(TNM_C, "Observation", "code.coding", "value", "C"),
      TNM_P, Mapping.of(TNM_P, "Observation", "code.coding", "value")
  ), null);


  @Test
  void fromJson_WithTwoSelectedConcepts() throws Exception {
    var mapper = new ObjectMapper();

    var criterion = (ValueSetCriterion) mapper.readValue("""
        {
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "http://loinc.org",
            "code": "76689-9",
            "display": "Sex assigned at birth"
          }],
          "valueFilter": {
            "type": "concept",
            "selectedConcepts": [
              {
                "system": "http://hl7.org/fhir/administrative-gender",
                "code": "male",
                "display": "Male"
              },
              {
                "system": "http://hl7.org/fhir/administrative-gender",
                "code": "female",
                "display": "Female"
              }
            ]
          }
        }
        """, Criterion.class);

    assertEquals(ContextualConcept.of(SEX), criterion.getConcept());
    assertEquals(List.of(MALE, FEMALE), criterion.getSelectedConcepts());
  }

  @Test
  void fromJson_WithMissingSelectedConcepts() {
    var mapper = new ObjectMapper();

    var error = assertThrows(JsonMappingException.class, () -> mapper.readValue("""
        {
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "http://loinc.org",
            "code": "76689-9",
            "display": "Sex assigned at birth"
          }],
          "valueFilter": {
            "type": "concept"
          }
        }
        """, Criterion.class));

    assertTrue(error.getMessage().startsWith(
        "Cannot construct instance of `de.fdpg.sq2pathling.model.structured_query.Criterion`"));
  }

  @Test
  void fromJson_WithEmptySelectedConcepts() {
    var mapper = new ObjectMapper();

    var error = assertThrows(JsonMappingException.class, () -> mapper.readValue("""
        {
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "http://loinc.org",
            "code": "76689-9",
            "display": "Sex assigned at birth"
          }],
          "valueFilter": {
            "type": "concept",
            "selectedConcepts": []
          }
        }
        """, Criterion.class));

    assertTrue(error.getMessage().startsWith(
        "Cannot construct instance of `de.fdpg.sq2pathling.model.structured_query.Criterion`"));
  }

  @Test
  void fromJson_WithAttributeFilter() throws Exception {
    var mapper = new ObjectMapper();

    var criterion = (ValueSetCriterion) mapper.readValue("""
        {
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "http://loinc.org",
            "code": "76689-9",
            "display": "Sex assigned at birth"
          }],
          "valueFilter": {
            "type": "concept",
            "selectedConcepts": [
              {
                "system": "http://hl7.org/fhir/administrative-gender",
                "code": "male",
                "display": "Male"
              }
            ]
          },
          "attributeFilters": [
            {
              "attributeCode": {
                "system": "http://hl7.org/fhir",
                "code": "observation-status",
                "display": "observation-status"
              },
              "type": "concept",
              "selectedConcepts": [
                {
                  "system": "http://hl7.org/fhir/observation-status",
                  "code": "final",
                  "display": "final"
                }
              ]
            }
          ]
        }
        """, Criterion.class);

    assertEquals(ContextualConcept.of(SEX), criterion.getConcept());
    assertEquals(List.of(MALE), criterion.getSelectedConcepts());
    assertEquals(List.of(ValueSetAttributeFilter.of(STATUS, FINAL)), criterion.attributeFilters);
  }

  @Test
  void fromJson_WithAttributeFilterAndMissingSelectedConcepts() throws Exception {
    var mapper = new ObjectMapper();

    var criterion = (ValueSetCriterion) mapper.readValue("""
        {
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "http://loinc.org",
            "code": "76689-9",
            "display": "Sex assigned at birth"
          }],
          "valueFilter": {
            "type": "concept",
            "selectedConcepts": [
              {
                "system": "http://hl7.org/fhir/administrative-gender",
                "code": "male",
                "display": "Male"
              }
            ]
          },
          "attributeFilters": [
            {
              "attributeCode": {
                "system": "http://hl7.org/fhir",
                "code": "observation-status",
                "display": "observation-status"
              },
              "type": "concept"
            }
          ]
        }
        """, Criterion.class);

    assertTrue(criterion.attributeFilters.isEmpty());
  }

  @Test
  void fromJson_WithAttributeFilterAndEmptySelectedConcepts() throws Exception {
    var mapper = new ObjectMapper();

    var criterion = (ValueSetCriterion) mapper.readValue("""
        {
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "http://loinc.org",
            "code": "76689-9",
            "display": "Sex assigned at birth"
          }],
          "valueFilter": {
            "type": "concept",
            "selectedConcepts": [
              {
                "system": "http://hl7.org/fhir/administrative-gender",
                "code": "male",
                "display": "Male"
              }
            ]
          },
          "attributeFilters": [
            {
              "attributeCode": {
                "system": "http://hl7.org/fhir",
                "code": "observation-status",
                "display": "observation-status"
              },
              "type": "concept",
              "selectedConcepts": []
            }
          ]
        }
        """, Criterion.class);

    assertTrue(criterion.attributeFilters.isEmpty());
  }

  @Test
  void toPathling_WithNoConcept() {
    assertThrows(IllegalArgumentException.class,
        () -> ValueSetCriterion.of(ContextualConcept.of(COVID)));
  }

  @Test
  void toPathling_WithOneConcept() {
    var criterion = ValueSetCriterion.of(ContextualConcept.of(COVID), POSITIVE);

    var container = criterion.toFhirPathFilter(MAPPING_CONTEXT);

    System.out.println(criterion.toFhirPathFilter(MAPPING_CONTEXT).print(PrintContext.ZERO));

    assertEquals("""
            reverseResolve(Observation.subject).exists(code.coding.where(system = 'http://loinc.org').exists(code = '94500-6') and
            (valueCodeableConcept.coding.where(system = 'http://snomed.info/sct').exists(code = 'positive')))""",
        PrintContext.ZERO.print(container));
  }

  @Test
  void toPathling_WithOneConceptAndMultipleTermCodes() {
    var criterion = ValueSetCriterion.of(
        ContextualConcept.of(CONTEXT, Concept.of(TNM_C_TC, TNM_P_TC)), LA3649_6);

    var container = criterion.toFhirPathFilter(MAPPING_CONTEXT);

    assertEquals("""
            reverseResolve(Observation.subject).exists(code.coding.where(system = 'http://loinc.org').exists(code = '21908-9') and
            value = 'LA3649-6') or
            reverseResolve(Observation.subject).exists(code.coding.where(system = 'http://loinc.org').exists(code = '21902-2') and
            value = 'LA3649-6')""",
        PrintContext.ZERO.print(container));
  }

  @Test
  void toPathling_WithTwoConcepts() {
    var criterion = ValueSetCriterion.of(ContextualConcept.of(SEX), MALE, FEMALE);

    var container = criterion.toFhirPathFilter(MAPPING_CONTEXT);

    assertEquals("""
            reverseResolve(Observation.subject).exists(code.coding.where(system = 'http://loinc.org').exists(code = '76689-9') and
            (value.coding.where(system = 'http://hl7.org/fhir/administrative-gender').exists(code = 'male') or
            value.coding.where(system = 'http://hl7.org/fhir/administrative-gender').exists(code = 'female')))""",
        PrintContext.ZERO.print(container));
  }

  @Test
  void toPathling_WithConditionSeverity() {
    var criterion = ValueSetCriterion.of(ContextualConcept.of(FINDING), SEVERE);

    var container = criterion.toFhirPathFilter(MAPPING_CONTEXT);

    assertEquals("""
            reverseResolve(Condition.subject).exists(code.coding.where(system = 'http://snomed.info/sct').exists(code = '404684003') and
            (severity.coding.where(system = 'http://snomed.info/sct').exists(code = '24484000')))""",
        PrintContext.ZERO.print(container));
  }

  @Test
  void toPathling_WithAttributeFilter() {
    var criterion = ValueSetCriterion.of(ContextualConcept.of(COVID), POSITIVE);
    criterion.appendAttributeFilter(ValueSetAttributeFilter.of(STATUS, FINAL));

    var container = criterion.toFhirPathFilter(MAPPING_CONTEXT);

    assertEquals("""
            reverseResolve(Observation.subject).exists(code.coding.where(system = 'http://loinc.org').exists(code = '94500-6') and
            (valueCodeableConcept.coding.where(system = 'http://snomed.info/sct').exists(code = 'positive')) and
            status = 'final')""",
        PrintContext.ZERO.print(container));
  }

  @Test
  void toPathling_WithFixedCriteria() {
    var criterion = ValueSetCriterion.of(ContextualConcept.of(COVID), POSITIVE);
    var mappingContext = MappingContext.of(Map.of(
        COVID, Mapping.of(COVID, "Observation", "code.coding", "valueCodeableConcept.coding", "Coding",
            List.of(CodeModifier.of("status", "final")),
            List.of())
    ), null);

    var container = criterion.toFhirPathFilter(mappingContext);

    assertEquals("""
            reverseResolve(Observation.subject).exists(code.coding.where(system = 'http://loinc.org').exists(code = '94500-6') and
            (valueCodeableConcept.coding.where(system = 'http://snomed.info/sct').exists(code = 'positive')) and
            status = 'final')""",
        PrintContext.ZERO.print(container));
  }

    @Test
  void toPathling_WithCodingValueTypeOnPatient() {
    var criterion = ValueSetCriterion.of(ContextualConcept.of(ETHNIC_GROUP), MIXED);
    var mappingContext = MappingContext.of(Map.of(
        ETHNIC_GROUP, Mapping.of(ETHNIC_GROUP, "Patient", "",
            "extension.where(url='https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/ethnic-group').first().value().coding",
            "Coding")
    ), null);

    var container = criterion.toFhirPathFilter(mappingContext);

    assertEquals(
        "extension.where(url='https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/ethnic-group').first().value().coding.where(system = 'http://snomed.info/sct').exists(code = '26242008')",
        PrintContext.ZERO.print(container));
  }

  @Test
  void toPathling_WithPatientGender() {
    var criterion = ValueSetCriterion.of(ContextualConcept.of(GENDER), MALE);
    var mappingContext = MappingContext.of(Map.of(
        GENDER, Mapping.of(GENDER, "Patient", "", "gender", "Code")
    ), null);

    var container = criterion.toFhirPathFilter(mappingContext);

    assertThat(PrintContext.ZERO.print(container)).isEqualTo("gender = 'male'");
  }

  @Test
  void toPathling_WithPatientGender_TwoConcepts() {
    var criterion = ValueSetCriterion.of(ContextualConcept.of(GENDER), MALE, FEMALE);
    var mappingContext = MappingContext.of(Map.of(
        GENDER, Mapping.of(GENDER, "Patient", "", "gender")
    ), null);

    var container = criterion.toFhirPathFilter(mappingContext);

    System.out.println(PrintContext.ZERO.print(container));

    assertThat(PrintContext.ZERO.print(container)).isEqualTo("""
        gender = 'male' or
        gender = 'female'""");
  }
}
