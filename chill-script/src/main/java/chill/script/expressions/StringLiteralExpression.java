package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;

public class StringLiteralExpression extends Expression {
    private final Token token;

    public StringLiteralExpression(Token token) {
        this.token = token;
        setToken(token);
    }

    @Override
    public String evaluate(ChillScriptRuntime runtime) {
        return token.getStringValue();
    }

    public static Expression parse(ChillScriptParser parser) {
        if (parser.match(TokenType.STRING)) {
            Token token = parser.consumeToken();
            return new StringLiteralExpression(token);
        }
        return null;
    }
}