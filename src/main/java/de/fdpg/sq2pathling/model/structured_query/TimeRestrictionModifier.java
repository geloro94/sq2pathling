package de.fdpg.sq2pathling.model.structured_query;

import static de.fdpg.sq2pathling.model.common.Comparator.GREATER_THAN;
import static de.fdpg.sq2pathling.model.common.Comparator.LESS_THAN;

import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.fhirpath.AndExpression;
import de.fdpg.sq2pathling.model.fhirpath.BooleanExpression;
import de.fdpg.sq2pathling.model.fhirpath.ComparatorExpression;
import de.fdpg.sq2pathling.model.fhirpath.DateTimeLiteralExpression;
import de.fdpg.sq2pathling.model.fhirpath.IdentifierExpression;
import de.fdpg.sq2pathling.model.fhirpath.InvocationExpression;
import de.fdpg.sq2pathling.model.fhirpath.MemberInvocation;
import de.fdpg.sq2pathling.model.fhirpath.OrExpression;


public final class TimeRestrictionModifier extends AbstractModifier {

  private final String beforeDate;
  private final String afterDate;

  private TimeRestrictionModifier(String path, String afterDate, String beforeDate) {
    super(path);
    this.beforeDate = beforeDate;
    this.afterDate = afterDate;
  }

  public static TimeRestrictionModifier of(String path, String afterDate, String beforeDate) {
    return new TimeRestrictionModifier(path, afterDate, beforeDate);
  }

  public BooleanExpression expression(MappingContext mappingContext) {
    if (beforeDate == null && afterDate == null) {
      return BooleanExpression.TRUE;
    } else if (afterDate == null) {
      return OrExpression.of(dateTimeBefore(), periodOverlapsInfiniteTilBefore());
    } else if (beforeDate == null) {
      return OrExpression.of(dateTimeAfter(), periodOverlapsAfterTilInfinite());
    } else {
      return OrExpression.of(dateTimeInInterval(), periodOverlaps());
    }
  }

  private BooleanExpression dateTimeBefore() {
    var identifierExpression = IdentifierExpression.of(path);
    var memberInvocation = InvocationExpression.of(identifierExpression,
        MemberInvocation.of("dateTime"));
    return ComparatorExpression.of(memberInvocation, LESS_THAN,
        DateTimeLiteralExpression.of(beforeDate));
  }

  private BooleanExpression dateTimeAfter() {
    var identifierExpression = IdentifierExpression.of(path);
    var memberInvocation = InvocationExpression.of(identifierExpression,
        MemberInvocation.of("dateTime"));
    return ComparatorExpression.of(memberInvocation, GREATER_THAN,
        DateTimeLiteralExpression.of(afterDate));
  }


  private BooleanExpression dateTimeInInterval() {
    var identifierExpression = IdentifierExpression.of(path);
    var memberInvocation = InvocationExpression.of(identifierExpression,
        MemberInvocation.of("dateTime"));
    return memberInInterval(memberInvocation);
  }


  private BooleanExpression memberInInterval(InvocationExpression memberInvocation) {
    var intervalComparison = AndExpression.of(
        ComparatorExpression.of(memberInvocation, GREATER_THAN,
            DateTimeLiteralExpression.of(afterDate)),
        ComparatorExpression.of(memberInvocation, LESS_THAN,
            DateTimeLiteralExpression.of(beforeDate)));
    return intervalComparison;
  }


  private BooleanExpression periodOverlapsInfiniteTilBefore() {
    var identifierExpression = IdentifierExpression.of(path);
    var memberInvocation = InvocationExpression.of(identifierExpression,
        MemberInvocation.of("period"));
    var startInInterval = ComparatorExpression.of(InvocationExpression.of(memberInvocation,
        MemberInvocation.of("start")), LESS_THAN,
        DateTimeLiteralExpression.of(beforeDate));
    return startInInterval;
  }

  private BooleanExpression periodOverlapsAfterTilInfinite() {
    var identifierExpression = IdentifierExpression.of(path);
    var memberInvocation = InvocationExpression.of(identifierExpression,
        MemberInvocation.of("period"));
    var endInInterval = ComparatorExpression.of(InvocationExpression.of(memberInvocation,
        MemberInvocation.of("end")), GREATER_THAN,
        DateTimeLiteralExpression.of(afterDate));
    return endInInterval;
  }

  private BooleanExpression periodOverlaps() {
    var identifierExpression = IdentifierExpression.of(path);
    var memberInvocation = InvocationExpression.of(identifierExpression,
        MemberInvocation.of("period"));
    var startInInterval = memberInInterval(InvocationExpression.of(memberInvocation,
        MemberInvocation.of("start")));
    var endInInterval = memberInInterval(InvocationExpression.of(memberInvocation,
        MemberInvocation.of("end")));
    return OrExpression.of(startInInterval, endInInterval);
  }
}
