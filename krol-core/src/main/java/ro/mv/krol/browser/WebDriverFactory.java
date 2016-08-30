package ro.mv.krol.browser;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

/**
 * Created by mihai.vaduva on 8/7/16.
 */
public interface WebDriverFactory {

    String CONST_CAPABILITIES = "selenium.capabilities";

    WebDriver newDriver() throws WebDriverException;

}
