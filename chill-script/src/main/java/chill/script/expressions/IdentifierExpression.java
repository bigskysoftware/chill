package chill.script.expressions;

import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;
import chill.script.parser.ChillScriptParser;
import chill.script.types.ChillProperty;
import chill.script.types.ChillType;

public class IdentifierExpression extends Expression {

    private final Token identifier;

    public IdentifierExpression(Token token) {
        this.identifier = token;
    }

    @Override
    public Object evaluate(ChillScriptRuntime runtime) {
        Object symbol = runtime.getSymbol(identifier.getStringValue());
        if (symbol == ChillScriptRuntime.UNDEFINED) {
            ChillType type = runtime.resolveType(getName());
            return type;
        } else if (symbol instanceof ChillProperty prop) {
            return prop.get(null); // TODO - support "this" notion?
        } else {
            return symbol;
        }
    }

    public String getName() {
        return identifier.getStringValue();
    }

    public static Expression parse(ChillScriptParser parser) {
        if (parser.match(TokenType.SYMBOL)) {
            Token start = parser.consumeToken();
            var expr = new IdentifierExpression(start);
            expr.setToken(start);
            return expr;
        }
        return null;
    }
}