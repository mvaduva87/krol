package ro.mv.krol.model;

import ro.mv.krol.util.Args;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * Created by mihai on 8/23/16.
 */
public class DecodedURL {

    private final String mediaType;
    private final Charset charset;
    private final boolean base64Encoded;
    private final byte[] data;

    private DecodedURL(String mediaType, String charsetName, boolean base64Encoded, String dataStr) {
        Args.notEmpty(dataStr, "data");
        this.mediaType = mediaType == null || mediaType.isEmpty() ? "text/plain" : mediaType;
        this.charset = charsetName == null || charsetName.isEmpty() ? Charset.forName("US-ASCII") : Charset.forName(charsetName);
        this.base64Encoded = base64Encoded;
        this.data = decode(dataStr, this.charset, base64Encoded);
    }

    private static byte[] decode(String data, Charset charset, boolean base64Encoded) {
        byte[] bytes = data.getBytes(charset);
        if (base64Encoded) {
            return Base64.getDecoder().decode(bytes);
        } else {
            return bytes;
        }
    }

    public String getMediaType() {
        return mediaType;
    }

    public Charset getCharset() {
        return charset;
    }

    public boolean isBase64Encoded() {
        return base64Encoded;
    }

    public byte[] getData() {
        return data;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String mediaType;
        private String charsetName;
        private boolean base64Encoded;
        private String data;

        public Builder withMediaType(String mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public Builder withCharsetName(String charsetName) {
            this.charsetName = charsetName;
            return this;
        }

        public Builder withBase64Encoded(boolean base64Encoded) {
            this.base64Encoded = base64Encoded;
            return this;
        }

        public Builder withData(String data) {
            this.data = data;
            return this;
        }

        public DecodedURL build() {
            return new DecodedURL(mediaType, charsetName, base64Encoded, data);
        }
    }
}
