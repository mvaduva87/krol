package ro.mv.krol.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import ro.mv.krol.storage.StoredType;
import ro.mv.krol.util.Args;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by mihai.vaduva on 10/08/2016.
 */
@JsonDeserialize(builder = Page.Builder.class)
public class Page {

    @JsonProperty
    private final URL url;

    @JsonProperty
    private final Date timestamp;

    @JsonProperty
    private final Charset charset;

    @JsonProperty
    private final Map<StoredType, String> locator;

    @JsonProperty
    private final List<Link> links;

    @JsonProperty
    private final List<Resource> resources;

    @JsonProperty
    private final Map<String, String> metadata;

    private Page(URL url,
                 Date timestamp,
                 Charset charset,
                 Map<StoredType, String> locator,
                 List<Link> links,
                 List<Resource> resources,
                 Map<String, String> metadata) {
        this.url = Args.notNull(url, "url");
        this.timestamp = Args.notNull(timestamp, "timestamp");
        this.charset = Args.notNull(charset, "charset");
        this.locator = Args.notEmpty(locator, "locator");
        this.links = links;
        this.resources = resources;
        this.metadata = metadata;
    }

    public URL getUrl() {
        return url;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Charset getCharset() {
        return charset;
    }

    public Map<StoredType, String> getLocator() {
        return locator;
    }

    public List<Link> getLinks() {
        return links;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder
    public static class Builder {

        private URL url;
        private Date timestamp;
        private Charset charset;
        private Map<StoredType, String> locator;
        private List<Link> links;
        private List<Resource> resources;
        private Map<String, String> metadata;

        public Builder withUrl(URL url) {
            this.url = url;
            return this;
        }

        public Builder withTimestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withCharset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public Builder withLocator(Map<StoredType, String> locator) {
            this.locator = locator;
            return this;
        }

        public Builder withLinks(List<Link> links) {
            this.links = links;
            return this;
        }

        public Builder withResources(List<Resource> resources) {
            this.resources = resources;
            return this;
        }

        public Builder withMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Page build() {
            return new Page(url, timestamp, charset, locator, links, resources, metadata);
        }
    }
}
