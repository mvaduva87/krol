package ro.mv.krol.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import ro.mv.krol.util.Args;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by mihai on 8/19/16.
 */
@JsonDeserialize(builder = Link.Builder.class)
public class Link {

    @JsonProperty
    private final String url;

    @JsonProperty
    private final Map<String, String> metadata;

    protected Link(String url, Map<String, String> metadata) {
        this.url = Args.notEmpty(url, "url");
        this.metadata = metadata;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder
    public static class Builder {

        private String url;
        private Map<String, String> metadata;

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder withMetadata(Map<String, String> metadata) {
            if (this.metadata != null) {
                this.metadata.putAll(metadata);
            } else {
                this.metadata = metadata;
            }
            return this;
        }

        public Builder putMetadata(String key, String value) {
            if (this.metadata == null) {
                this.metadata = new LinkedHashMap<>();
            }
            this.metadata.put(key, value);
            return this;
        }

        public Link build() {
            return new Link(url, metadata);
        }
    }
}
