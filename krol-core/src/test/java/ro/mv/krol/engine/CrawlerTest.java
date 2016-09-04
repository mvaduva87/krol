package ro.mv.krol.engine;

import groovy.lang.Script;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import ro.mv.krol.browser.Browser;
import ro.mv.krol.browser.HtmlPage;
import ro.mv.krol.exception.CrawlException;
import ro.mv.krol.exception.ScriptCompileException;
import ro.mv.krol.model.CompiledSeed;
import ro.mv.krol.model.Page;
import ro.mv.krol.model.Seed;
import ro.mv.krol.script.ScriptManager;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

/**
 * Created by mihai.vaduva on 04/09/2016.
 */
@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class CrawlerTest {

    private Crawler crawler;

    @Mock
    private Browser browser;

    @Mock
    private ScriptManager scriptManager;

    @Mock
    private PageProcessor pageProcessor;

    @Mock
    private Seed seed;

    @Mock
    private CompiledSeed compiledSeed;

    @Mock
    private HtmlPage htmlPage;

    @Mock
    private Page page;

    private final Random random = new Random(System.currentTimeMillis());

    @Before
    public void setUp() throws Exception {
        crawler = new Crawler(browser, scriptManager, pageProcessor);
        crawler = spy(crawler);
        doReturn(compiledSeed).when(crawler).compile(seed);
        doReturn(mock(Page.class)).when(pageProcessor)
                .process(any(HtmlPage.class), any(CompiledSeed.class));
    }

    @Test
    public void shouldCompileSeedThenCrawlWithBrowserAndFinallyProcess() throws Exception {
        doAnswer(invocation -> {
            Consumer<HtmlPage> consumer = (Consumer<HtmlPage>) invocation.getArguments()[2];
            consumer.accept(htmlPage);
            return null;
        }).when(browser).crawl(any(Seed.class), any(Script.class), any(Consumer.class));
        InOrder inOrder = inOrder(crawler, browser, pageProcessor);

        crawler.crawl(seed);

        inOrder.verify(crawler, times(1)).compile(seed);
        inOrder.verify(browser, times(1)).crawl(any(Seed.class), any(Script.class), any(Consumer.class));
        inOrder.verify(pageProcessor, times(1)).process(htmlPage, compiledSeed);
        inOrder.verifyNoMoreInteractions();
    }

    @Test(expected = ScriptCompileException.class)
    public void shouldFailCrawlOnSeedCompileError() throws Exception {
        doThrow(ScriptCompileException.class).when(crawler).compile(seed);
        InOrder inOrder = inOrder(crawler, browser, pageProcessor);

        try {
            crawler.crawl(seed);
        } finally {
            inOrder.verify(crawler, times(1)).compile(seed);
            inOrder.verifyNoMoreInteractions();
        }
    }

    @Test(expected = CrawlException.class)
    public void shouldThrowCrawlExceptionIfNoHtmlPagesAreCapturedWithBrowser() throws Exception {
        InOrder inOrder = inOrder(crawler, browser, pageProcessor);

        try {
            crawler.crawl(seed);
        } finally {
            inOrder.verify(crawler, times(1)).compile(seed);
            inOrder.verify(browser, times(1))
                    .crawl(any(Seed.class), any(Script.class), any(Consumer.class));
            inOrder.verifyNoMoreInteractions();
        }
    }

    @Test(expected = CrawlException.class)
    public void shouldThrowCrawlExceptionIfNoCapturedHtmlPagesAreProcessedSuccessfully() throws Exception {
        int capturedHtmlPageCount = Math.max(1, random.nextInt(100));
        doAnswer(BrowserCapture.times(capturedHtmlPageCount)).when(browser)
                .crawl(any(Seed.class), any(Script.class), any(Consumer.class));
        doThrow(IOException.class).when(pageProcessor)
                .process(any(HtmlPage.class), any(CompiledSeed.class));
        InOrder inOrder = inOrder(crawler, browser, pageProcessor);

        try {
            crawler.crawl(seed);
        } finally {
            inOrder.verify(crawler, times(1)).compile(seed);
            inOrder.verify(browser, times(1))
                    .crawl(any(Seed.class), any(Script.class), any(Consumer.class));
            inOrder.verify(pageProcessor, times(capturedHtmlPageCount))
                    .process(any(HtmlPage.class), any(CompiledSeed.class));
        }
    }

    @Test
    public void shouldReturnSuccessfullyProcessedHtmlPages() throws Exception {
        int capturedHtmlPageCount = Math.max(2, random.nextInt(100));
        int processFailCount = capturedHtmlPageCount / 2;
        doAnswer(BrowserCapture.times(capturedHtmlPageCount)).when(browser)
                .crawl(any(Seed.class), any(Script.class), any(Consumer.class));
        doAnswer(new Answer() {
            int count = 0;

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (count++ < processFailCount) {
                    throw new IOException();
                }
                return mock(Page.class);
            }
        }).when(pageProcessor).process(any(HtmlPage.class), any(CompiledSeed.class));
        InOrder inOrder = inOrder(crawler, browser, pageProcessor);

        List<Page> pages = crawler.crawl(seed);

        inOrder.verify(crawler, times(1)).compile(seed);
        inOrder.verify(browser, times(1))
                .crawl(any(Seed.class), any(Script.class), any(Consumer.class));
        inOrder.verify(pageProcessor, times(capturedHtmlPageCount))
                .process(any(HtmlPage.class), any(CompiledSeed.class));
        inOrder.verifyNoMoreInteractions();
        assertThat(pages.size(), is(capturedHtmlPageCount - processFailCount));
    }

    private static class BrowserCapture implements Answer {

        private int captureCount;

        static BrowserCapture times(int count) {
            return new BrowserCapture(count);
        }

        BrowserCapture(int captureCount) {
            this.captureCount = captureCount;
        }

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            Consumer<HtmlPage> consumer = (Consumer<HtmlPage>) invocation.getArguments()[2];
            for (int i = 0; i < captureCount; i++) {
                consumer.accept(mock(HtmlPage.class));
            }
            return null;
        }
    }
}
