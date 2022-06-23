package autom8.parsetree.expressions;

import autom8.runtime.Autom8Runtime;
import chill.script.expressions.Expression;
import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import chill.utils.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ButtonPostfixExpression extends Expression {

    private Expression root;

    public ButtonPostfixExpression(Token token, Expression root) {
        setToken(token);
        this.root = addChild(root);
    }

    @Override
    public Object evaluate(ChillScriptRuntime runtime) {
        Autom8Runtime chillTests = (Autom8Runtime) runtime;
        WebDriver driver = chillTests.getDriver();
        Object value = root.evaluate(chillTests);
        String idOrTextOrValue = String.valueOf(value);
        try {
            return driver.findElement(By.id(idOrTextOrValue));
        } catch (NoSuchElementException e) {
            String lowerCaseValue = idOrTextOrValue.toLowerCase();
            List<WebElement> buttons = driver.findElements(By.tagName("button"));
            for (WebElement element : buttons) {
                String normalizedValue = element.getText().toLowerCase().strip();
                if (normalizedValue.equals(lowerCaseValue) && element.isDisplayed() && element.isEnabled()) {
                    return element;
                }
            }
            List<WebElement> submits = driver.findElements(By.cssSelector("input[type='submit']"));
            for (WebElement element : submits) {
                String normalizedValue = element.getAttribute("value").toLowerCase().strip();
                if (normalizedValue.equals(lowerCaseValue)  && element.isDisplayed() && element.isEnabled()) {
                    return element;
                }
            }
        }
        return null;
    }

    public static Expression parse(Pair<ChillScriptParser, Expression> parserAndRoot) {
        ChillScriptParser parser = parserAndRoot.first;
        if (parser.match("button")) {
            return new ButtonPostfixExpression(parser.consumeToken(), parserAndRoot.second);
        }
        return null;
    }
}
