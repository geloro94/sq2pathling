package de.fdpg.sq2pathling.model.fhirpath;

import de.fdpg.sq2pathling.PrintContext;

/**
 * @author Lorenz
 */
public interface CollectionExpression extends Expression {

  String print(PrintContext printContext);

}
