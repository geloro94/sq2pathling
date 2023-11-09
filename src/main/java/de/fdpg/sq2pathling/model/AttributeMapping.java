package de.fdpg.sq2pathling.model;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import de.fdpg.sq2pathling.model.common.TermCode;

/**
 * @author Lorenz Rosenau
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AttributeMapping(String type, TermCode key, String path) {

    public AttributeMapping {
        requireNonNull(type);
        requireNonNull(key);
        requireNonNull(path);
    }

    @JsonCreator
    public static AttributeMapping of(@JsonProperty("attributeType") String type,
                                      @JsonProperty("attributeKey") JsonNode key,
                                      @JsonProperty("attributePath") String path) {
        return new AttributeMapping(type, TermCode.fromJsonNode(key), path);
    }

    public static AttributeMapping of(String type, TermCode key, String path) {
        return new AttributeMapping(type, key, path);
    }

}
