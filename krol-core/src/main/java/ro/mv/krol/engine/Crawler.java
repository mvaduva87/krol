package ro.mv.krol.engine;

import ro.mv.krol.browser.Browser;
import ro.mv.krol.browser.HtmlPage;
import ro.mv.krol.exception.CrawlException;
import ro.mv.krol.exception.ScriptCompileException;
import ro.mv.krol.extract.Document;
import ro.mv.krol.extract.DocumentFactory;
import ro.mv.krol.extract.LinkExtractor;
import ro.mv.krol.model.*;
import ro.mv.krol.script.ScriptManager;
import ro.mv.krol.storage.PageStorage;
import ro.mv.krol.storage.ResourceStorage;
import ro.mv.krol.storage.StoredType;
import ro.mv.krol.util.Args;
import ro.mv.krol.util.Metadata;
import ro.mv.krol.util.URLUtils;
import groovy.lang.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by mihai.vaduva on 08/08/2016.
 */
public class Crawler implements AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Browser browser;
    private final ScriptManager scriptManager;
    private final PageStorage pageStorage;
    private final ResourceStorage resourceStorage;
    private final DocumentFactory documentFactory;
    private final LinkExtractor linkExtractor = new LinkExtractor();

    @Inject
    public Crawler(Browser browser,
                   ScriptManager scriptManager,
                   PageStorage pageStorage,
                   ResourceStorage resourceStorage,
                   DocumentFactory documentFactory) {
        this.browser = Args.notNull(browser, "browser");
        this.scriptManager = Args.notNull(scriptManager, "scriptManager");
        this.pageStorage = Args.notNull(pageStorage, "pageStorage");
        this.resourceStorage = Args.notNull(resourceStorage, "resourceStorage");
        this.documentFactory = Args.notNull(documentFactory, "documentFactory");
    }

    public List<Page> crawl(final Seed seed) throws CrawlException {
        List<Page> pages = new ArrayList<>();
        crawl(seed, pages::add);
        return pages;
    }

    public int crawl(final Seed seed, Consumer<Page> consumer) throws CrawlException {
        Script crawlScript = compileScriptFor(seed);
        final AtomicInteger processedCount = new AtomicInteger(0);
        long time = System.currentTimeMillis();
        browser.crawl(seed, crawlScript, htmlPage -> {
            logger.info("crawl time " + (System.currentTimeMillis() - time));
            try {
                Page page = createFrom(htmlPage, seed);
                consumer.accept(page);
                processedCount.incrementAndGet();
            } catch (IOException | RuntimeException e) {
                logger.error("failed to process captured page", e);
            }
        });
        int count = processedCount.get();
        if (count == 0) {
            throw new CrawlException("no pages successfully processed");
        }
        return count;
    }

    private Page createFrom(HtmlPage htmlPage, Seed origin) throws IOException {
        Metadata.transfer(origin.getMetadata(), htmlPage.getMetadata(), false);
        Page.Builder builder = Page.builder();
        builder.withUrl(htmlPage.getUrl());
        builder.withTimestamp(htmlPage.getTimestamp());
        builder.withCharset(htmlPage.getCharset());
        builder.withMetadata(htmlPage.getMetadata());
        Map<StoredType, String> locatorMap = pageStorage.store(htmlPage);
        builder.withLocator(locatorMap);
        try {
            URL baseURL = URLUtils.getBaseURLOf(htmlPage.getUrl());
            Document document = documentFactory.createFrom(baseURL, htmlPage.getSource());
            List<Link> links = extractLinks(document, htmlPage, origin.getSelectorsFor(Selector.Target.LINKS));
            builder.withLinks(links);
            List<Resource> resources = extractResources(document, htmlPage, origin.getSelectorsFor(Selector.Target.RESOURCES));
            builder.withResources(resources);
        } catch (IOException | RuntimeException e) {
            logger.warn("failed to process html page document", e);
        }
        return builder.build();
    }

    private Stream<Link> extract(Document document, HtmlPage htmlPage, List<Selector> selectors) {
        return selectors.stream()
                .filter(s -> pass(s, htmlPage))
                .map(s -> linkExtractor.extract(document, s))
                .flatMap(Collection::stream);
    }

    private boolean pass(Selector selector, HtmlPage htmlPage) {
        if (selector.getWhen() == null || selector.getWhen().isEmpty()) {
            return true;
        }
        if (htmlPage.getMetadata() == null || htmlPage.getMetadata().isEmpty()) {
            return false;
        }
        try {
            Script script = scriptManager.parse(selector.getWhen());
            htmlPage.getMetadata().forEach(script::setProperty);
            boolean result = (boolean) script.run();
            return result;
        } catch (Exception ignored) {
            return false;
        }
    }

    private Script compileScriptFor(Seed seed) throws ScriptCompileException {
        if (seed.getMetadata() == null || seed.getMetadata().isEmpty()) {
            return null;
        }
        String scriptUrlString = seed.getMetadata().get("x-script");
        if (scriptUrlString == null || scriptUrlString.isEmpty()) {
            return null;
        }
        return scriptManager.load(scriptUrlString);
    }

    private List<Link> extractLinks(Document document, HtmlPage htmlPage, List<Selector> selectors) {
        if (selectors == null || selectors.isEmpty()) {
            return null;
        }
        return extract(document, htmlPage, selectors)
                .collect(Collectors.toList());
    }

    private List<Resource> extractResources(Document document, HtmlPage htmlPage, List<Selector> selectors) {
        if (selectors == null || selectors.isEmpty()) {
            return null;
        }
        return extract(document, htmlPage, selectors)
                .map(link -> {
                    try {
                        return resourceStorage.store(link);
                    } catch (IOException e) {
                        logger.warn("failed to store resource " + link.getUrl(), e);
                        return null;
                    }
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }

    @Override
    public void close() throws Exception {
        browser.close();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Browser browser;
        private ScriptManager scriptManager;
        private PageStorage pageStorage;
        private ResourceStorage resourceStorage;
        private DocumentFactory documentFactory;

        public Builder withBrowser(Browser browser) {
            this.browser = browser;
            return this;
        }

        public Builder withScriptManager(ScriptManager scriptManager) {
            this.scriptManager = scriptManager;
            return this;
        }

        public Builder withPageStorage(PageStorage pageStorage) {
            this.pageStorage = pageStorage;
            return this;
        }

        public Builder withResourceStorage(ResourceStorage resourceStorage) {
            this.resourceStorage = resourceStorage;
            return this;
        }

        public Builder withDocumentFactory(DocumentFactory documentFactory) {
            this.documentFactory = documentFactory;
            return this;
        }

        public Crawler build() {
            if (scriptManager == null) {
                scriptManager = new ScriptManager();
            }
            return new Crawler(browser, scriptManager, pageStorage, resourceStorage, documentFactory);
        }
    }
}
