package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;

import static chill.script.tokenizer.TokenType.LEFT_PAREN;
import static chill.script.tokenizer.TokenType.RIGHT_PAREN;

public class ParenthesizedExpression extends Expression {

    private Expression body;

    @Override
    public Object evaluate(ChillScriptRuntime runtime) {
        return body.evaluate(runtime);
    }

    public static Expression parse(ChillScriptParser parser) {
        if (parser.match(LEFT_PAREN)) {
            ParenthesizedExpression parenthesizedExpression = new ParenthesizedExpression();
            parenthesizedExpression.setStart(parser.consumeToken());
            Expression expression = parser.parse("expression");
            parenthesizedExpression.setBody(expression);
            Token rightParen = parser.require(RIGHT_PAREN, parenthesizedExpression, "Expected a close paren");
            parenthesizedExpression.setEnd(rightParen);
            return parenthesizedExpression;
        }
        return null;
    }

    private void setBody(Expression expression) {
        this.body = addChild(expression);
    }
}
