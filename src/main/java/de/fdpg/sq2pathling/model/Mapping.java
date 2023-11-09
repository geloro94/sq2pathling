package de.fdpg.sq2pathling.model;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.fdpg.sq2pathling.model.common.TermCode;
import de.fdpg.sq2pathling.model.structured_query.ContextualTermCode;
import de.fdpg.sq2pathling.model.structured_query.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Alexander Kiel
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Mapping {

  private final ContextualTermCode key;
  private final String resourceType;
  private final String termCodePath;
  private final String valueFhirPath;
  private final String valueType;
  private final List<Modifier> fixedCriteria;
  private final Map<TermCode, AttributeMapping> attributeMappings;
  private final String timeRestrictionPath;

  public Mapping(ContextualTermCode key, String resourceType, String termCodePath, String valueFhirPath,
      String valueType, List<Modifier> fixedCriteria,
      List<AttributeMapping> attributeMappings, String timeRestrictionPath) {
    this.key = requireNonNull(key);
    this.resourceType = requireNonNull(resourceType);
    this.termCodePath = termCodePath;
    this.valueFhirPath = valueFhirPath;
    this.valueType = valueType;
    this.fixedCriteria = List.copyOf(fixedCriteria);
    this.attributeMappings = (attributeMappings == null ? Map.of() : attributeMappings.stream()
        .collect(Collectors.toMap(AttributeMapping::key, Function.identity())));
    this.timeRestrictionPath = timeRestrictionPath;
  }

  public static Mapping of(ContextualTermCode key, String resourceType) {
    return new Mapping(key, resourceType, "code", "value", null, List.of(), List.of(), null);
  }

  public static Mapping of(ContextualTermCode concept, String resourceType, String termCodePath) {
    return new Mapping(concept, resourceType, termCodePath, null, null, List.of(), List.of(), null);
  }

  public static Mapping of(ContextualTermCode concept, String resourceType, String termCodePath,
      String valueFhirPath) {
    return new Mapping(concept, resourceType, termCodePath, valueFhirPath, null, List.of(),
        List.of(), null);
  }

  public static Mapping of(ContextualTermCode concept, String resourceType, String termCodePath,
      String valueFhirPath, String valueType) {
    return new Mapping(concept, resourceType, termCodePath, valueFhirPath, valueType, List.of(),
        List.of(), null);
  }

  public static Mapping of(ContextualTermCode key, String resourceType, String termCodePath,
      String valueFhirPath,
      String valueType, List<Modifier> fixedCriteria, List<AttributeMapping> attributeMappings) {
    return new Mapping(key, resourceType, termCodePath,
        valueFhirPath == null ? "value" : valueFhirPath,
        valueType,
        fixedCriteria == null ? List.of() : List.copyOf(fixedCriteria),
        attributeMappings, null);
  }

  public static Mapping of(ContextualTermCode key, String resourceType, String termCodePath,
      String valueFhirPath,
      String valueType, List<Modifier> fixedCriteria, List<AttributeMapping> attributeMappings,
      String timeRestrictionPath) {
    return new Mapping(key, resourceType, termCodePath,
        valueFhirPath == null ? "value" : valueFhirPath,
        valueType,
        fixedCriteria == null ? List.of() : List.copyOf(fixedCriteria),
        attributeMappings, timeRestrictionPath);
  }

  @JsonCreator
  public static Mapping of(
      @JsonProperty("context") TermCode context,
      @JsonProperty("key") TermCode key,
      @JsonProperty("resourceType") String resourceType,
      @JsonProperty("termCodeFhirPath") String termCodeFhirPath,
      @JsonProperty("valueFhirPath") String valueFhirPath,
      @JsonProperty("valueType") String valueType,
      @JsonProperty("fixedCriteria") List<Modifier> fixedCriteria,
      @JsonProperty("attributeFhirPaths") List<AttributeMapping> attributeMappings,
      @JsonProperty("timeRestrictionFhirPath") String timeRestrictionPath) {
    var contextualTermCode = ContextualTermCode.of(context, key);
    return new Mapping(contextualTermCode,
        requireNonNull(resourceType, "missing JSON property: resourceType"),
        termCodeFhirPath,
        valueFhirPath == null ? "value" : valueFhirPath,
        valueType,
        fixedCriteria == null ? List.of() : List.copyOf(fixedCriteria),
        attributeMappings,
        timeRestrictionPath
        );
  }

  public ContextualTermCode key() {
    return key;
  }

  public String resourceType() {
    return resourceType;
  }

  public String valueFhirPath() {
    return valueFhirPath;
  }

  public String termCodePath() {
    return termCodePath;
  }

  public String valueType() {
    return valueType;
  }

  public List<Modifier> fixedCriteria() {
    return fixedCriteria;
  }

  public Map<TermCode, AttributeMapping> attributeMappings() {
    return attributeMappings;
  }

  public Optional<String> timeRestrictionPath() {
    return Optional.ofNullable(timeRestrictionPath);
  }
}
