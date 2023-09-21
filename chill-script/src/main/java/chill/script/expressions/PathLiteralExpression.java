package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;

public class PathLiteralExpression extends Expression {

    private final Token token;

    public PathLiteralExpression(Token token) {
        this.token = token;
        setToken(token);
    }

    @Override
    public String evaluate(ChillScriptRuntime chillTests) {
        return token.getStringValue();
    }

    public static Expression parse(ChillScriptParser parser) {
        if (parser.match(TokenType.PATH)) {
            return new PathLiteralExpression(parser.consumeToken());
        }
        return null;
    }
}