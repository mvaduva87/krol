import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import ro.mv.krol.browser.GrabberDelegate

/**
 * Created by mihai.vaduva on 04/09/2016.
 */
WebDriver driver = (WebDriver) getProperty("driver");
GrabberDelegate grabber = (GrabberDelegate) getProperty("grabber");

driver.findElement(By.cssSelector('button#linksBtn')).click();

By linkLocator = By.cssSelector("#linksContainer > ul > li > a");
new WebDriverWait(driver, 1)
        .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(linkLocator));

grabber.captureWith(["script-meta": "success"]);