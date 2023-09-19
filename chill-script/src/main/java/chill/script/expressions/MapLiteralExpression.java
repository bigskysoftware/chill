package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.TokenType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MapLiteralExpression extends Expression {
    HashMap<Expression, Expression> fields = new HashMap<>();

    public MapLiteralExpression() {}

    void addField(Expression key, Expression value) {
        fields.put(key, value);
        addChild(key);
        addChild(value);
    }

    @Override
    public Object evaluate(ChillScriptRuntime runtime) {
        HashMap<String, Object> rv = new HashMap<>();
        for (var entry : fields.entrySet()) {
            Object key = entry.getKey().evaluate(runtime);
            Object value = entry.getValue().evaluate(runtime);
            rv.put(String.valueOf(key), value);
        }
        return rv;
    }

    public static MapLiteralExpression parse(ChillScriptParser parser) {
        if (parser.matchAndConsume(TokenType.LEFT_BRACE)) {
            MapLiteralExpression expr = new MapLiteralExpression();
            expr.setStart(parser.lastMatch());

            while (parser.moreTokens() && !parser.match(TokenType.RIGHT_BRACE)) {
                Expression key = parser.parse("expression");
                parser.require(TokenType.COLON, expr, "expected a ':'");
                Expression value = parser.requireExpression(expr, "expression");
                expr.addField(key, value);
                if (parser.match(TokenType.COMMA)) parser.consumeToken();
            }
            parser.require(TokenType.RIGHT_BRACE, expr, "expected a '}'");

            expr.setEnd(parser.lastMatch());
            return expr;
        }
        return null;
    }
}
