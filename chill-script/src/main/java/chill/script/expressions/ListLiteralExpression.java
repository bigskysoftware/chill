package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;
import chill.script.parser.ErrorType;
import chill.utils.NiceList;

import java.util.LinkedList;
import java.util.List;

public class ListLiteralExpression extends Expression {

    private List<Expression> values;

    public void setValues(List<Expression> values) {
        this.values = addChildren(values);
    }

    @Override
    public Object evaluate(ChillScriptRuntime runtime) {
        NiceList<Object> returnList = new NiceList<>();
        for (Expression value : values) {
            returnList.add(value.evaluate(runtime));
        }
        return returnList;
    }

    public static Expression parse(ChillScriptParser parser) {
        if (parser.match(TokenType.LEFT_BRACKET)) {
            Token start = parser.consumeToken();
            var expr = new ListLiteralExpression();
            List<Expression> values = new LinkedList<>();
            if (!parser.match(TokenType.RIGHT_BRACKET)) {
                do {
                    values.add(parser.parse("expression"));
                } while (parser.matchAndConsume(TokenType.COMMA) && parser.moreTokens());
            }
            expr.setValues(values);
            expr.setStart(start);
            expr.setEnd(parser.require(TokenType.RIGHT_BRACKET, expr, ErrorType.UNTERMINATED_LIST.toString()));
            return expr;
        }
        return null;
    }
}