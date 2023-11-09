package de.fdpg.sq2pathling.model.structured_query;

import de.fdpg.sq2pathling.model.common.TermCode;

/**
 * @author Alexander Kiel
 */
public class MappingNotFoundException extends TranslationException {

    private final ContextualTermCode contextualTermCode;

    public MappingNotFoundException(ContextualTermCode contextualTermCode) {
        super("Mapping for concept with system `%s`, code `%s` and display `%s` and context with system `%s`, code `%s` and display `%s` not found".formatted(
            contextualTermCode.termCode().system(), contextualTermCode.termCode().code(), contextualTermCode.termCode().display(),
            contextualTermCode.context().system(), contextualTermCode.context().code(), contextualTermCode.context().display()));
        this.contextualTermCode = contextualTermCode;
    }

    public ContextualTermCode getTermCode() {
        return contextualTermCode;
    }
}
