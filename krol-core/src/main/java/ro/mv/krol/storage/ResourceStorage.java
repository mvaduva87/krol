package ro.mv.krol.storage;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import ro.mv.krol.model.DecodedURL;
import ro.mv.krol.model.Link;
import ro.mv.krol.model.Resource;
import ro.mv.krol.storage.cache.ResourceCache;
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
    private final ResourceCache resourceCache;

    @Inject
    public ResourceStorage(Storage storage, ResourceCache resourceCache) {
        this.storage = Args.notNull(storage, "storage");
        this.resourceCache = Args.notNull(resourceCache, "resourceCache");
    }

    public Resource store(Link resourceLink) throws IOException {
        String urlString = resourceLink.getUrl();
        if (URLUtils.isDataEncoded(urlString)) {
            DataContent data = storeEncoded(resourceLink);
            return createResourceFrom(resourceLink, data);
        } else {
            Resource cachedResource = resourceCache.get(urlString);
            if (cachedResource == null) {
                DataContent data = storeDirect(resourceLink);
                cachedResource = createResourceFrom(resourceLink, data);
                resourceCache.put(urlString, cachedResource);
            }
            return cachedResource;
        }
    }

    private Resource createResourceFrom(Link resourceLink, DataContent dataContent) {
        return new Resource.Builder()
                .withUrl(resourceLink.getUrl())
                .withTimestamp(dataContent.timestamp)
                .withMetadata(resourceLink.getMetadata())
                .withContentType(dataContent.contentType)
                .withLocator(dataContent.locator)
                .build();
    }

    private DataContent storeEncoded(Link resourceLink) throws IOException {
        String urlString = resourceLink.getUrl();
        DataContent dataContent = fetchEncodedDataFrom(urlString);
        StorageKey storageKey = createStorageKeyFor(resourceLink, dataContent);
        try (InputStream stream = new ByteArrayInputStream(dataContent.data)) {
            dataContent.locator = storage.write(storageKey, stream);
        }
        return dataContent;
    }

    private DataContent storeDirect(Link resourceLink) throws IOException {
        String urlString = resourceLink.getUrl();
        DataContent dataContent = fetchDirectDataFrom(urlString);
        StorageKey storageKey = createStorageKeyFor(resourceLink, dataContent);
        try (InputStream stream = new ByteArrayInputStream(dataContent.data)) {
            dataContent.locator = storage.write(storageKey, stream);
        }
        return dataContent;
    }

    private StorageKey createStorageKeyFor(Link resourceLink, DataContent dataContent) {
        String name = DigestUtils.md5Hex(dataContent.data);
        return StorageKey.builder()
                .withType(StoredType.RESOURCE)
                .withContentType(dataContent.contentType)
                .withCharset(dataContent.charset)
                .withTimestamp(dataContent.timestamp)
                .withMetadata(resourceLink.getMetadata())
                .withName(name)
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
        final Date timestamp = new Date();
        String locator;

        DataContent(String contentType, Charset charset, byte[] data) {
            this.contentType = contentType == null || contentType.isEmpty() ? "application/octet-stream" : contentType;
            this.charset = charset;
            this.data = data;
        }
    }
}
