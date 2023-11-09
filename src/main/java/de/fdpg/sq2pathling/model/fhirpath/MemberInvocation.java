package de.fdpg.sq2pathling.model.fhirpath;

import static java.util.Objects.requireNonNull;

import de.fdpg.sq2pathling.PrintContext;

public record MemberInvocation(String member) implements Invocation {

    public MemberInvocation {
        requireNonNull(member);
    }

    public static MemberInvocation of(String member) {
        return new MemberInvocation(member);
    }

    @Override
    public String print(PrintContext printContext) {
        return member;
    }
}
