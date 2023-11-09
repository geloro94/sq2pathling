package de.fdpg.sq2pathling.model.pathling;

import static java.util.Objects.requireNonNull;

public record Parameter(String name, String valueString) {

  public Parameter {
    requireNonNull(name);
    requireNonNull(valueString);
  }

  public static Parameter of(String aggregation, String valueString) {
    return new Parameter(aggregation, valueString);
  }
}
