package de.fdpg.sq2pathling.model.structured_query;

import static de.fdpg.sq2pathling.model.common.Comparator.EQUAL;

import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.fhirpath.BooleanExpression;
import de.fdpg.sq2pathling.model.fhirpath.ComparatorExpression;
import de.fdpg.sq2pathling.model.fhirpath.FunctionInvocation;
import de.fdpg.sq2pathling.model.fhirpath.IdentifierExpression;
import de.fdpg.sq2pathling.model.fhirpath.OrExpression;
import de.fdpg.sq2pathling.model.fhirpath.StringLiteralExpression;
import java.util.List;
import java.util.Objects;

public final class CodeModifier extends AbstractModifier {

  private final List<String> codes;

  private CodeModifier(String path, List<String> codes) {
    super(path);
    this.codes = codes;
  }

  public static CodeModifier of(String path, String... codes) {
    return new CodeModifier(path, codes == null ? List.of() : List.of(codes));
  }

  public BooleanExpression expression(MappingContext mappingContext) {

    if (codes.size() == 1) {
      return ComparatorExpression.of(
          IdentifierExpression.of(path), EQUAL,
          StringLiteralExpression.of("%s".formatted(codes.get(0))));
    } else {
      return codes.stream().map(c ->
              (BooleanExpression) ComparatorExpression.of(
                  IdentifierExpression.of(path), EQUAL, StringLiteralExpression.of("%s".formatted(c))))
          .reduce(BooleanExpression.FALSE,
              OrExpression::of);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CodeModifier that = (CodeModifier) o;
    return path.equals(that.path) && codes.equals(that.codes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, codes);
  }
}
