package ro.mv.krol.browser;

import ro.mv.krol.exception.CrawlException;
import ro.mv.krol.model.Seed;
import ro.mv.krol.util.Args;
import groovy.lang.Script;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.ErrorHandler;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Closeable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * NOT THREAD SAFE
 * <p/>
 * Created by mihai.vaduva on 8/7/16.
 */
public class Browser implements Closeable {

    public static final String CONST_PAGE_LOAD_TIMEOUT = "selenium.pageLoadTimeout";
    public static final String CONST_IMPLICIT_WAIT_TIMEOUT = "selenium.implicitWaitTimeout";
    public static final String CONST_SCRIPT_TIMEOUT = "selenium.scriptTimeout";

    public static final String SCRIPT_PARAM_DRIVER = "driver";
    public static final String SCRIPT_PARAM_SEED = "seed";
    public static final String SCRIPT_PARAM_GRABBER = "grabber";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final WebDriverFactory webDriverFactory;
    private final long pageLoadTimeout;
    private final long implicitWaitTimeout;
    private final long scriptTimeout;
    private final HtmlGrabber htmlGrabber;

    private WebDriver driver;

    @Inject
    public Browser(WebDriverFactory webDriverFactory,
                   @Named(CONST_PAGE_LOAD_TIMEOUT) long pageLoadTimeout,
                   @Named(CONST_IMPLICIT_WAIT_TIMEOUT) long implicitWaitTimeout,
                   @Named(CONST_SCRIPT_TIMEOUT) long scriptTimeout,
                   HtmlGrabber htmlGrabber) {
        this.webDriverFactory = Args.notNull(webDriverFactory, "webDriverFactory");
        this.htmlGrabber = Args.notNull(htmlGrabber, "htmlGrabber");
        this.pageLoadTimeout = pageLoadTimeout;
        this.implicitWaitTimeout = implicitWaitTimeout;
        this.scriptTimeout = scriptTimeout;
    }

    public void crawl(Seed seed, Script script, Consumer<HtmlPage> consumer) throws CrawlException {
        prepareDriver();
        navigate(seed);
        if (script == null) {
            consumer.accept(forcedCapture());
        } else {
            GrabberDelegate grabberDelegate = new GrabberDelegate(driver, htmlGrabber, consumer);
            try {
                script.setProperty(SCRIPT_PARAM_SEED, seed);
                script.setProperty(SCRIPT_PARAM_DRIVER, driver);
                script.setProperty(SCRIPT_PARAM_GRABBER, grabberDelegate);
                script.run();
            } catch (Exception e) {
                if (e instanceof CrawlException) {
                    throw (CrawlException) e;
                }
                resetDriverIfCrashedFrom(e);
                logger.warn("crawl script exception", e);
            }
            if (!grabberDelegate.hasCaptured()) {
                consumer.accept(forcedCapture());
            }
        }
    }

    private void navigate(Seed seed) throws CrawlException {
        for (int i = 0; i < 2; i++) {
            try {
                driver.navigate().to(seed.getUrl());
                return;
            } catch (WebDriverException e) {
                resetDriverIfCrashedFrom(e);
            }
        }
        throw new CrawlException("failed to recover driver");
    }

    private HtmlPage forcedCapture() throws CrawlException {
        try {
            return htmlGrabber.grab(driver);
        } catch (RuntimeException e) {
            throw new CrawlException("failed on forced capture", e);
        }
    }

    private void prepareDriver() throws CrawlException {
        if (driver == null) {
            try {
                driver = webDriverFactory.newDriver();
                driver.manage().timeouts().pageLoadTimeout(pageLoadTimeout, TimeUnit.MILLISECONDS);
                driver.manage().timeouts().implicitlyWait(implicitWaitTimeout, TimeUnit.MILLISECONDS);
                driver.manage().timeouts().setScriptTimeout(scriptTimeout, TimeUnit.MILLISECONDS);
                driver.manage().window().maximize();
            } catch (WebDriverException e) {
                close();
                throw new CrawlException("failed to prepare driver", e);
            }
        }
    }

    private void resetDriverIfCrashedFrom(Exception e) {
        if (isDriverCrash(e)) {
            close();
        }
    }

    private boolean isDriverCrash(Exception e) {
        return e instanceof NoSuchSessionException
                || e instanceof SessionNotCreatedException
                || e instanceof UnreachableBrowserException
                || e instanceof ErrorHandler.UnknownServerException;
    }

    @Override
    public void close() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (RuntimeException ignored) {
            }
            driver = null;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private WebDriverFactory webDriverFactory;
        private long pageLoadTimeout = 30000L;
        private long implicitWaitTimeout;
        private long scriptTimeout;
        private HtmlGrabber htmlGrabber;

        public Builder withWebDriverFactory(WebDriverFactory webDriverFactory) {
            this.webDriverFactory = webDriverFactory;
            return this;
        }

        public Builder withPageLoadTimeout(long pageLoadTimeout) {
            this.pageLoadTimeout = pageLoadTimeout;
            return this;
        }

        public Builder withImplicitWaitTimeout(long implicitWaitTimeout) {
            this.implicitWaitTimeout = implicitWaitTimeout;
            return this;
        }

        public Builder withScriptTimeout(long scriptTimeout) {
            this.scriptTimeout = scriptTimeout;
            return this;
        }

        public Builder withHtmlGrabber(HtmlGrabber htmlGrabber) {
            this.htmlGrabber = htmlGrabber;
            return this;
        }

        public Browser build() {
            if (htmlGrabber == null) {
                htmlGrabber = new HtmlGrabber();
            }
            if (webDriverFactory == null) {
                webDriverFactory = new PhantomJSWebDriverFactory(null);
            }
            return new Browser(webDriverFactory, pageLoadTimeout, implicitWaitTimeout, scriptTimeout, htmlGrabber);
        }
    }
}
