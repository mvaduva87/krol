package ro.mv.krol.storage;

import ro.mv.krol.browser.HtmlPage;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by mihai.vaduva on 08/08/2016.
 */
@Singleton
public class PageStorage {

    private final Storage storage;

    @Inject
    public PageStorage(Storage storage) {
        this.storage = storage;
    }

    public Map<StoredType, String> store(HtmlPage htmlPage) throws IOException {
        Map<StoredType, String> locatorMap = new LinkedHashMap<>();
        locatorMap.put(StoredType.SOURCE, storeContent(htmlPage));
        if (htmlPage.getScreenshot() != null) {
            locatorMap.put(StoredType.SCREENSHOT, storeScreenshot(htmlPage));
        }
        return locatorMap;
    }

    private String storeContent(HtmlPage htmlPage) throws IOException {
        StorageKey key = StorageKey.builder()
                .withType(StoredType.SOURCE)
                .withName(htmlPage.getUrlHash() + ".html")
                .withTimestamp(htmlPage.getTimestamp())
                .withMetadata(htmlPage.getMetadata())
                .build();
        try (Reader reader = new StringReader(htmlPage.getSource())) {
            return storage.write(key, reader);
        }
    }

    private String storeScreenshot(HtmlPage htmlPage) throws IOException {
        StorageKey key = StorageKey.builder()
                .withType(StoredType.SCREENSHOT)
                .withName(htmlPage.getUrlHash() + ".png")
                .withTimestamp(htmlPage.getTimestamp())
                .withMetadata(htmlPage.getMetadata())
                .build();
        try (InputStream stream = new ByteArrayInputStream(htmlPage.getScreenshot())) {
            return storage.write(key, stream);
        }
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
