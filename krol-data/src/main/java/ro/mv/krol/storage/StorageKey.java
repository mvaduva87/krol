package ro.mv.krol.storage;

import ro.mv.krol.util.Args;

import java.util.Date;
import java.util.Map;

/**
 * Created by mihai.vaduva on 13/08/2016.
 */
public class StorageKey {

    private final StoredType type;
    private final String url;
    private final Date timestamp;
    private final Map<String, String> metadata;

    private StorageKey(StoredType type,
                       String url,
                       Date timestamp,
                       Map<String, String> metadata) {
        this.type = Args.notNull(type, "type");
        this.url = Args.notEmpty(url, "url");
        this.timestamp = Args.notNull(timestamp, "timestamp");
        this.metadata = metadata;
    }

    public StoredType getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private StoredType type;
        private String url;
        private Date timestamp;
        private Map<String, String> metadata;

        public Builder withType(StoredType type) {
            this.type = type;
            return this;
        }

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder withTimestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public StorageKey build() {
            return new StorageKey(type, url, timestamp, metadata);
        }
    }
}
