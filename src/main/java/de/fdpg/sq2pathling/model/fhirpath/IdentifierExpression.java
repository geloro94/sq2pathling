package de.fdpg.sq2pathling.model.fhirpath;

import static java.util.Objects.requireNonNull;

import de.fdpg.sq2pathling.PrintContext;
import java.util.logging.Logger;

public record IdentifierExpression(String identifier) implements Expression {

  public IdentifierExpression {
    requireNonNull(identifier, "Identifier cannot be null");
    // We allow single quotes and '.' in identifiers, to support direct expressions like:
    // extension.where(url='https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/ethnic-group').value.first()
    // But we warn about it, because it is not FHIRPath compliant. Use with caution.
    if (identifier.contains("'")) {
      Logger.getLogger(IdentifierExpression.class.getName()).warning("Identifier contains single quote, use with caution: " + identifier);
    }
    if (identifier.contains(".")) {
      Logger.getLogger(IdentifierExpression.class.getName()).warning("Identifier contains dot, use with caution: " + identifier);
    }
    if (!identifier.matches("([A-Za-z]|_)([A-Za-z0-9]|_|'|.)*")) {
      throw new IllegalArgumentException("Invalid identifier: " + identifier);
    }
  }

  public static IdentifierExpression of(String identifier) {
    return new IdentifierExpression(identifier);
  }

  @Override
  public String print(PrintContext printContext) {
    return identifier;
  }
}