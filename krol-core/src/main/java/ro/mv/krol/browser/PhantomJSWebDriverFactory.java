package ro.mv.krol.browser;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Created by mihai.vaduva on 09/08/2016.
 */
@Singleton
public class PhantomJSWebDriverFactory implements WebDriverFactory {

    private final Capabilities capabilities;

    @Inject
    public PhantomJSWebDriverFactory(@Named(CONST_CAPABILITIES) Capabilities capabilities) {
        this.capabilities = capabilities;
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
