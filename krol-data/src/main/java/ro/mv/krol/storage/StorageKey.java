package ro.mv.krol.storage;

import ro.mv.krol.util.Args;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;

/**
 * Created by mihai.vaduva on 13/08/2016.
 */
public class StorageKey {

    private final StoredType type;
    private final String contentType;
    private final Charset charset;
    private final String name;
    private final Date timestamp;
    private final Map<String, String> metadata;

    private StorageKey(StoredType type,
                       String contentType,
                       Charset charset,
                       String name,
                       Date timestamp,
                       Map<String, String> metadata) {
        this.type = Args.notNull(type, "type");
        this.contentType = Args.notEmpty(contentType, "contentType");
        this.charset = Args.notNull(charset, "charset");
        this.name = Args.notEmpty(name, "name");
        this.timestamp = Args.notNull(timestamp, "timestamp");
        this.metadata = metadata;
    }

    public StoredType getType() {
        return type;
    }

    public String getContentType() {
        return contentType;
    }

    public Charset getCharset() {
        return charset;
    }

    public String getName() {
        return name;
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
        private String contentType;
        private Charset charset;
        private String name;
        private Date timestamp;
        private Map<String, String> metadata;

        public Builder withType(StoredType type) {
            this.type = type;
            return this;
        }

        public Builder withContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder withCharset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
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
            return new StorageKey(type, contentType, charset, name, timestamp, metadata);
        }
    }
}
