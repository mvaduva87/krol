package ro.mv.krol.browser;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;
import ro.mv.krol.util.Args;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URL;

/**
 * Created by mihai.vaduva on 09/08/2016.
 */
@Singleton
public class RemoteWebDriverFactory implements WebDriverFactory {

    public static final String CONST_REMOTE_ADDRESS = "selenium.remoteAddress";

    private final Capabilities capabilities;
    private final URL remoteAddress;

    @Inject
    public RemoteWebDriverFactory(@Named(CONST_REMOTE_ADDRESS) URL remoteAddress,
                                  @Named(CONST_CAPABILITIES) Capabilities capabilities) {
        this.capabilities = Args.notNull(capabilities, "capabilities");
        this.remoteAddress = Args.notNull(remoteAddress, "remoteAddress");
    }

    @Override
    public WebDriver newDriver() throws WebDriverException {
        WebDriver driver = new RemoteWebDriver(remoteAddress, capabilities);
        return new Augmenter().augment(driver);
    }
}
