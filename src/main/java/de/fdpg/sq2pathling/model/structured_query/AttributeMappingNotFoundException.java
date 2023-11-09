package de.fdpg.sq2pathling.model.structured_query;


import de.fdpg.sq2pathling.model.common.TermCode;

public class AttributeMappingNotFoundException extends TranslationException {

  private final TermCode termCode;

  public AttributeMappingNotFoundException(TermCode termCode) {
    super("Mapping for concept with system `%s`, code `%s` and display `%s`".formatted(
        termCode.system(), termCode.code(), termCode.display()));
    this.termCode = termCode;
  }

  public TermCode getTermCode() {
    return termCode;
  }
}

