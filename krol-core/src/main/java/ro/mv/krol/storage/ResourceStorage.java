package ro.mv.krol.storage;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import ro.mv.krol.model.DecodedURL;
import ro.mv.krol.model.Link;
import ro.mv.krol.model.Resource;
import ro.mv.krol.util.Args;
import ro.mv.krol.util.URLUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * Created by mihai on 8/22/16.
 */
@Singleton
public class ResourceStorage {

    private final Storage storage;

    @Inject
    public ResourceStorage(Storage storage) {
        this.storage = Args.notNull(storage, "storage");
    }

    public Resource store(Link resourceLink) throws IOException {
        String urlString = resourceLink.getUrl();
        DataContent dataContent;
        if (URLUtils.isDataEncoded(urlString)) {
            dataContent = fetchEncodedDataFrom(urlString);
        } else {
            dataContent = fetchDirectDataFrom(urlString);
        }
        String locator;
        StorageKey storageKey = createKeyFor(resourceLink, dataContent);
        try (InputStream stream = new ByteArrayInputStream(dataContent.data)) {
            locator = storage.write(storageKey, stream);
        }
        return new Resource.Builder()
                .withUrl(urlString)
                .withTimestamp(dataContent.timestamp)
                .withContentType(dataContent.contentType)
                .withLocator(locator)
                .withCharset(dataContent.charset)
                .withMetadata(resourceLink.getMetadata())
                .build();
    }

    private StorageKey createKeyFor(Link resourceLink, DataContent dataContent) {
        return StorageKey.builder()
                .withType(StoredType.RESOURCE)
                .withUrl(resourceLink.getUrl())
                .withTimestamp(dataContent.timestamp)
                .withContentType(dataContent.contentType)
                .withCharset(dataContent.charset)
                .withMetadata(resourceLink.getMetadata())
                .build();
    }

    private DataContent fetchEncodedDataFrom(String urlString) {
        DecodedURL decodedURL = URLUtils.decode(urlString);
        if (decodedURL == null) {
            throw new NullPointerException("unexpected null decodedURL");
        }
        return new DataContent(decodedURL.getMediaType(), decodedURL.getCharset(), decodedURL.getData());
    }

    private DataContent fetchDirectDataFrom(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        String contentType = conn.getHeaderField(HttpHeaders.CONTENT_TYPE);
        String contentEncoding = conn.getHeaderField(HttpHeaders.CONTENT_ENCODING);
        Charset charset = null;
        if (contentEncoding != null) {
            try {
                charset = Charset.forName(contentEncoding);
            } catch (IllegalArgumentException ignored) {
                // could not parse the content-encoding to a java.nio.Charset
            }
        }

        try (InputStream stream = conn.getInputStream()) {
            byte[] data = IOUtils.toByteArray(stream);
            return new DataContent(contentType, charset, data);
        }
    }

    private static class DataContent {

        final Date timestamp;
        final String contentType;
        final Charset charset;
        final byte[] data;

        DataContent(String contentType, Charset charset, byte[] data) {
            this.timestamp = new Date();
            this.contentType = contentType == null || contentType.isEmpty() ? "application/octet-stream" : contentType;
            this.charset = charset;
            this.data = data;
        }
    }
}
