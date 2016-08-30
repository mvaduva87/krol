package ro.mv.krol.exception;

/**
 * Created by mihai.vaduva on 8/7/16.
 */
public class ScriptCompileException extends CrawlException {

    public ScriptCompileException(Object object, Throwable t) {
        super("failed to compile script from: " + object, t);
    }

}
