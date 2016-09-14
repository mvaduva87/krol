package ro.mv.krol.storage;

import org.apache.commons.io.IOUtils;
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

    private static final Charset DEFAULT_CHARSET = Charset.forName("US-ASCII");
    private final Storage storage;

    @Inject
    public ResourceStorage(Storage storage) {
        this.storage = Args.notNull(storage, "storage");
    }

    public Resource store(Link resourceLink) throws IOException {
        Date timestamp = new Date();
        String urlString = resourceLink.getUrl();
        DataContent dataContent;
        if (URLUtils.isDataEncoded(urlString)) {
            dataContent = fetchEncodedDataFrom(urlString);
        } else {
            dataContent = fetchDirectDataFrom(urlString);
        }
        String locator;
        StorageKey storageKey = createKeyFor(resourceLink, timestamp, dataContent.contentType);
        try (InputStream stream = new ByteArrayInputStream(dataContent.data)) {
            locator = storage.write(storageKey, stream);
        }
        return new Resource.Builder()
                .withUrl(urlString)
                .withTimestamp(timestamp)
                .withContentType(dataContent.contentType)
                .withLocator(locator)
                .withMetadata(resourceLink.getMetadata())
                .build();
    }

    private StorageKey createKeyFor(Link resourceLink, Date timestamp, String contentType) {
        return StorageKey.builder()
                .withType(StoredType.RESOURCE)
                .withUrl(resourceLink.getUrl())
                .withTimestamp(timestamp)
                .withContentType(contentType)
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
        String contentType = conn.getHeaderField("Content-Type");
        try (InputStream stream = conn.getInputStream()) {
            byte[] data = IOUtils.toByteArray(stream);
            return new DataContent(contentType, DEFAULT_CHARSET, data);
        }
    }

    private static class DataContent {

        final String contentType;
        final Charset charset;
        final byte[] data;

        DataContent(String contentType, Charset charset, byte[] data) {
            this.contentType = contentType == null || contentType.isEmpty() ? "application/octet-stream" : contentType;
            this.charset = charset;
            this.data = data;
        }
    }
}
