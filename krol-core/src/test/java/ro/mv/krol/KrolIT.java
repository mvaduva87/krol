package ro.mv.krol;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ro.mv.krol.browser.Browser;
import ro.mv.krol.browser.HtmlGrabber;
import ro.mv.krol.browser.HtmlUnitWebDriverFactory;
import ro.mv.krol.browser.WebDriverFactory;
import ro.mv.krol.engine.Crawler;
import ro.mv.krol.engine.Extractor;
import ro.mv.krol.engine.PageProcessor;
import ro.mv.krol.extract.DocumentFactory;
import ro.mv.krol.extract.LinkExtractor;
import ro.mv.krol.extract.jsoup.JsoupDocumentFactory;
import ro.mv.krol.model.*;
import ro.mv.krol.script.ScriptManager;
import ro.mv.krol.storage.*;
import ro.mv.krol.storage.cache.MemoryResourceCache;
import ro.mv.krol.storage.cache.ResourceCache;
import ro.mv.krol.util.Metadata;
import ro.mv.krol.util.PathTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by mihai.vaduva on 04/09/2016.
 */
public class KrolIT {

    private static final String ROOT_DIR_PATH = "archive";
    private static final URL pageURL = KrolIT.class.getResource("/page/test.html");

    private Crawler crawler;
    private Seed seed;

    @Before
    public void setUp() throws Exception {
        Browser browser = prepareBrowser();
        Storage storage = prepareStorage();
        ScriptManager scriptManager = new ScriptManager();
        DocumentFactory documentFactory = new JsoupDocumentFactory();
        Extractor extractor = new Extractor(documentFactory, new LinkExtractor());
        PageStorage pageStorage = new PageStorage(storage);
        ResourceCache resourceCache = new MemoryResourceCache(100);
        ResourceStorage resourceStorage = new ResourceStorage(storage, resourceCache);
        PageProcessor pageProcessor = new PageProcessor(extractor, pageStorage, resourceStorage);
        crawler = new Crawler(browser, scriptManager, pageProcessor);
        seed = prepareSeed();
    }

    @After
    public void tearDown() throws Exception {
        if (crawler != null) {
            crawler.close();
        }
    }

    private Browser prepareBrowser() {
        WebDriverFactory webDriverFactory = new HtmlUnitWebDriverFactory();
        return new Browser(webDriverFactory, 10000L, 1000L, 1000L, new HtmlGrabber());
    }

    private Storage prepareStorage() {
        Map<StoredType, String> templateMap = new HashMap<>();
        templateMap.put(StoredType.SOURCE, "pages/{date:format(timestamp, 'yyyy-MM-dd')}/{name}");
        templateMap.put(StoredType.SCREENSHOT, "pages/{date:format(timestamp, 'yyyy-MM-dd')}/{name}");
        templateMap.put(StoredType.RESOURCE, "resources/{name}");
        PathTemplate pathTemplate = new PathTemplate(templateMap);
        return new FileSystemStorage(ROOT_DIR_PATH, pathTemplate);
    }

    private Seed prepareSeed() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("type", "test");
        URL scriptURL = getClass().getResource("/crawl.groovy");
        metadata.put(Metadata.FIELD_SCRIPT, scriptURL.toExternalForm());
        Map<Selector.Target, List<Selector>> selectors = new HashMap<>();
        Selector resourceSelector = Selector.builder()
                .withName("main-image")
                .withQuery("#main-image")
                .withAttribute("abs:src")
                .build();
        Selector linkSelector = Selector.builder()
                .withName("main-link")
                .withQuery("#linksContainer > ul > li > a")
                .withAttribute("abs:href")
                .withMetadata(Collections.singletonMap("type", "child"))
                .build();
        selectors.put(Selector.Target.RESOURCES, Collections.singletonList(resourceSelector));
        selectors.put(Selector.Target.LINKS, Collections.singletonList(linkSelector));
        return Seed.builder()
                .withUrl(pageURL)
                .withTimestamp(new Date())
                .withSelectors(selectors)
                .withMetadata(metadata)
                .build();
    }

    @Test
    public void crawlSeed() throws Exception {
        Page page = crawler.crawl(seed).get(0);

        assertThat(page.getUrl().toExternalForm(), is(equalTo(pageURL.toExternalForm())));
        assertThat(page.getCharset(), is(equalTo(Charset.forName("UTF-8"))));
        assertThat(page.getMetadata(), hasEntry("type", "test"));
        assertThat(page.getMetadata(), hasEntry("script-meta", "success"));
        assertThat(page.getLinks(), hasSize(10));
        validate(page.getLinks());
        assertThat(page.getResources(), hasSize(1));
        Resource resource = page.getResources().get(0);
        URL imageURL = getClass().getResource("/page/test.gif");
        File imageFile = new File(imageURL.getFile());
        assertThat(resource.getUrl(), is(equalTo(imageURL.toExternalForm())));
        assertThat(resource.getContentType(), is(equalTo("image/gif")));
        assertThat(FileUtils.contentEquals(imageFile, new File(resource.getLocator())), is(true));
        validateSourceOf(page);
    }

    private void validate(List<Link> links) {
        int i = 0;
        for (Link link : links) {
            assertThat(link.getUrl(), is(equalTo("file:/child" + i)));
            assertThat(link.getMetadata(), hasEntry("selector", "main-link"));
            assertThat(link.getMetadata(), hasEntry("type", "child"));
            i++;
        }
    }

    private void validateSourceOf(Page page) throws IOException {
        assertThat(page.getLocator().get(StoredType.SOURCE), not(isEmptyOrNullString()));
        // todo: a bit tricky but should find a way to compare stored html with origin html
    }
}
