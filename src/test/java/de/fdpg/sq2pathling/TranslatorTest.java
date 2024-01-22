package de.fdpg.sq2pathling;

import static de.fdpg.sq2pathling.model.common.Comparator.LESS_THAN;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.fdpg.sq2pathling.model.AttributeMapping;
import de.fdpg.sq2pathling.model.Mapping;
import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.TermCodeNode;
import de.fdpg.sq2pathling.model.common.TermCode;
import de.fdpg.sq2pathling.model.structured_query.CodingModifier;
import de.fdpg.sq2pathling.model.structured_query.ConceptCriterion;
import de.fdpg.sq2pathling.model.structured_query.ContextualConcept;
import de.fdpg.sq2pathling.model.structured_query.ContextualTermCode;
import de.fdpg.sq2pathling.model.structured_query.Criterion;
import de.fdpg.sq2pathling.model.structured_query.NumericCriterion;
import de.fdpg.sq2pathling.model.structured_query.StructuredQuery;
import de.fdpg.sq2pathling.model.structured_query.TimeRestriction;
import de.fdpg.sq2pathling.model.structured_query.TranslationException;
import de.fdpg.sq2pathling.model.structured_query.ValueSetAttributeFilter;
import de.fdpg.sq2pathling.model.structured_query.ValueSetCriterion;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

/**
 * @author Lorenz Rosenau
 */
class TranslatorTest {

  static final TermCode CONTEXT = TermCode.of("context", "context", "context");
  static final ContextualTermCode COMBINED_CONSENT = ContextualTermCode.of(CONTEXT,
      TermCode.of("mii.abide", "combined-consent", ""));
  static final ContextualTermCode AGE = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://snomed.info/sct", "424144002", "Current chronological age"));
  static final ContextualTermCode GENDER = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://snomed.info/sct", "263495000", "Gender"));
  static final TermCode CONTEXT_CONSENT = TermCode.of("fdpg.mii.cds", "Einwilligung", "Einwilligung");
  static final ContextualTermCode CONSENT_MDAT = ContextualTermCode.of(CONTEXT_CONSENT,
      TermCode.of("urn:oid:2.16.840.1.113883.3.1937.777.24.5.3",
          "2.16.840.1.113883.3.1937.777.24.5.3.8",
          "MDAT wissenschaftlich nutzen EU DSGVO NIVEAU"));
  static final ContextualTermCode ROOT = ContextualTermCode.of(CONTEXT, TermCode.of("", "", ""));
  static final ContextualTermCode C71 = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71",
          "Malignant neoplasm of brain"));
  static final ContextualTermCode C71_0 = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.0",
          "Malignant neoplasm of frontal lobe"));
  static final ContextualTermCode C71_1 = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.1",
          "Malignant neoplasm of temporal lobe"));
  static final ContextualTermCode PLATELETS = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://loinc.org", "26515-7", "Platelets"));
  static final ContextualTermCode FRAILTY_SCORE = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://snomed.info/sct",
          "713636003", "Canadian Study of Health and Aging Clinical Frailty Scale score"));
  static final TermCode VERY_FIT = TermCode.of(
      "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty-score", "1",
      "Very Fit");
  static final TermCode WELL = TermCode.of(
      "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty-score", "2", "Well");
  static final ContextualTermCode COPD = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://snomed.info/sct", "13645005",
          "Chronic obstructive lung disease (disorder)"));
  static final ContextualTermCode G47_31 = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "G47.31",
          "Obstruktives Schlafapnoe-Syndrom"));
  static final ContextualTermCode TOBACCO_SMOKING_STATUS = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://loinc.org", "72166-2", "Tobacco smoking status"));
  static final TermCode CURRENT_EVERY_DAY_SMOKER = TermCode.of("http://loinc.org", "LA18976-3",
      "Current every day smoker");
  static final ContextualTermCode HYPERTENSION = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "I10",
          "Essential (Primary) Hypertension"));
  static final ContextualTermCode SERUM = ContextualTermCode.of(CONTEXT,
      TermCode.of("https://fhir.bbmri.de/CodeSystem/SampleMaterialType", "Serum", "Serum"));
  static final ContextualTermCode TMZ = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://fhir.de/CodeSystem/dimdi/atc", "L01AX03", "Temozolomide"));
  static final ContextualTermCode LIPID = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://fhir.de/CodeSystem/dimdi/atc", "C10AA", "lipid lowering drugs"));
  static final TermCode CONFIRMED = TermCode.of(
      "http://terminology.hl7.org/CodeSystem/condition-ver-status", "confirmed", "Confirmed");
  static final TermCode VERIFICATION_STATUS = TermCode.of("hl7.org", "verificationStatus",
      "verificationStatus");
  static final AttributeMapping VERIFICATION_STATUS_ATTR_MAPPING = AttributeMapping.of("Coding",
      VERIFICATION_STATUS, "verificationStatus.coding");

  private Mapping readMapping(String s) throws Exception {
    return new ObjectMapper().readValue(s, Mapping.class);
  }

  private StructuredQuery readStructuredQuery(String s) throws Exception {
    return new ObjectMapper().readValue(s, StructuredQuery.class);
  }

  @Nested
  class toPathling {

    @Test
    void nonExpandableConcept() {
      var structuredQuery = StructuredQuery.of(List.of(List.of(ConceptCriterion.of(
          ContextualConcept.of(C71)))));

      var message = assertThrows(TranslationException.class, () -> Translator.of().toPathling(structuredQuery)).getMessage();

      assertEquals(
          "Failed to expand the concept ContextualConcept[context=TermCode[system=context, code=context, display=context], concept=Concept[termCodes=[TermCode[system=http://fhir.de/CodeSystem/bfarm/icd-10-gm, code=C71, display=Malignant neoplasm of brain]]]].",
          message);
    }

    @Test
    void nonMappableConcept() {
      var conceptTree = TermCodeNode.of(C71, TermCodeNode.of(C71_0), TermCodeNode.of(C71_1));
      var mappingContext = MappingContext.of(Map.of(), conceptTree);

      var message = assertThrows(TranslationException.class, () -> Translator.of(mappingContext)
          .toPathling(StructuredQuery.of(
              List.of(List.of(ConceptCriterion.of(ContextualConcept.of(C71))))))).getMessage();

      assertEquals(
          "Failed to expand the concept ContextualConcept[context=TermCode[system=context, code=context, display=context], concept=Concept[termCodes=[TermCode[system=http://fhir.de/CodeSystem/bfarm/icd-10-gm, code=C71, display=Malignant neoplasm of brain]]]].",
          message);
    }

    @Test
    void usage_Documentation() throws JsonProcessingException, JSONException {
      var c71_1 = ContextualTermCode.of(CONTEXT,
          TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.1",
              "Malignant neoplasm of brain"));
      var mappings = Map.of(c71_1, Mapping.of(c71_1, "Condition", "code.coding"));
      var conceptTree = TermCodeNode.of(c71_1);
      var mappingContext = MappingContext.of(mappings, conceptTree);

      var parameters = Translator.of(mappingContext).toPathling(
          StructuredQuery.of(List.of(List.of(ConceptCriterion.of(ContextualConcept.of(c71_1))))));

      //to json

      JSONAssert.assertEquals("""
          {
            "parameter" : [ {
              "name" : "aggregation",
              "valueString" : "count()"
            }, {
              "name" : "filter",
              "valueString" : "(reverseResolve(Condition.subject).exists(code.coding.where(system = 'http://fhir.de/CodeSystem/bfarm/icd-10-gm').exists(code = 'C71.1')))"
            } ],
            "resourceType" : "Parameters"
          }""", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(parameters), JSONCompareMode.LENIENT);
    }

    @Test
    void timeRestriction() throws JsonProcessingException, JSONException {
      var c71_1 = ContextualTermCode.of(CONTEXT,
          TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.1",
              "Malignant neoplasm of brain"));
      var mappings = Map.of(c71_1,
          Mapping.of(c71_1, "Condition", "code.coding", null, null, List.of(), List.of(), "onset"));
      var conceptTree = TermCodeNode.of(c71_1);
      var mappingContext = MappingContext.of(mappings, conceptTree);

      var parameters = Translator.of(mappingContext).toPathling(StructuredQuery.of(List.of(List.of(
          ConceptCriterion.of(ContextualConcept.of(c71_1),
              TimeRestriction.of("2020-01-01T", "2020-01-02T"))))));


      var json = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(parameters);
      System.out.println(json);

      JSONAssert.assertEquals("""
          {
            "parameter" : [ {
              "name" : "aggregation",
              "valueString" : "count()"
            }, {
              "name" : "filter",
              "valueString" : "(reverseResolve(Condition.subject).exists(code.coding.where(system = 'http://fhir.de/CodeSystem/bfarm/icd-10-gm').exists(code = 'C71.1') and\\n(onset.dateTime > @2020-01-01 and\\nonset.dateTime < @2020-01-02 or\\nonset.period.start > @2020-01-01 and\\nonset.period.start < @2020-01-02 or\\nonset.period.end > @2020-01-01 and\\nonset.period.end < @2020-01-02)))"
            } ],
            "resourceType" : "Parameters"
          }""", json, JSONCompareMode.LENIENT);
    }

    @Test
    void timeRestriction_missingPathInMapping() {
      var c71_1 = ContextualTermCode.of(CONTEXT,
          TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.1",
              "Malignant neoplasm of brain"));
      var mappings = Map.of(c71_1,
          Mapping.of(c71_1, "Condition", "code.coding", null, null, List.of(), List.of(), null));
      var conceptTree = TermCodeNode.of(c71_1);
      var mappingContext = MappingContext.of(mappings, conceptTree);
      var query = StructuredQuery.of(List.of(List.of(ConceptCriterion.of(ContextualConcept.of(c71_1),
          TimeRestriction.of("2020-01-01T", "2020-01-02T")))));
      var translator = Translator.of(mappingContext);

      assertThatIllegalStateException().isThrownBy(() -> translator.toPathling(query)).withMessage(
          "Missing timeRestrictionPath in mapping with key ContextualTermCode[context=TermCode[system=context, code=context, display=context], termCode=TermCode[system=http://fhir.de/CodeSystem/bfarm/icd-10-gm, code=C71.1, display=Malignant neoplasm of brain]].");
    }

    @Test
    void test_Task1() throws JsonProcessingException, JSONException {
      var mappings = Map.of(PLATELETS, Mapping.of(PLATELETS, "Observation", "code.coding", "value", "Quantity"), C71_0,
          Mapping.of(C71_0, "Condition", "code.coding", null, null, List.of(),
              List.of(VERIFICATION_STATUS_ATTR_MAPPING)), C71_1,
          Mapping.of(C71_1, "Condition", "code.coding", null, null, List.of(),
              List.of(VERIFICATION_STATUS_ATTR_MAPPING)), TMZ,
          Mapping.of(TMZ, "MedicationStatement"));
      var conceptTree = TermCodeNode.of(ROOT, TermCodeNode.of(TMZ),
          TermCodeNode.of(C71, TermCodeNode.of(C71_0), TermCodeNode.of(C71_1)));
      var mappingContext = MappingContext.of(mappings, conceptTree);
      var concept = ConceptCriterion.of(ContextualConcept.of(C71));
      concept.appendAttributeFilter(ValueSetAttributeFilter.of(VERIFICATION_STATUS, CONFIRMED));
      var structuredQuery = StructuredQuery.of(List.of(List.of(concept),
          List.of(
              NumericCriterion.of(ContextualConcept.of(PLATELETS), LESS_THAN, BigDecimal.valueOf(50),
                  "g/dl")), List.of(ConceptCriterion.of(ContextualConcept.of(TMZ)))));

      var parameters = Translator.of(mappingContext).toPathling(structuredQuery);

      var json = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(parameters);

      System.out.println(json);

      JSONAssert.assertEquals("""
          {
            "parameter" : [ {
              "name" : "aggregation",
              "valueString" : "count()"
            }, {
              "name" : "filter",
              "valueString" : "(reverseResolve(Condition.subject).exists(code.coding.where(system = 'http://fhir.de/CodeSystem/bfarm/icd-10-gm').exists(code = 'C71.0') and\\n(verificationStatus.coding.coding.where(system = 'http://terminology.hl7.org/CodeSystem/condition-ver-status').exists(code = 'confirmed'))) or\\nreverseResolve(Condition.subject).exists(code.coding.where(system = 'http://fhir.de/CodeSystem/bfarm/icd-10-gm').exists(code = 'C71.1') and\\n(verificationStatus.coding.coding.where(system = 'http://terminology.hl7.org/CodeSystem/condition-ver-status').exists(code = 'confirmed')))) and\\n(reverseResolve(Observation.subject).exists(code.coding.where(system = 'http://loinc.org').exists(code = '26515-7') and\\nvalue < 50 'g/dl')) and\\n(reverseResolve(MedicationStatement.subject).exists(code.where(system = 'http://fhir.de/CodeSystem/dimdi/atc').exists(code = 'L01AX03')))"
            } ],
            "resourceType" : "Parameters"
          }""", json, JSONCompareMode.LENIENT);
    }

    @Test
    void test_Task2() throws JsonProcessingException, JSONException {
      var mappings = Map.of(PLATELETS, Mapping.of(PLATELETS, "Observation", "value"), HYPERTENSION,
          Mapping.of(HYPERTENSION, "Condition", "code.coding", null, null, List.of(),
              List.of(VERIFICATION_STATUS_ATTR_MAPPING)), SERUM, Mapping.of(SERUM, "Specimen"), LIPID,
          Mapping.of(LIPID, "MedicationStatement"));
      var conceptTree = TermCodeNode.of(ROOT, TermCodeNode.of(HYPERTENSION), TermCodeNode.of(SERUM),
          TermCodeNode.of(LIPID));
      var mappingContext = MappingContext.of(mappings, conceptTree);
      var concept = ConceptCriterion.of(ContextualConcept.of(HYPERTENSION));
      concept.appendAttributeFilter(ValueSetAttributeFilter.of(VERIFICATION_STATUS, CONFIRMED));
      var structuredQuery = StructuredQuery.of(List.of(List.of(concept),
              List.of(ConceptCriterion.of(ContextualConcept.of(SERUM)))),
          List.of(List.of(ConceptCriterion.of(ContextualConcept.of(LIPID)))));

      var parameters = Translator.of(mappingContext).toPathling(structuredQuery);

      var json = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(parameters);
      System.out.println(json);
      JSONAssert.assertEquals("""
          {
            "parameter" : [ {
              "name" : "aggregation",
              "valueString" : "count()"
            }, {
              "name" : "filter",
              "valueString" : "(reverseResolve(Condition.subject).exists(code.coding.where(system = 'http://fhir.de/CodeSystem/bfarm/icd-10-gm').exists(code = 'I10') and\\n(verificationStatus.coding.coding.where(system = 'http://terminology.hl7.org/CodeSystem/condition-ver-status').exists(code = 'confirmed')))) and\\n(reverseResolve(Specimen.subject).exists(code.where(system = 'https://fhir.bbmri.de/CodeSystem/SampleMaterialType').exists(code = 'Serum')))"
            }, {
              "name" : "filter",
              "valueString" : "(reverseResolve(MedicationStatement.subject).exists(code.where(system = 'http://fhir.de/CodeSystem/dimdi/atc').exists(code = 'C10AA'))).not()"
            } ],
            "resourceType" : "Parameters"
          }""", json, JSONCompareMode.LENIENT);

    }

    @Test
    void geccoTask2() {
      var mappings = Map.of(FRAILTY_SCORE, Mapping.of(FRAILTY_SCORE, "Observation", "code.coding", "value", "Coding"), COPD,
          Mapping.of(COPD, "Condition", "code.coding", null, null,
              List.of(CodingModifier.of("verificationStatus.coding", CONFIRMED)), List.of()), G47_31,
          Mapping.of(G47_31, "Condition",  "code.coding", null, null,
              List.of(CodingModifier.of("verificationStatus.coding", CONFIRMED)), List.of()),
          TOBACCO_SMOKING_STATUS, Mapping.of(TOBACCO_SMOKING_STATUS, "Observation", "code.coding", "value.coding", "Coding"));
      var conceptTree = TermCodeNode.of(ROOT, TermCodeNode.of(COPD), TermCodeNode.of(G47_31));
      var mappingContext = MappingContext.of(mappings, conceptTree);
      var structuredQuery = StructuredQuery.of(
          List.of(List.of(ValueSetCriterion.of(ContextualConcept.of(FRAILTY_SCORE), VERY_FIT, WELL))),
          List.of(List.of(ConceptCriterion.of(ContextualConcept.of(COPD)),
              ConceptCriterion.of(ContextualConcept.of(G47_31))), List.of(
              ValueSetCriterion.of(ContextualConcept.of(TOBACCO_SMOKING_STATUS),
                  CURRENT_EVERY_DAY_SMOKER))));

      var parameters = Translator.of(mappingContext).toPathling(structuredQuery);

//      assertThat(library).printsTo("""
//                    library Retrieve version '1.0.0'
//                    using FHIR version '4.0.0'
//                    include FHIRHelpers version '4.0.0'
//
//                    codesystem frailty-score: 'https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty-score'
//                    codesystem icd10: 'http://fhir.de/CodeSystem/bfarm/icd-10-gm'
//                    codesystem loinc: 'http://loinc.org'
//                    codesystem snomed: 'http://snomed.info/sct'
//                    codesystem ver_status: 'http://terminology.hl7.org/CodeSystem/condition-ver-status'
//
//                    context Patient
//
//                    define "Criterion 1":
//                      exists (from [Observation: Code '713636003' from snomed] O
//                        where O.value.coding contains Code '1' from frailty-score or
//                          O.value.coding contains Code '2' from frailty-score)
//
//                    define Inclusion:
//                      "Criterion 1"
//
//                    define "Criterion 2":
//                      exists (from [Condition: Code '13645005' from snomed] C
//                        where C.verificationStatus.coding contains Code 'confirmed' from ver_status)
//
//                    define "Criterion 3":
//                      exists (from [Condition: Code 'G47.31' from icd10] C
//                        where C.verificationStatus.coding contains Code 'confirmed' from ver_status)
//
//                    define "Criterion 4":
//                      exists (from [Observation: Code '72166-2' from loinc] O
//                        where O.value.coding contains Code 'LA18976-3' from loinc)
//
//                    define Exclusion:
//                      "Criterion 2" and
//                      "Criterion 3" or
//                      "Criterion 4"
//
//                    define InInitialPopulation:
//                      Inclusion and
//                      not Exclusion
//                    """);
    }

    @Test
    void onlyFixedCriteria() throws Exception {
      var mapping = readMapping("""
                    {
                        "resourceType": "Consent",
                        "fixedCriteria": [
                            {
                                "fhirPath": "status",
                                "searchParameter": "status",
                                "type": "code",
                                "value": [
                                    {
                                        "code": "active",
                                        "display": "Active",
                                        "system": "http://hl7.org/fhir/consent-state-codes"
                                    }
                                ]
                            },
                            {
                                "fhirPath": "provision.provision.code.coding",
                                "searchParameter": "mii-provision-provision-code",
                                "type": "coding",
                                "value": [
                                    {
                                        "code": "2.16.840.1.113883.3.1937.777.24.5.3.5",
                                        "display": "IDAT bereitstellen EU DSGVO NIVEAU",
                                        "system": "urn:oid:2.16.840.1.113883.3.1937.777.24.5.3"
                                    }
                                ]
                            },
                            {
                                "fhirPath": "provision.provision.code.coding",
                                "searchParameter": "mii-provision-provision-code",
                                "type": "coding",
                                "value": [
                                    {
                                        "code": "2.16.840.1.113883.3.1937.777.24.5.3.2",
                                        "display": "IDAT erheben",
                                        "system": "urn:oid:2.16.840.1.113883.3.1937.777.24.5.3"
                                    }
                                ]
                            }
                        ],
                        "context": {
                            "code": "context",
                            "display": "context",
                            "system": "context"
                        },
                        "key": {
                            "code": "combined-consent",
                            "display": "Einwilligung f\\u00fcr die zentrale Datenanalyse",
                            "system": "mii.abide"
                        },
                        "termCode": {
                            "code": "54133-1",
                            "display": "Consent Document",
                            "system": "http://loinc.org"
                        },
                        "termCodeFhirPath": "category",
                        "timeRestrictionParameter": "date",
                        "timeRestrictionFhirPath": "dateTime"
                    }
                    """);

      var structuredQuery = readStructuredQuery("""
                    {
                      "version": "https://medizininformatik-initiative.de/fdpg/StructuredQuery/v3/schema",
                      "display": "",
                      "inclusionCriteria": [
                        [
                          {
                            "context": {
                                "code": "context",
                                "display": "context",
                                "system": "context"
                            },
                            "termCodes": [
                              {
                                "code": "combined-consent",
                                "system": "mii.abide",
                                "display": "Einwilligung für die zentrale Datenanalyse"
                              }
                            ]
                          }
                        ]
                      ]
                    }
                    """);

      var conceptTree = TermCodeNode.of(ROOT, TermCodeNode.of(COMBINED_CONSENT));
      var mappings = Map.of(COMBINED_CONSENT, mapping);
      var mappingContext = MappingContext.of(mappings, conceptTree);

      var parameters = Translator.of(mappingContext).toPathling(structuredQuery);

//      assertThat(library).printsTo("""
//                    library Retrieve version '1.0.0'
//                    using FHIR version '4.0.0'
//                    include FHIRHelpers version '4.0.0'
//
//                    codesystem consent: 'urn:oid:2.16.840.1.113883.3.1937.777.24.5.3'
//                    codesystem loinc: 'http://loinc.org'
//
//                    context Patient
//
//                    define Criterion:
//                      exists (from [Consent: Code '54133-1' from loinc] C
//                        where C.status = 'active' and
//                          C.provision.provision.code.coding contains Code '2.16.840.1.113883.3.1937.777.24.5.3.5' from consent and
//                          C.provision.provision.code.coding contains Code '2.16.840.1.113883.3.1937.777.24.5.3.2' from consent)
//
//                    define InInitialPopulation:
//                      Criterion
//                    """);
    }

    @Test
    void numericAgeTranslation() throws Exception {
      var mapping = readMapping("""
                    {
                        "resourceType": "Patient",
                        "context": {
                            "code": "context",
                            "display": "context",
                            "system": "context"
                        },
                        "key": {
                            "code": "424144002",
                            "display": "Current chronological age",
                            "system": "http://snomed.info/sct"
                        },
                        "valueFhirPath": "birthDate",
                        "valueSearchParameter": "birthDate",
                        "valueType": "Age"
                    }
                    """);

      var structuredQuery = readStructuredQuery("""
                    {
                      "version": "https://medizininformatik-initiative.de/fdpg/StructuredQuery/v3/schema",
                      "display": "",
                      "inclusionCriteria": [
                        [
                          {
                            "context": {
                                "code": "context",
                                "display": "context",
                                "system": "context"
                            },
                            "termCodes": [
                              {
                                "code": "424144002",
                                "system": "http://snomed.info/sct",
                                "display": "Current chronological age"
                              }
                            ],
                            "valueFilter": {
                              "selectedConcepts": [],
                              "type": "quantity-comparator",
                              "unit": {
                                "code": "a",
                                "display": "a"
                              },
                              "value": 5,
                              "comparator": "gt"
                            }
                          }
                        ]
                      ]
                    }
                    """);
      var conceptTree = TermCodeNode.of(ROOT, TermCodeNode.of(AGE));
      var mappings = Map.of(AGE, mapping);
      var mappingContext = MappingContext.of(mappings, conceptTree);

      var parameters = Translator.of(mappingContext).toPathling(structuredQuery);

//      assertThat(library).printsTo("""
//                    library Retrieve version '1.0.0'
//                    using FHIR version '4.0.0'
//                    include FHIRHelpers version '4.0.0'
//
//                    context Patient
//
//                    define Criterion:
//                      AgeInYears() > 5
//
//                    define InInitialPopulation:
//                      Criterion
//                    """);
    }

    @Test
    void ageRangeTranslation() throws Exception {
      var mapping = readMapping("""
                    {
                        "resourceType": "Patient",
                        "context": {
                            "code": "context",
                            "display": "context",
                            "system": "context"
                        },
                        "key": {
                            "code": "424144002",
                            "display": "Current chronological age",
                            "system": "http://snomed.info/sct"
                        },
                        "valueFhirPath": "birthDate",
                        "valueSearchParameter": "birthDate",
                        "valueType": "Age"
                    }
                    """);

      var structuredQuery = readStructuredQuery("""
                    {
                      "version": "https://medizininformatik-initiative.de/fdpg/StructuredQuery/v3/schema",
                      "display": "",
                      "inclusionCriteria": [
                        [
                          {
                            "context": {
                                "code": "context",
                                "display": "context",
                                "system": "context"
                            },
                            "termCodes": [
                              {
                                "code": "424144002",
                                "system": "http://snomed.info/sct",
                                "display": "Current chronological age"
                              }
                            ],
                            "valueFilter": {
                              "selectedConcepts": [],
                              "type": "quantity-range",
                              "unit": {
                                "code": "a",
                                "display": "a"
                              },
                              "minValue": 5,
                              "maxValue": 10
                            }
                          }
                        ]
                      ]
                    }
                    """);
      var conceptTree = TermCodeNode.of(ROOT, TermCodeNode.of(AGE));
      var mappings = Map.of(AGE, mapping);
      var mappingContext = MappingContext.of(mappings, conceptTree);

      var parameters = Translator.of(mappingContext).toPathling(structuredQuery);

//      assertThat(library).printsTo("""
//                    library Retrieve version '1.0.0'
//                    using FHIR version '4.0.0'
//                    include FHIRHelpers version '4.0.0'
//
//                    context Patient
//
//                    define Criterion:
//                      AgeInYears() between 5 and 10
//
//                    define InInitialPopulation:
//                      Criterion
//                    """);
    }

    @Test
    void numericAgeTranslationInHours() throws Exception {
      var mapping = readMapping("""
                    {
                        "resourceType": "Patient",
                        "context": {
                            "code": "context",
                            "display": "context",
                            "system": "context"
                        },
                        "key": {
                            "code": "424144002",
                            "display": "Current chronological age",
                            "system": "http://snomed.info/sct"
                        },
                        "valueFhirPath": "birthDate",
                        "valueSearchParameter": "birthDate",
                        "valueType": "Age"
                    }
                    """);

      var structuredQuery = readStructuredQuery("""
                    {
                      "version": "https://medizininformatik-initiative.de/fdpg/StructuredQuery/v3/schema",
                      "display": "",
                      "inclusionCriteria": [
                        [
                          {
                            "context": {
                                "code": "context",
                                "display": "context",
                                "system": "context"
                            },
                            "termCodes": [
                              {
                                "code": "424144002",
                                "system": "http://snomed.info/sct",
                                "display": "Current chronological age"
                              }
                            ],
                            "valueFilter": {
                              "selectedConcepts": [],
                              "type": "quantity-comparator",
                              "unit": {
                                "code": "mo",
                                "display": "mo"
                              },
                              "value": 5,
                              "comparator": "lt"
                            }
                          }
                        ]
                      ]
                    }
                    """);
      var conceptTree = TermCodeNode.of(ROOT, TermCodeNode.of(AGE));
      var mappings = Map.of(AGE, mapping);
      var mappingContext = MappingContext.of(mappings, conceptTree);

      var parameters = Translator.of(mappingContext).toPathling(structuredQuery);

//      assertThat(library).printsTo("""
//                    library Retrieve version '1.0.0'
//                    using FHIR version '4.0.0'
//                    include FHIRHelpers version '4.0.0'
//
//                    context Patient
//
//                    define Criterion:
//                      AgeInHours() < 5
//
//                    define InInitialPopulation:
//                      Criterion
//                    """);
    }

    @Test
    void patientGender() throws Exception {
      var mapping = readMapping("""
                    {
                        "resourceType": "Patient",
                        "context": {
                            "code": "context",
                            "display": "context",
                            "system": "context"
                        },
                        "key": {
                            "code": "263495000",
                            "display": "Geschlecht",
                            "system": "http://snomed.info/sct"
                        },
                        "valueFhirPath": "gender",
                        "valueSearchParameter": "gender",
                        "valueType": "code",
                        "valueTypeFhir": "code"
                    }
                    """);
      var structuredQuery = readStructuredQuery("""
                    {
                      "version": "https://medizininformatik-initiative.de/fdpg/StructuredQuery/v3/schema",
                      "display": "",
                      "inclusionCriteria": [
                        [
                          {
                            "context": {
                                "code": "context",
                                "display": "context",
                                "system": "context"
                            },
                            "termCodes": [
                              {
                                "code": "263495000",
                                "display": "Geschlecht",
                                "system": "http://snomed.info/sct"
                              }
                            ],
                            "valueFilter": {
                              "type": "concept",
                              "selectedConcepts": [
                                {
                                  "code": "female",
                                  "system": "http://hl7.org/fhir/administrative-gender",
                                  "display": "Female"
                                }
                              ]
                            }
                          }
                        ]
                      ]
                    }
                    """);
      var conceptTree = TermCodeNode.of(ROOT, TermCodeNode.of(GENDER));
      var mappings = Map.of(GENDER, mapping);
      var mappingContext = MappingContext.of(mappings, conceptTree);

      var parameters = Translator.of(mappingContext).toPathling(structuredQuery);

//      assertThat(library).printsTo("""
//                    library Retrieve version '1.0.0'
//                    using FHIR version '4.0.0'
//                    include FHIRHelpers version '4.0.0'
//
//                    context Patient
//
//                    define Criterion:
//                      Patient.gender = 'female'
//
//                    define InInitialPopulation:
//                      Criterion
//                    """);
    }

    @Test
    void consent() throws Exception {
      var mapping = readMapping("""
                    {
                        "context": {
                            "code": "Einwilligung",
                            "display": "Einwilligung",
                            "system": "fdpg.mii.cds",
                            "version": "1.0.0"
                        },
                        "key": {
                            "code": "2.16.840.1.113883.3.1937.777.24.5.3.8",
                            "display": "MDAT wissenschaftlich nutzen EU DSGVO NIVEAU",
                            "system": "urn:oid:2.16.840.1.113883.3.1937.777.24.5.3",
                            "version": "1.0.2"
                        },
                        "name": "Einwilligung",
                        "resourceType": "Consent",
                        "termCodeFhirPath": "provision.provision.code"
                    }
                    """);

      var structuredQuery = readStructuredQuery("""
                    {
                      "version": "http://to_be_decided.com/draft-1/schema#",
                      "display": "",
                      "inclusionCriteria": [
                        [
                          {
                            "context": {
                              "code": "Einwilligung",
                              "display": "Einwilligung",
                              "system": "fdpg.mii.cds",
                              "version": "1.0.0"
                            },
                            "termCodes": [
                              {
                                "code": "2.16.840.1.113883.3.1937.777.24.5.3.8",
                                "display": "MDAT wissenschaftlich nutzen EU DSGVO NIVEAU",
                                "system": "urn:oid:2.16.840.1.113883.3.1937.777.24.5.3"
                              }
                            ]
                          }
                        ]
                      ]
                    }
                    """);
      var conceptTree = TermCodeNode.of(ROOT, TermCodeNode.of(CONSENT_MDAT));
      var mappings = Map.of(CONSENT_MDAT, mapping);
      var mappingContext = MappingContext.of(mappings, conceptTree);

      var parameters = Translator.of(mappingContext).toPathling(structuredQuery);

//      assertThat(library).printsTo("""
//                    library Retrieve version '1.0.0'
//                    using FHIR version '4.0.0'
//                    include FHIRHelpers version '4.0.0'
//
//                    codesystem consent: 'urn:oid:2.16.840.1.113883.3.1937.777.24.5.3'
//
//                    context Patient
//
//                    define Criterion:
//                      exists (from [Consent] C
//                        where C.provision.provision.code.coding contains Code '2.16.840.1.113883.3.1937.777.24.5.3.8' from consent)
//
//                    define InInitialPopulation:
//                      Criterion
//                    """);
    }

    @Nested
    class Inclusion {

      @Test
      void oneDisjunctionWithOneCriterion() {
        var structuredQuery = StructuredQuery.of(List.of(List.of(Criterion.TRUE)));

        var parameters = Translator.of().toPathling(structuredQuery);

//        assertThat(library).patientContextPrintsTo("""
//                        context Patient
//
//                        define Criterion:
//                          true
//
//                        define InInitialPopulation:
//                          Criterion
//                        """);
      }

      @Test
      void oneDisjunctionWithTwoCriteria() {
        var structuredQuery = StructuredQuery.of(List.of(List.of(Criterion.TRUE, Criterion.FALSE)));

        var parameters = Translator.of().toPathling(structuredQuery);

//        assertThat(library).patientContextPrintsTo("""
//                        context Patient
//
//                        define "Criterion 1":
//                          true
//
//                        define "Criterion 2":
//                          false
//
//                        define InInitialPopulation:
//                          "Criterion 1" or
//                          "Criterion 2"
//                        """);
      }

      @Test
      void twoDisjunctionsWithOneCriterionEach() {
        var structuredQuery = StructuredQuery.of(List.of(List.of(Criterion.TRUE), List.of(Criterion.FALSE)));

        var parameters = Translator.of().toPathling(structuredQuery);

//        assertThat(library).patientContextPrintsTo("""
//                        context Patient
//
//                        define "Criterion 1":
//                          true
//
//                        define "Criterion 2":
//                          false
//
//                        define InInitialPopulation:
//                          "Criterion 1" and
//                          "Criterion 2"
//                        """);
      }

      @Test
      void twoDisjunctionsWithTwoCriterionEach() {
        var structuredQuery = StructuredQuery.of(List.of(List.of(Criterion.TRUE, Criterion.TRUE),
            List.of(Criterion.FALSE, Criterion.FALSE)));

        var parameters = Translator.of().toPathling(structuredQuery);

//        assertThat(library).patientContextPrintsTo("""
//                        context Patient
//
//                        define "Criterion 1":
//                          true
//
//                        define "Criterion 2":
//                          true
//
//                        define "Criterion 3":
//                          false
//
//                        define "Criterion 4":
//                          false
//
//                        define InInitialPopulation:
//                          ("Criterion 1" or
//                          "Criterion 2") and
//                          ("Criterion 3" or
//                          "Criterion 4")
//                        """);
      }
    }

    @Nested
    class InclusionAndExclusion {

      @Test
      void oneConjunctionWithOneCriterion() {
        var structuredQuery = StructuredQuery.of(List.of(List.of(Criterion.TRUE)), List.of(List.of(Criterion.FALSE)));

        var parameters = Translator.of().toPathling(structuredQuery);

//        assertThat(library).patientContextPrintsTo("""
//                        context Patient
//
//                        define "Criterion 1":
//                          true
//
//                        define Inclusion:
//                          "Criterion 1"
//
//                        define "Criterion 2":
//                          false
//
//                        define Exclusion:
//                          "Criterion 2"
//
//                        define InInitialPopulation:
//                          Inclusion and
//                          not Exclusion
//                        """);
      }

      @Test
      void oneConjunctionWithTwoCriteria() {
        var structuredQuery = StructuredQuery.of(List.of(List.of(Criterion.TRUE)),
            List.of(List.of(Criterion.FALSE, Criterion.FALSE)));

        var parameters = Translator.of().toPathling(structuredQuery);

//        assertThat(library).patientContextPrintsTo("""
//                        context Patient
//
//                        define "Criterion 1":
//                          true
//
//                        define Inclusion:
//                          "Criterion 1"
//
//                        define "Criterion 2":
//                          false
//
//                        define "Criterion 3":
//                          false
//
//                        define Exclusion:
//                          "Criterion 2" and
//                          "Criterion 3"
//
//                        define InInitialPopulation:
//                          Inclusion and
//                          not Exclusion
//                        """);
      }

      @Test
      void twoConjunctionsWithOneCriterionEach() {
        var structuredQuery = StructuredQuery.of(List.of(List.of(Criterion.TRUE)),
            List.of(List.of(Criterion.TRUE), List.of(Criterion.FALSE)));


        var parameters = Translator.of().toPathling(structuredQuery);

//        assertThat(library).patientContextPrintsTo("""
//                        context Patient
//
//                        define "Criterion 1":
//                          true
//
//                        define Inclusion:
//                          "Criterion 1"
//
//                        define "Criterion 2":
//                          true
//
//                        define "Criterion 3":
//                          false
//
//                        define Exclusion:
//                          "Criterion 2" or
//                          "Criterion 3"
//
//                        define InInitialPopulation:
//                          Inclusion and
//                          not Exclusion
//                        """);
      }

      @Test
      void twoConjunctionsWithTwoCriterionEach() {
        var structuredQuery = StructuredQuery.of(List.of(List.of(Criterion.TRUE)),
            List.of(List.of(Criterion.FALSE, Criterion.FALSE),
                List.of(Criterion.FALSE, Criterion.FALSE)));

        var parameters = Translator.of().toPathling(structuredQuery);

//        assertThat(library).patientContextPrintsTo("""
//                        context Patient
//
//                        define "Criterion 1":
//                          true
//
//                        define Inclusion:
//                          "Criterion 1"
//
//                        define "Criterion 2":
//                          false
//
//                        define "Criterion 3":
//                          false
//
//                        define "Criterion 4":
//                          false
//
//                        define "Criterion 5":
//                          false
//
//                        define Exclusion:
//                          "Criterion 2" and
//                          "Criterion 3" or
//                          "Criterion 4" and
//                          "Criterion 5"
//
//                        define InInitialPopulation:
//                          Inclusion and
//                          not Exclusion
//                        """);
      }

      @Test
      void twoInclusionAndTwoExclusionCriteria() {
        var structuredQuery = StructuredQuery.of(List.of(List.of(Criterion.TRUE), List.of(Criterion.FALSE)),
            List.of(List.of(Criterion.TRUE, Criterion.FALSE)));

        var parameters = Translator.of().toPathling(structuredQuery);

//        assertThat(library).patientContextPrintsTo("""
//                        context Patient
//
//                        define "Criterion 1":
//                          true
//
//                        define "Criterion 2":
//                          false
//
//                        define Inclusion:
//                          "Criterion 1" and
//                          "Criterion 2"
//
//                        define "Criterion 3":
//                          true
//
//                        define "Criterion 4":
//                          false
//
//                        define Exclusion:
//                          "Criterion 3" and
//                          "Criterion 4"
//
//                        define InInitialPopulation:
//                          Inclusion and
//                          not Exclusion
//                        """);
      }
    }
  }
}
