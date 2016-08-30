package ro.mv.krol.browser;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;
import ro.mv.krol.util.Args;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URL;
import java.util.Map;

/**
 * Created by mihai.vaduva on 09/08/2016.
 */
@Singleton
public class RemoteWebDriverFactory extends AbstractWebDriverFactory {

    public static final String CONST_REMOTE_ADDRESS = "selenium.remoteAddress";

    private final URL remoteAddress;

    @Inject
    public RemoteWebDriverFactory(@Named(CONST_REMOTE_ADDRESS) URL remoteAddress,
                                  @Named(CONST_CAPABILITIES) Map<String, Object> rawCapabilities) {
        super(Args.notEmpty(rawCapabilities, "capabilities"));
        this.remoteAddress = Args.notNull(remoteAddress, "remoteAddress");
    }

    @Override
    public WebDriver newDriver() throws WebDriverException {
        WebDriver driver = new RemoteWebDriver(remoteAddress, capabilities);
        return new Augmenter().augment(driver);
    }
}
