package ro.mv.krol.storage;

import org.apache.commons.codec.digest.DigestUtils;
import ro.mv.krol.browser.HtmlPage;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by mihai.vaduva on 08/08/2016.
 */
@Singleton
public class PageStorage {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private final Storage storage;

    @Inject
    public PageStorage(Storage storage) {
        this.storage = storage;
    }

    public Map<StoredType, String> store(HtmlPage htmlPage) throws IOException {
        Map<StoredType, String> locatorMap = new LinkedHashMap<>();
        String name = generateNameFor(htmlPage);
        locatorMap.put(StoredType.SOURCE, storeContent(htmlPage, name));
        if (htmlPage.getScreenshot() != null) {
            locatorMap.put(StoredType.SCREENSHOT, storeScreenshot(htmlPage, name));
        }
        return locatorMap;
    }

    private String storeContent(HtmlPage htmlPage, String name) throws IOException {
        StorageKey key = StorageKey.builder()
                .withType(StoredType.SOURCE)
                .withContentType("text/html")
                .withCharset(htmlPage.getCharset())
                .withName(name + ".html")
                .withTimestamp(htmlPage.getTimestamp())
                .withMetadata(htmlPage.getMetadata())
                .build();
        try (Reader reader = new StringReader(htmlPage.getSource())) {
            return storage.write(key, reader);
        }
    }

    private String storeScreenshot(HtmlPage htmlPage, String name) throws IOException {
        StorageKey key = StorageKey.builder()
                .withType(StoredType.SCREENSHOT)
                .withContentType("image/png")
                .withCharset(UTF8)
                .withName(name + ".png")
                .withTimestamp(htmlPage.getTimestamp())
                .withMetadata(htmlPage.getMetadata())
                .build();
        try (InputStream stream = new ByteArrayInputStream(htmlPage.getScreenshot())) {
            return storage.write(key, stream);
        }
    }

    private static String generateNameFor(HtmlPage htmlPage) {
        String rawName = htmlPage.getTimestamp().toString() + "/" + htmlPage.getUrl().toExternalForm();
        return DigestUtils.md5Hex(rawName);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Storage storage;

        public Builder withStorage(Storage storage) {
            this.storage = storage;
            return this;
        }

        public PageStorage build() {
            return new PageStorage(storage);
        }
    }

}
