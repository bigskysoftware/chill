package chill.m8.parsetree.expressions;

import chill.m8.runtime.ChillTestRuntime;
import chill.script.expressions.Expression;
import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import chill.utils.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

public class InputPostfixExpression extends Expression {

    private Expression root;

    public InputPostfixExpression(Token token, Expression root) {
        setToken(token);
        this.root = addChild(root);
    }

    @Override
    public Object evaluate(ChillScriptRuntime runtime) {
        ChillTestRuntime chillTests = (ChillTestRuntime) runtime;
        WebDriver driver = chillTests.getDriver();
        Object value = root.evaluate(runtime);
        String idOrName = String.valueOf(value);
        try {
            return driver.findElement(By.id(idOrName));
        } catch (NoSuchElementException e) {
            try {
                return driver.findElement(By.name(idOrName));
            } catch (NoSuchElementException e2) {
                return null;
            }
        }
    }

    public static Expression parse(Pair<ChillScriptParser, Expression> parserAndRoot) {
        ChillScriptParser parser = parserAndRoot.first;
        if (parser.match("input")) {
            return new InputPostfixExpression(parser.consumeToken(), parserAndRoot.second);
        }
        return null;
    }
}
