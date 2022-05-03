package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;

public class StringLiteralExpression extends Expression {
    private final String stringValue;

    public StringLiteralExpression(String value) {
        this.stringValue = value;
    }

    @Override
    public Object evaluate(ChillScriptRuntime runtime) {
        return stringValue;
    }

    public static Expression parse(ChillScriptParser parser) {
        if (parser.match(TokenType.STRING)) {
            Token token = parser.consumeToken();
            var expr = new StringLiteralExpression(token.getStringValue());
            expr.setToken(token);
            return expr;
        }
        return null;
    }
}