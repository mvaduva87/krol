package ro.mv.krol.browser;

import ro.mv.krol.exception.CrawlException;
import ro.mv.krol.util.Args;
import ro.mv.krol.util.HtmlUtils;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * Created by mihai.vaduva on 14/05/2016.
 */
@Singleton
public class HtmlGrabber {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final Charset defaultCharset;

    @Inject
    public HtmlGrabber(@Named("defaultCharset") Charset defaultCharset) {
        this.defaultCharset = Args.notNull(defaultCharset, "defaultCharset");
    }

    public HtmlGrabber() {
        this(Charset.forName("UTF-8"));
    }

    public HtmlPage grab(WebDriver driver) throws CrawlException, WebDriverException {
        HtmlPage.Builder builder = new HtmlPage.Builder();
        grab(driver, builder);
        return builder.build();
    }

    public void grab(WebDriver driver, HtmlPage.Builder builder) throws CrawlException, WebDriverException {
        Date captureTimestamp = new Date();
        String source = grabHtml(driver);
        byte[] screenshot = takeScreenshot(driver);
        Charset charset = extractCharsetFrom(source);
        URL url;
        String currentUrl = driver.getCurrentUrl();
        try {
            url = new URL(currentUrl);
        } catch (MalformedURLException e) {
            throw new CrawlException("failed to parse currentUrl: " + currentUrl, e);
        }
        builder.withUrl(url)
                .withTimestamp(captureTimestamp)
                .withSource(source)
                .withCharset(charset)
                .withScreenshot(screenshot);
    }

    protected String grabHtml(WebDriver driver) throws WebDriverException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        return (String) js.executeScript("return document.documentElement.outerHTML");
    }

    protected byte[] takeScreenshot(WebDriver driver) throws WebDriverException {
        if (driver instanceof TakesScreenshot) {
            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            return takesScreenshot.getScreenshotAs(OutputType.BYTES);
        }
        return null;
    }

    protected Charset extractCharsetFrom(String source) {
        Charset charset = null;
        try {
            charset = HtmlUtils.getCharset(source);
        } catch (RuntimeException e) {
            logger.warn("failed to extract charset", e);
        }
        if (charset == null) {
            logger.warn("no charset found -> using default charset " + defaultCharset.displayName());
            charset = defaultCharset;
        }
        return charset;
    }
}
