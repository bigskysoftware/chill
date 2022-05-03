package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.BoundChillMethod;
import chill.script.runtime.UninvokableException;
import chill.script.tokenizer.Token;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.TokenType;
import chill.script.types.*;
import chill.utils.Pair;
import chill.utils.TheMissingUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

public class MethodCallExpression extends Expression {

    private Expression root;
    private List<Expression> args;

    public void setRoot(Expression root) {
        this.root = root;
    }

    public void setArgs(List<Expression> args) {
        this.args = addChildren(args);
    }

    @Override
    public Object evaluate(ChillScriptRuntime runtime) {
        Object rootVal = root.evaluate(runtime);
        if (rootVal == null) {
            throw new NullPointerException("The expression " + root.getSource() + " returned null");
        } else {
            var argValues = new ArrayList<>(args.size());
            for (Expression arg : args) {
                argValues.add(arg.evaluate(runtime));
            }

            if(rootVal instanceof BoundChillMethod boundMethod){
                return boundMethod.invoke(argValues);
            } else if(rootVal instanceof ChillJavaMethod unboundMethod){
                return unboundMethod.invoke(null, argValues);
            } else if(rootVal instanceof Runnable runnable){
                runnable.run();
                return null;
            } else if(rootVal instanceof Callable callable){
                try {
                    return callable.call();
                } catch (Exception e) {
                    throw TheMissingUtils.forceThrow(e);
                }
            } else {
                throw new UninvokableException("The expression " + root.getSource() + " returned " + root + " which I don't know how to invoke!");
            }
        }
    }

    public static MethodCallExpression parse(Pair<ChillScriptParser, Expression> parserAndRoot) {
        ChillScriptParser parser = parserAndRoot.first;
        Expression root = parserAndRoot.second;
        if (parser.matchAndConsume(TokenType.LEFT_PAREN) ) {

            // mark root as favoring methods
            if (root instanceof CanFavorMethods canFavorMethods) {
                canFavorMethods.favorMethods();
            }

            MethodCallExpression mce = new MethodCallExpression();
            mce.setRoot(root);
            var args = new LinkedList<Expression>();
            if (!parser.match(TokenType.RIGHT_PAREN) && parser.moreTokens()) {
                do {
                    var arg = parser.parse("expression");
                    args.add(arg);
                } while (parser.matchAndConsume(TokenType.COMMA));
            }
            mce.setArgs(args);
            Token closeParen = parser.require(TokenType.RIGHT_PAREN, mce, "Expected close paren here!");
            mce.setEnd(closeParen);
            return mce;
        }
        return null;
    }

}