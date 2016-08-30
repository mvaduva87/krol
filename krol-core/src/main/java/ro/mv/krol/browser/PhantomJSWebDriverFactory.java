package ro.mv.krol.browser;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Map;

/**
 * Created by mihai.vaduva on 09/08/2016.
 */
@Singleton
public class PhantomJSWebDriverFactory extends AbstractWebDriverFactory {

    @Inject
    public PhantomJSWebDriverFactory(@Named(CONST_CAPABILITIES) Map<String, Object> rawCapabilities) {
        super(rawCapabilities);
    }

    @Override
    public WebDriver newDriver() throws WebDriverException {
        if (capabilities != null) {
            return new PhantomJSDriver(capabilities);
        } else {
            return new PhantomJSDriver();
        }
    }
}
