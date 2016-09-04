package ro.mv.krol.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.mv.krol.browser.Browser;
import ro.mv.krol.exception.CrawlException;
import ro.mv.krol.exception.ScriptCompileException;
import ro.mv.krol.model.CompiledSeed;
import ro.mv.krol.model.Page;
import ro.mv.krol.model.Seed;
import ro.mv.krol.script.ScriptManager;
import ro.mv.krol.util.Args;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Created by mihai.vaduva on 08/08/2016.
 */
public class Crawler implements AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Browser browser;
    private final ScriptManager scriptManager;
    private final PageProcessor pageProcessor;

    @Inject
    public Crawler(Browser browser,
                   ScriptManager scriptManager,
                   PageProcessor pageProcessor) {
        this.browser = Args.notNull(browser, "browser");
        this.scriptManager = Args.notNull(scriptManager, "scriptManager");
        this.pageProcessor = Args.notNull(pageProcessor, "pageProcessor");
    }

    public List<Page> crawl(final Seed seed) throws CrawlException {
        List<Page> pages = new ArrayList<>();
        crawl(seed, pages::add);
        return pages;
    }

    public int crawl(final Seed seed, Consumer<Page> consumer) throws CrawlException {
        final CompiledSeed compiledSeed = compile(seed);
        final AtomicInteger processedCount = new AtomicInteger(0);
        browser.crawl(seed, compiledSeed.getCrawlScript(), htmlPage -> {
            try {
                Page page = pageProcessor.process(htmlPage, compiledSeed);
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

    CompiledSeed compile(Seed seed) throws ScriptCompileException {
        return CompiledSeed.compile(seed, scriptManager);
    }

    @Override
    public void close() throws Exception {
        browser.close();
    }
}
