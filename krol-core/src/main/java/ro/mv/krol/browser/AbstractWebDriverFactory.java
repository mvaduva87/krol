package ro.mv.krol.browser;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.Map;

/**
 * Created by mihai on 8/19/16.
 */
public abstract class AbstractWebDriverFactory implements WebDriverFactory {

    final Capabilities capabilities;

    public AbstractWebDriverFactory(Capabilities capabilities) {
        this.capabilities = capabilities;
    }

    protected AbstractWebDriverFactory(Map<String, Object> rawMap) {
        if (rawMap == null || rawMap.isEmpty()) {
            capabilities = null;
        } else {
            capabilities = new DesiredCapabilities(rawMap);
        }
    }
}
