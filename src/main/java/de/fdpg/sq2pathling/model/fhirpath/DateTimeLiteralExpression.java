package de.fdpg.sq2pathling.model.fhirpath;

import de.fdpg.sq2pathling.PrintContext;

public record DateTimeLiteralExpression(String value) implements Expression {

  private static final String DATE_REGEX =
      "([0-9]{4}(-[0-9]{2}(-[0-9]{2})?)?)";
  private static final String DATETIME_REGEX =
      "([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}(:[0-9]{2}(\\.[0-9]+)?)?(Z|([+-][0-9]{2}:[0-9]{2}))?)";

  public DateTimeLiteralExpression {
    if (value.endsWith("T")) {
      value = value.substring(0, value.length() - 1);
    }
    validateFormat(value);
    value = '@' + value;
  }

  public static DateTimeLiteralExpression Date(String dateValue) {
    validateDate(dateValue);
    return new DateTimeLiteralExpression(dateValue);
  }

  public static DateTimeLiteralExpression DateTime(String dateTimeValue) {
    validateDateTime(dateTimeValue);
    return new DateTimeLiteralExpression(dateTimeValue);
  }

  private static void validateDate(String dateValue) {
    if (!dateValue.matches(DATE_REGEX)) {
      throw new IllegalArgumentException("Invalid date format: " + dateValue);
    }
  }

  private static void validateDateTime(String dateTimeValue) {
    if (!dateTimeValue.matches(DATETIME_REGEX)) {
      throw new IllegalArgumentException("Invalid datetime format: " + dateTimeValue);
    }
  }

  public static Expression of(String afterDate) {
    return new DateTimeLiteralExpression(afterDate);
  }

  private void validateFormat(String value) {
    if (!isValidDate(value) && !isValidDateTime(value)) {
      throw new IllegalArgumentException("Invalid date/datetime format: " + value);
    }
  }

  private static boolean isValidDate(String value) {
    return value.matches(DATE_REGEX);
  }

  private static boolean isValidDateTime(String value) {
    return value.matches(DATETIME_REGEX);
  }

  @Override
  public String print(PrintContext printContext) {
    return value;
  }
}
