package autom8.parsetree.expressions.html;

import chill.script.expressions.Expression;
import chill.script.parser.ChillScriptParser;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;
import chill.utils.NiceList;


public class HtmlTagExpression extends Expression {
    private Token tagName;

    public HtmlTagExpression() {}

    public Token getTagName() {
        return tagName;
    }

    public void setTagName(Token tagName) {
        this.tagName = tagName;
    }

    static HtmlTagExpression parse(ChillScriptParser parser) {
        var a = parser.matchAndConsume("a");
        if (!parser.match("<")) {
            if (a) parser.produceToken();
            return null;
        }

        HtmlTagExpression expression = new HtmlTagExpression();
        expression.setStart(parser.consumeToken());

        if (parser.match(TokenType.SYMBOL) || parser.match(TokenType.STAR)) {
            expression.setTagName(parser.consumeToken());
        } else {
            expression.addError(parser.consumeToken(), "Expected tag name");
        }

        var endToken = parser.require(">", expression, "Expected closing tag");
        expression.setEnd(endToken);

        return expression;
    }
}
