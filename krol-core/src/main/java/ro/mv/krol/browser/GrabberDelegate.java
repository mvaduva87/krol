package ro.mv.krol.browser;

import ro.mv.krol.exception.CrawlException;
import org.openqa.selenium.WebDriver;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by mihai.vaduva on 14/05/2016.
 */
public class GrabberDelegate {

    private final WebDriver driver;
    private final HtmlGrabber grabber;
    private final Consumer<HtmlPage> consumer;
    private boolean captured = false;

    public GrabberDelegate(WebDriver driver, HtmlGrabber grabber, Consumer<HtmlPage> consumer) {
        this.driver = driver;
        this.grabber = grabber;
        this.consumer = consumer;
    }

    public void capture() throws CrawlException {
        captureWith(null);
    }

    public void captureWith(Map<String, String> metadata) throws CrawlException {
        HtmlPage.Builder pageBuilder = HtmlPage.builder();
        grabber.grab(driver, pageBuilder);
        if (metadata != null) {
            pageBuilder.withMetadata(metadata);
        }
        HtmlPage htmlPage = pageBuilder.build();
        captured = true;
        consumer.accept(htmlPage);
    }

    public boolean hasCaptured() {
        return captured;
    }
}
