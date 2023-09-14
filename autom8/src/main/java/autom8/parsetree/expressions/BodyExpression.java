package autom8.parsetree.expressions;

import autom8.runtime.Autom8Runtime;
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
        Autom8Runtime chillTests = (Autom8Runtime) runtime;
        return chillTests.getDriver().findElement(By.tagName("body"));
    }

    public static Expression parse(ChillScriptParser chillTestsParser) {
        var the = chillTestsParser.matchAndConsume("the");
        if (chillTestsParser.match("body")) {
            return new BodyExpression(chillTestsParser.consumeToken());
        } else if (the) {
            chillTestsParser.produceToken();
        }
        return null;
    }
}
