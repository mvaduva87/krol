package ro.mv.krol.browser;

import ro.mv.krol.util.Args;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mihai.vaduva on 8/7/16.
 */
public class HtmlPage {

    private final URL url;
    private final Date timestamp;
    private final Charset charset;
    private final Map<String, String> metadata;
    private final String source;
    private final byte[] screenshot;

    private HtmlPage(URL url,
                     Date timestamp,
                     String source,
                     byte[] screenshot,
                     Charset charset,
                     Map<String, String> metadata) {
        this.url = Args.notNull(url, "url");
        this.timestamp = Args.notNull(timestamp, "timestamp");
        this.source = Args.notEmpty(source, "source");
        this.screenshot = screenshot;
        this.charset = Args.notNull(charset, "charset");
        this.metadata = metadata;
    }

    public URL getUrl() {
        return url;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getSource() {
        return source;
    }

    public byte[] getScreenshot() {
        return screenshot;
    }

    public Charset getCharset() {
        return charset;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private URL url;
        private Date timestamp;
        private String source;
        private byte[] screenshot;
        private Charset charset;
        private Map<String, String> metadata = new HashMap<>();

        public Builder withUrl(URL url) {
            this.url = url;
            return this;
        }

        public Builder withTimestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withSource(String source) {
            this.source = source;
            return this;
        }

        public Builder withScreenshot(byte[] screenshot) {
            this.screenshot = screenshot;
            return this;
        }

        public Builder withCharset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public Builder withMetadata(Map<String, String> metadata) {
            if (metadata != null) {
                this.metadata.putAll(metadata);
            }
            return this;
        }

        public HtmlPage build() {
            return new HtmlPage(url, timestamp, source, screenshot, charset, metadata);
        }
    }
}
