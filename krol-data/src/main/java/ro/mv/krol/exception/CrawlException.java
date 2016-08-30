package ro.mv.krol.exception;

/**
 * Created by mihai.vaduva on 8/7/16.
 */
public class CrawlException extends Exception {

    public CrawlException(String message) {
        super(message);
    }

    public CrawlException(String message, Throwable cause) {
        super(message, cause);
    }
}
