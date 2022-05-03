package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;

import java.net.MalformedURLException;
import java.net.URL;

public class URLLiteralExpression extends Expression {

    private final String _value;

    public URLLiteralExpression(Token token) {
        _value = token.getStringValue();
        setToken(token);
    }

    @Override
    public Object evaluate(ChillScriptRuntime chillTests) {
        try {
            return new URL(_value);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Expression parse(ChillScriptParser parser) {
        if (parser.match(TokenType.ABSOLUTE_URL)) {
            return new URLLiteralExpression(parser.consumeToken());
        }
        return null;
    }
}