package de.fdpg.sq2pathling.model.structured_query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.common.TermCode;
import de.fdpg.sq2pathling.model.fhirpath.BooleanExpression;
import de.fdpg.sq2pathling.model.fhirpath.IdentifierExpression;
import java.util.stream.Stream;

/**
 * @author Alexander Kiel
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public interface Modifier {

    @JsonCreator
    static Modifier create(@JsonProperty("type") String type,
                           @JsonProperty("fhirPath") String path,
                           @JsonProperty("value") JsonNode... values) {
        if (values == null) {
            throw new IllegalArgumentException("missing modifier values");
        }

        if (values.length == 0) {
            throw new IllegalArgumentException("empty modifier values");
        }

        if ("code".equals(type)) {
            return CodeModifier.of(path, Stream.of(values).map(TermCode::fromJsonNode).map(TermCode::code).toArray(String[]::new));
        }
        if ("coding".equals(type)) {
            return CodingModifier.of(path, Stream.of(values).map(TermCode::fromJsonNode).toArray(TermCode[]::new));
        }
        throw new IllegalArgumentException("unknown type: " + type);
    }

    BooleanExpression expression(MappingContext mappingContext);
}
