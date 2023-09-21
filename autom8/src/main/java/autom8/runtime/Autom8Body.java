package autom8.runtime;

import chill.script.runtime.Container;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.function.Function;

public class Autom8Body implements Container {
    Autom8Runtime runtime;

    public Autom8Body(Autom8Runtime runtime) {
        this.runtime = runtime;
    }

    @Override
    public boolean contains(Object value) {
        WebDriver driver = runtime.getDriver();
        if (driver == null) return false;

        var body = driver.findElement(By.tagName("body"));
        return traverseHtml(body, (elt) -> {
            return elt.getText().contains(value.toString());
        });
    }

    static boolean traverseHtml(WebElement element, Function<WebElement, Boolean> callback) {
        if (callback.apply(element)) return true;
        for (var child : element.findElements(By.xpath("./*"))) {
            if (traverseHtml(child, callback)) return true;
        }
        return false;
    }
}
