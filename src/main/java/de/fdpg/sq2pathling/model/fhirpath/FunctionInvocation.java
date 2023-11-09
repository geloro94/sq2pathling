package de.fdpg.sq2pathling.model.fhirpath;

import static java.util.Objects.requireNonNull;

import de.fdpg.sq2pathling.PrintContext;
import java.util.List;

/**
 * @author Lorenz
 */
public record FunctionInvocation(String identifier, List<Expression> paramList) implements
    Invocation, BooleanExpression {

  public FunctionInvocation {
    requireNonNull(identifier);
    List.copyOf(paramList);
  }

  public static FunctionInvocation of(String identifier) {
    return new FunctionInvocation(identifier, List.of());
  }

  public static FunctionInvocation of(String identifier, List<Expression> paramList) {
    return new FunctionInvocation(identifier, paramList);
  }

  // Returns true if the input collection is empty, and false otherwise.
  public static FunctionInvocation empty() {
    return new FunctionInvocation("empty", List.of());
  }

  // Tests whether the input collection is empty. When invoked without arguments,
  // exists() is equivalent to empty().not().
  public static FunctionInvocation exists() {
    return new FunctionInvocation("exists", List.of());
  }

  // Returns a collection containing only the first item in the input collection.
  // This function will return an empty collection if the input collection has no items.
  public static FunctionInvocation first() {
    return new FunctionInvocation("first", List.of());
  }

  // Returns true if the input collection evaluates to false, and false if it evaluates to true.
  // Otherwise, the result is empty ({ }).
  public static FunctionInvocation not() {
    return new FunctionInvocation("not", List.of());
  }

  // Takes a collection of Boolean values and returns true if all the items are false.
  // If any items are true, the result is false. If the input is empty ({ }), the result is true.
  public static FunctionInvocation allFalse() {
    return new FunctionInvocation("allFalse", List.of());
  }

  //Takes a collection of Boolean values and returns true if all the items are true.
  // If any items are false, the result is false. If the input is empty ({ }), the result is true.
  public static FunctionInvocation allTrue() {
    return new FunctionInvocation("allTrue", List.of());
  }

  // Takes a collection of Boolean values and returns true if any of the items are false.
  // If all the items are true, or if the input is empty ({ }), the result is false.
  public static FunctionInvocation anyFalse() {
    return new FunctionInvocation("anyFalse", List.of());
  }

  // Takes a collection of Boolean values and returns true if any of the items are true.
  // If all the items are false, or if the input is empty ({ }), the result is false.
  public static FunctionInvocation anyTrue() {
    return new FunctionInvocation("anyTrue", List.of());
  }

  // Returns the Integer count of the number of items in the input collection.
  public static FunctionInvocation count() {
    return new FunctionInvocation("count", List.of());
  }

  @Override
  public String print(PrintContext printContext) {
    if (paramList.isEmpty()) {
      return "%s()".formatted(identifier);
    }
    return "%s(%s)".formatted(identifier, String.join(", ",
        paramList.stream().map(expression -> expression.print(printContext)).toList()));
  }

}
