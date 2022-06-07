package chill.m8.parsetree.expressions;

import chill.m8.runtime.ChillTestRuntime;
import chill.script.expressions.Expression;
import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import org.openqa.selenium.By;

public class BodyExpression extends Expression {

    public BodyExpression(Token token) {
        setToken(token);
    }

    @Override
    public Object evaluate(ChillScriptRuntime runtime) {
        ChillTestRuntime chillTests = (ChillTestRuntime) runtime;
        return chillTests.getDriver().findElement(By.tagName("body"));
    }

    public static Expression parse(ChillScriptParser chillTestsParser) {
        if (chillTestsParser.match("body")) {
            return new BodyExpression(chillTestsParser.consumeToken());
        }
        return null;
    }
}
