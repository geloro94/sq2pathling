package de.fdpg.sq2pathling.model.pathling;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record Parameters(List<Parameter> parameter) {

  public Parameters {
    final String resourceType = "Parameters";
    List.copyOf(parameter);
  }


  @JsonProperty("resourceType")
  public String getResourceType() {
    return "Parameters";
  }


  public static Parameters of(List<Parameter> parameter) {
    return new Parameters(parameter);
  }
}
