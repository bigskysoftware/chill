package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;

public class PathLiteralExpression extends Expression {

    private final String _value;

    public PathLiteralExpression(Token token) {
        _value = token.getStringValue();
        setToken(token);
    }

    @Override
    public Object evaluate(ChillScriptRuntime chillTests) {
        return _value;
    }

    public static Expression parse(ChillScriptParser parser) {
        if (parser.match(TokenType.PATH)) {
            return new PathLiteralExpression(parser.consumeToken());
        }
        return null;
    }
}