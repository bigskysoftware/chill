package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.BoundChillMethod;
import chill.script.runtime.BoundChillProperty;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;
import chill.script.types.*;
import chill.utils.Pair;

public class PropertyReferenceExpression extends Expression {

    private Expression root;
    private Token property;
    private String propName;


    public void setProperty(Token token) {
        this.property = token;
        this.propName = token.getStringValue();
    }

    public Token getProperty() {
        return property;
    }

    @Override
    public Object evaluate(ChillScriptRuntime runtime) {
        Object rootVal = root.evaluate(runtime);
        if (rootVal == null) {
            return null;
        } else {
            ChillType runtimeType = TypeSystem.getRuntimeType(rootVal);
            ChillProperty property = runtimeType.getProperty(propName);
            if (property != null) {
                return new BoundChillProperty(rootVal, property);
            }

            ChillMethod method = runtimeType.getMethod(propName);
            if (method != null) {
                return new BoundChillMethod(rootVal, method);
            }

            throw new NoSuchChillPropertyException(this, rootVal, propName, TypeSystem.getRuntimeType(rootVal));
        }
    }


    public static Expression parse(Pair<ChillScriptParser, Expression> parserAndRoot) {
        ChillScriptParser parser = parserAndRoot.first;
        if (parser.match(TokenType.SHARP)) {
            Expression root = parserAndRoot.second;
            // sharp must be adjacent to the root expression
            if (root.getEnd().getEnd() == parser.currentToken().getStart()) {
                parser.consumeToken();
                PropertyReferenceExpression propAccess = new PropertyReferenceExpression();
                propAccess.setProperty(parser.require(TokenType.SYMBOL, propAccess, "Expected symbol here!"));
                propAccess.setRoot(root);
                return propAccess;
            }
        }
        return null;
    }

    private void setRoot(Expression root) {
        this.root = addChild(root);
    }

    public Expression getRoot() {
        return root;
    }
}