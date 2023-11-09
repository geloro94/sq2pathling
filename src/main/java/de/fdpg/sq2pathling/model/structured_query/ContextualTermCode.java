package de.fdpg.sq2pathling.model.structured_query;


import de.fdpg.sq2pathling.model.common.TermCode;

public record ContextualTermCode(TermCode context, TermCode termCode) {

  public static ContextualTermCode of(TermCode context, TermCode termCode) {
    return new ContextualTermCode(context, termCode);
  }
}