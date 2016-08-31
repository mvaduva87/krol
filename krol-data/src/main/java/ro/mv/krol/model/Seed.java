package ro.mv.krol.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import ro.mv.krol.util.Args;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by mihai.vaduva on 8/7/16.
 */
@JsonDeserialize(builder = Seed.Builder.class)
public class Seed {

    @JsonProperty
    protected final URL url;

    @JsonProperty
    protected final Date timestamp;

    @JsonProperty
    protected final Map<Selector.Target, List<Selector>> selectors;

    @JsonProperty
    protected final Map<String, String> metadata;

    protected Seed(URL url, Date timestamp, Map<Selector.Target, List<Selector>> selectors, Map<String, String> metadata) {
        this.url = Args.notNull(url, "url");
        this.timestamp = Args.notNull(timestamp, "timestamp");
        this.selectors = selectors;
        this.metadata = metadata;
    }

    public URL getUrl() {
        return url;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Map<Selector.Target, List<Selector>> getSelectors() {
        return selectors;
    }

    public List<Selector> getSelectorsFor(Selector.Target target) {
        return selectors != null ? selectors.get(target) : null;
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
        private Map<Selector.Target, List<Selector>> selectors;
        private Map<String, String> metadata;

        public Builder withUrl(URL url) {
            this.url = url;
            return this;
        }

        public Builder withTimestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withSelectors(Map<Selector.Target, List<Selector>> selectors) {
            this.selectors = selectors;
            return this;
        }

        public Builder withMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Seed build() {
            return new Seed(url, timestamp, selectors, metadata);
        }
    }

}
