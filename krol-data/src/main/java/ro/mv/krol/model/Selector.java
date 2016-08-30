package ro.mv.krol.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import ro.mv.krol.util.Args;

import java.util.Map;

/**
 * Created by mihai on 8/7/16.
 */
@JsonDeserialize(builder = Selector.Builder.class)
public class Selector {

    public enum Target {LINKS, RESOURCES}

    @JsonProperty(required = true)
    protected final String name;

    @JsonProperty(required = true)
    protected final String query;

    @JsonProperty
    protected final String attribute;

    @JsonProperty
    protected final String when;

    @JsonProperty
    protected final Map<String, String> metadata;

    protected Selector(String name, String query, String attribute, String when, Map<String, String> metadata) {
        this.name = Args.notEmpty(name, "name");
        this.query = Args.notEmpty(query, "query");
        this.attribute = attribute;
        this.when = when;
        this.metadata = metadata;
    }

    public String getName() {
        return name;
    }

    public String getQuery() {
        return query;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getWhen() {
        return when;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder
    public static class Builder {

        private String name;
        private String query;
        private String attribute;
        private String when;
        private Map<String, String> metadata;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withQuery(String query) {
            this.query = query;
            return this;
        }

        public Builder withAttribute(String attribute) {
            this.attribute = attribute;
            return this;
        }

        public Builder withWhen(String when) {
            this.when = when;
            return this;
        }

        public Builder withMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Selector build() {
            return new Selector(name, query, attribute, when, metadata);
        }
    }
}
