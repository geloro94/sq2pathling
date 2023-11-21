package de.fdpg.sq2pathling.model.structured_query;

import de.fdpg.sq2pathling.model.common.Comparator;
import de.fdpg.sq2pathling.model.common.TermCode;
import de.fdpg.sq2pathling.model.fhirpath.AndExpression;
import de.fdpg.sq2pathling.model.fhirpath.BooleanExpression;
import de.fdpg.sq2pathling.model.fhirpath.ComparatorExpression;
import de.fdpg.sq2pathling.model.fhirpath.MemberInvocation;
import de.fdpg.sq2pathling.model.fhirpath.DateTimeLiteralExpression;
import java.time.LocalDate;

enum AgeUnit {
  a, mo, wk
}

public class AgeUtils {

  public static final TermCode AGE = TermCode.of("http://snomed.info/sct", "424144002", "Current chronological age");


  private static LocalDate getBirthDate(LocalDate today, int age, AgeUnit unit) {
    return switch (unit) {
      case a -> today.minusYears(age);
      case mo -> today.minusMonths(age);
      case wk -> today.minusWeeks(age);
    };
  }

  public static BooleanExpression translateAge(String path, Comparator comparator,
      int age, AgeUnit unit) {
    var today = LocalDate.now();
    var birthDate = getBirthDate(today, age, unit);
    return switch (comparator) {
      case EQUAL -> ComparatorExpression.of(MemberInvocation.of(path), Comparator.EQUAL,
          DateTimeLiteralExpression.of(birthDate.toString()));
      case GREATER_THAN -> ComparatorExpression.of(MemberInvocation.of(path), Comparator.LESS_EQUAL,
          DateTimeLiteralExpression.of(birthDate.toString()));
      case GREATER_EQUAL -> ComparatorExpression.of(MemberInvocation.of(path), Comparator.LESS_THAN,
          DateTimeLiteralExpression.of(birthDate.toString()));
      case LESS_THAN -> ComparatorExpression.of(MemberInvocation.of(path), Comparator.GREATER_EQUAL,
          DateTimeLiteralExpression.of(birthDate.toString()));
      case LESS_EQUAL -> ComparatorExpression.of(MemberInvocation.of(path), Comparator.GREATER_THAN,
          DateTimeLiteralExpression.of(birthDate.toString()));
    };
  }

  public static BooleanExpression translateAgeFromRange(String path,
      int ageLowerBound, int ageUpperBound, AgeUnit unit) {
    var today = LocalDate.now();
    var birthDateLowerBound = getBirthDate(today, ageUpperBound, unit);
    var birthDateUpperBound = getBirthDate(today, ageLowerBound, unit);
    return AndExpression.of(
        ComparatorExpression.of(MemberInvocation.of(path), Comparator.GREATER_EQUAL,
            DateTimeLiteralExpression.of(birthDateLowerBound.toString())
        ), ComparatorExpression.of(MemberInvocation.of(path), Comparator.LESS_EQUAL,
                DateTimeLiteralExpression.of(birthDateUpperBound.toString())));
  }
}
