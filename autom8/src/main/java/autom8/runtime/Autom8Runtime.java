package autom8.runtime;

import chill.script.runtime.ChillScriptRuntime;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;
import java.util.Map;

public class Autom8Runtime extends ChillScriptRuntime {

    private ChromeDriver driver;
    private final Map<String, byte[]> _screenshots;

    public Autom8Runtime() {
        _screenshots = new HashMap<>();
    }

    @Override
    public void onStart() {
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
    }

    public void onFinish() {
        driver.close();
    }

    public void takeScreenshot(String name) {
        _screenshots.put(name, driver.getScreenshotAs(OutputType.BYTES));
    }

    public Map<String, byte[]> getScreenshots() {
        return _screenshots;
    }

    public WebDriver getDriver() {
        return driver;
    }
}
