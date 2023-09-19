package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;

public class ConstructorExpression extends Expression {
    private Expression type;

    public ConstructorExpression() {}

    public Expression getType() {
        return type;
    }

    public void setType(Expression type) {
        this.type = type;
    }

    @Override
    public Object evaluate(ChillScriptRuntime runtime) {
        Object value = type.evaluate(runtime);
        if (value instanceof Class<?> clazz) {
            try {
                return clazz.getConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("failed to create instance of " + clazz, e);
            }
        } else {
            throw new RuntimeException("i don't know how to make a " + value);
        }
    }

    public static ConstructorExpression parse(ChillScriptParser parser) {
        boolean a = parser.matchAndConsume("a");
        if (parser.matchAndConsume("new")) {
            ConstructorExpression expr = new ConstructorExpression();
            expr.setStart(parser.lastMatch());

            expr.setType(parser.requireExpression(expr, "identifier"));
            parser.require(TokenType.LEFT_PAREN, expr, "expected a '('");
            // todo: args
            parser.require(TokenType.RIGHT_PAREN, expr, "expected a ')'");

            expr.setEnd(parser.lastMatch());
            return expr;
        }

        if (a) {
            parser.produceToken();
        }

        return null;
    }
}
