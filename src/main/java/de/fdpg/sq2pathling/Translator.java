package de.fdpg.sq2pathling;

import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.fhirpath.AndExpression;
import de.fdpg.sq2pathling.model.fhirpath.BooleanExpression;
import de.fdpg.sq2pathling.model.fhirpath.FunctionInvocation;
import de.fdpg.sq2pathling.model.fhirpath.InvocationExpression;
import de.fdpg.sq2pathling.model.fhirpath.OrExpression;
import de.fdpg.sq2pathling.model.pathling.Parameter;
import de.fdpg.sq2pathling.model.pathling.Parameters;
import de.fdpg.sq2pathling.model.structured_query.Criterion;
import de.fdpg.sq2pathling.model.structured_query.StructuredQuery;
import de.fdpg.sq2pathling.model.structured_query.TranslationException;
import java.util.List;

/**
 * The translator from Structured Query to a Pathling Request
 * <p>
 * It needs {@code mappings} and will produce the parameters for a $aggregate function {@link
 * de.fdpg.sq2pathling.model.pathling.Parameters} by calling {@link #toPathling(StructuredQuery)
 * toPathling}.
 * <p>
 * Instances are immutable and thread-safe.
 *
 * @author Lorenz Rosenau
 */
public class Translator {

  private final MappingContext mappingContext;

  private static Parameter AGGREGATION_PARAMETER = Parameter.of("aggregation",
      FunctionInvocation.count().print(PrintContext.ZERO));

  private Translator(MappingContext mappingContext) {
    this.mappingContext = mappingContext;
  }

  /**
   * Returns a translator without any mappings.
   *
   * @return a translator without any mappings
   */
  public static Translator of() {
    return new Translator(MappingContext.of());
  }

  /**
   * Returns a translator with mappings defined in {@code mappingContext}.
   *
   * @return a translator with mappings defined in {@code mappingContext}
   */
  public static Translator of(MappingContext mappingContext) {
    return new Translator(mappingContext);
  }

  private static List<Parameter> inclusionOnlyFilters(BooleanExpression inclusionExpr) {
    return List.of(AGGREGATION_PARAMETER, Parameter.of("filter", PrintContext.ZERO.print(inclusionExpr)));
  }

  private static List<Parameter> filters(BooleanExpression inclusionExpr,
      BooleanExpression exclusionExpr) {
      return List.of(AGGREGATION_PARAMETER, Parameter.of("filter", PrintContext.ZERO.print(inclusionExpr)),
          Parameter.of("filter", PrintContext.ZERO.print(InvocationExpression.of(exclusionExpr, FunctionInvocation.not()))));
  }

  /**
   * Translates the given {@code structuredQuery} into a pathling aggregate {@link Parameters}.
   *
   * @param structuredQuery the Structured Query to translate
   * @return the translated pathling aggregate {@link Parameters}
   * @throws TranslationException if the given {@code structuredQuery} can't be translated into a
   *                              pathling aggregate {@link Parameters}
   */
  public Parameters toPathling(StructuredQuery structuredQuery) {
    BooleanExpression inclusionExpr = inclusionExpr(structuredQuery.inclusionCriteria());
    BooleanExpression exclusionExpr = exclusionExpr(structuredQuery.exclusionCriteria());

    if (inclusionExpr == null) {
      throw new IllegalStateException("Inclusion criteria lead to empty inclusion expression.");
    }

    //TODO: This should be done with exlucsionExpr instead of the content of the exclusionCriteria in the StructuredQuery
    var filters = structuredQuery.exclusionCriteria().get(0).isEmpty()
        ? inclusionOnlyFilters(inclusionExpr)
        : filters(inclusionExpr, exclusionExpr);
    return Parameters.of(filters);
  }

  /**
   * Builds the inclusion expression as conjunctive normal form (CNF) of {@code criteria}.
   *
   * @param criteria a list of lists of {@link Criterion} representing a CNF
   * @return a {@link BooleanExpression} of the boolean inclusion expression
   */
  private BooleanExpression inclusionExpr(List<List<Criterion>> criteria) {
    return criteria.stream().map(this::orExpr).reduce(BooleanExpression.TRUE, AndExpression::of);
  }

  private BooleanExpression orExpr(List<Criterion> criteria) {
    return criteria.stream().map(c -> c.toFhirPathFilter(mappingContext))
        .reduce(BooleanExpression.FALSE, OrExpression::of);
  }

  /**
   * Builds the exclusion expression as disjunctive normal form (DNF) of {@code criteria}.
   *
   * @param criteria a list of lists of {@link Criterion} representing a DNF
   * @return a {@link BooleanExpression} of the boolean exclusion expression
   */
  private BooleanExpression exclusionExpr(List<List<Criterion>> criteria) {
    return criteria.stream().map(this::andExpr).reduce(BooleanExpression.FALSE, OrExpression::of);
  }

  private BooleanExpression andExpr(List<Criterion> criteria) {
    return criteria.stream().map(c -> c.toFhirPathFilter(mappingContext))
        .reduce(BooleanExpression.TRUE, AndExpression::of);
  }
}
