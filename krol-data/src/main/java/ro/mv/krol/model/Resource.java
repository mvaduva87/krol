package ro.mv.krol.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import ro.mv.krol.util.Args;

import java.util.Date;
import java.util.Map;

/**
 * Created by mihai.vaduva on 21/08/2016.
 */
@JsonDeserialize(builder = Resource.Builder.class)
public class Resource extends Link {

    @JsonProperty
    private final String contentType;

    @JsonProperty
    private final Date timestamp;

    @JsonProperty
    private final String locator;

    private Resource(String url, Map<String, String> metadata, String contentType, Date timestamp, String locator) {
        super(url, metadata);
        this.contentType = Args.notEmpty(contentType, "contentType");
        this.locator = Args.notEmpty(locator, "locator");
        this.timestamp = Args.notNull(timestamp, "timestamp");
    }

    public String getContentType() {
        return contentType;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getLocator() {
        return locator;
    }

    @JsonPOJOBuilder
    public static class Builder {

        private String url;
        private Map<String, String> metadata;
        private String contentType;
        private Date timestamp;
        private String locator;

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder withMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder withContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder withTimestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withLocator(String locator) {
            this.locator = locator;
            return this;
        }

        public Resource build() {
            return new Resource(url, metadata, contentType, timestamp, locator);
        }
    }

}
