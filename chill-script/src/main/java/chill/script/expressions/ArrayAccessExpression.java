package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.TokenType;
import chill.script.types.ChillProperty;
import chill.script.types.ChillType;
import chill.script.types.TypeSystem;
import chill.utils.Pair;

import java.util.List;
import java.util.Map;

public class ArrayAccessExpression extends Expression {

    private Expression root;
    private Expression index;

    @Override
    public Object evaluate(ChillScriptRuntime runtime) {
        Object rootVal = root.evaluate(runtime);
        Object indexVal = index.evaluate(runtime);
        if (rootVal == null) {
            return null;
        } else if (rootVal instanceof Map<?, ?>) {
            Map rootAsMap = (Map) rootVal;
            return rootAsMap.get(indexVal);
        } else if (rootVal instanceof List<?>) {
            List rootAsList = (List) rootVal;
            int index = makeInt(indexVal);
            if (index < 0 || rootAsList.size() <= index) {
                return null;
            }
            return rootAsList.get(index);
        } else if (rootVal instanceof Object[]) {
            Object[] rootAsArr = (Object[]) rootVal;
            int index = makeInt(indexVal);
            if (index < 0 || rootAsArr.length <= index) {
                return null;
            }
            return rootAsArr[index];
        } else {
            ChillType runTimeType = TypeSystem.getRuntimeType(rootVal);
            String propName = String.valueOf(indexVal);
            ChillProperty property = runTimeType.getProperty(propName);
            if (property != null) {
                return property.get(rootVal);
            } else {
                return null;
            }
        }
    }

    private int makeInt(Object indexVal) {
        // TODO support more elaborate access
        if (indexVal instanceof Number) {
            Number numberVal = (Number) indexVal;
            return numberVal.intValue();
        } else {
            return Integer.valueOf(String.valueOf(indexVal));
        }
    }

    public static Expression parse(Pair<ChillScriptParser, Expression> parserAndRoot) {
        ChillScriptParser parser = parserAndRoot.first;
        if (parser.matchAndConsume(TokenType.LEFT_BRACKET)) {
            Expression root = parserAndRoot.second;
            ArrayAccessExpression arrayAccess = new ArrayAccessExpression();
            arrayAccess.setRoot(root);
            arrayAccess.setIndex(parser.requireExpression(arrayAccess, "expression"));
            parser.require(TokenType.RIGHT_BRACKET, arrayAccess, "Expected closing bracket");
            return arrayAccess;
        }
        return null;
    }

    private void setRoot(Expression root) {
        this.root = addChild(root);
    }

    private void setIndex(Expression expression) {
        index = expression;
    }

    public Expression getRoot() {
        return root;
    }
}