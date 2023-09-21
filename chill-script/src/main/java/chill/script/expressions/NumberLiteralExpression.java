package chill.script.expressions;

import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;
import chill.script.parser.ChillScriptParser;

import java.math.BigDecimal;

public class NumberLiteralExpression extends Expression {
    private final BigDecimal value;

    public NumberLiteralExpression(String value) {
        this.value = new BigDecimal(value);
    }

    @Override
    public BigDecimal evaluate(ChillScriptRuntime runtime) {
        return value;
    }

    public static Expression parse(ChillScriptParser parser) {
        if (parser.match(TokenType.NUMBER)) {
            Token token = parser.consumeToken();
            var expr = new NumberLiteralExpression(token.getStringValue());
            expr.setToken(token);
            return expr;
        }
        return null;
    }
}