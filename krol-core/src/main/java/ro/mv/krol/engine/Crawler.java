package ro.mv.krol.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.mv.krol.browser.Browser;
import ro.mv.krol.browser.HtmlPage;
import ro.mv.krol.exception.CrawlException;
import ro.mv.krol.model.*;
import ro.mv.krol.script.ScriptManager;
import ro.mv.krol.storage.PageStorage;
import ro.mv.krol.storage.ResourceStorage;
import ro.mv.krol.storage.StoredType;
import ro.mv.krol.util.Args;
import ro.mv.krol.util.Metadata;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by mihai.vaduva on 08/08/2016.
 */
public class Crawler implements AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Browser browser;
    private final ScriptManager scriptManager;
    private final PageStorage pageStorage;
    private final ResourceStorage resourceStorage;
    private final Extractor extractor;

    @Inject
    public Crawler(Browser browser,
                   ScriptManager scriptManager,
                   PageStorage pageStorage,
                   ResourceStorage resourceStorage,
                   Extractor extractor) {
        this.browser = Args.notNull(browser, "browser");
        this.scriptManager = Args.notNull(scriptManager, "scriptManager");
        this.pageStorage = Args.notNull(pageStorage, "pageStorage");
        this.resourceStorage = Args.notNull(resourceStorage, "resourceStorage");
        this.extractor = Args.notNull(extractor, "extractor");
    }

    public List<Page> crawl(final Seed seed) throws CrawlException {
        List<Page> pages = new ArrayList<>();
        crawl(seed, pages::add);
        return pages;
    }

    public int crawl(final Seed seed, Consumer<Page> consumer) throws CrawlException {
        final CompiledSeed compiledSeed = CompiledSeed.compile(seed, scriptManager);
        final AtomicInteger processedCount = new AtomicInteger(0);
        browser.crawl(seed, compiledSeed.getCrawlScript(), htmlPage -> {
            try {
                Page page = createPageFrom(htmlPage, compiledSeed);
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

    protected Page createPageFrom(HtmlPage htmlPage, CompiledSeed origin) throws IOException {
        Metadata.transfer(origin.getMetadata(), htmlPage.getMetadata(), false);
        Page.Builder builder = Page.builder();
        builder.withUrl(htmlPage.getUrl());
        builder.withTimestamp(htmlPage.getTimestamp());
        builder.withCharset(htmlPage.getCharset());
        builder.withMetadata(htmlPage.getMetadata());
        Map<StoredType, String> locatorMap = pageStorage.store(htmlPage);
        builder.withLocator(locatorMap);
        if (origin.canExtract()) {
            try {
                Map<Selector.Target, List<Link>> linkMap = extractor.extract(htmlPage, origin);
                if (linkMap != null) {
                    builder.withLinks(linkMap.get(Selector.Target.LINKS));
                    List<Link> resourceLinks = linkMap.get(Selector.Target.RESOURCES);
                    if (resourceLinks != null && !resourceLinks.isEmpty()) {
                        builder.withResources(storeResourcesFrom(resourceLinks));
                    }
                }
            } catch (IOException | RuntimeException e) {
                logger.warn("failed to process html page document", e);
            }
        }
        return builder.build();
    }

    private List<Resource> storeResourcesFrom(List<Link> links) {
        return links.stream()
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
}
