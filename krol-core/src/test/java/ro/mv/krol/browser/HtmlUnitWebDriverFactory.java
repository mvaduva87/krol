package ro.mv.krol.browser;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * Created by mihai.vaduva on 04/09/2016.
 */
public class HtmlUnitWebDriverFactory extends AbstractWebDriverFactory {

    public HtmlUnitWebDriverFactory() {
        super((Capabilities) null);
    }

    @Override
    public WebDriver newDriver() throws WebDriverException {
        return new HtmlUnitDriver(true);
    }
}
