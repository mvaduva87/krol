package ro.mv.krol.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import ro.mv.krol.util.Args;

import java.util.Map;

/**
 * Created by mihai.vaduva on 21/08/2016.
 */
@JsonDeserialize(builder = Resource.Builder.class)
public class Resource extends Link {

    @JsonProperty
    private final String locator;

    private Resource(String url, Map<String, String> metadata, String locator) {
        super(url, metadata);
        this.locator = Args.notEmpty(locator, "locator");
    }

    private Resource(Link link, String locator) {
        this(link.getUrl(), link.getMetadata(), locator);
    }

    public String getLocator() {
        return locator;
    }

    @JsonPOJOBuilder
    public static class Builder {

        private String url;
        private Map<String, String> metadata;
        private String locator;

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder withMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder withLocator(String locator) {
            this.locator = locator;
            return this;
        }

        public Resource build() {
            return new Resource(url, metadata, locator);
        }
    }

}
