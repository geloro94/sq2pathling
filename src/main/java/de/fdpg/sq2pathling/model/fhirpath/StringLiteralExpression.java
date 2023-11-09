package de.fdpg.sq2pathling.model.fhirpath;

import de.fdpg.sq2pathling.PrintContext;
import java.util.Objects;

public record StringLiteralExpression(String value) implements Expression {

  public StringLiteralExpression(String value) {
    Objects.requireNonNull(value, "String value cannot be null");
    this.value = escapeStringForFhirPath(value);
  }

  private static String escapeStringForFhirPath(String value) {
    return value.replace("'", "\\'");
  }

  public static Expression of(String value) {
    return new StringLiteralExpression(value);
  }

  @Override
  public String print(PrintContext printContext) {
    return "'" + value + "'";
  }
}