package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;

public class BooleanLiteralExpression extends Expression {
    final Token value;

    public BooleanLiteralExpression(Token value) {
        this.value = value;
    }

    @Override
    public Object evaluate(ChillScriptRuntime runtime) {
        return value.getStringValue().equals("true");
    }

    public static Expression parse(ChillScriptParser parser) {
        if (parser.match("true", "false")) {
            Token token = parser.consumeToken();
            BooleanLiteralExpression expression = new BooleanLiteralExpression(token);
            expression.setToken(token);
            return expression;
        } else {
            return null;
        }
    }
}
