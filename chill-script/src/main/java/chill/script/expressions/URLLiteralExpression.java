package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;

import java.net.MalformedURLException;
import java.net.URL;

public class URLLiteralExpression extends Expression {

    private final Token token;
    private URL value;

    public URLLiteralExpression(Token token) {
        this.token = token;
        setToken(token);
    }

    @Override
    public URL evaluate(ChillScriptRuntime chillTests) {
        if (value == null) {
            try {
                value = new URL(token. getStringValue());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return value;
    }

    public static Expression parse(ChillScriptParser parser) {
        if (parser.match(TokenType.ABSOLUTE_URL)) {
            return new URLLiteralExpression(parser.consumeToken());
        }
        return null;
    }
}