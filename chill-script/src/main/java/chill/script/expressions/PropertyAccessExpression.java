package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.BoundChillMethod;
import chill.script.runtime.Gettable;
import chill.script.tokenizer.Token;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.TokenType;
import chill.script.types.*;
import chill.utils.DynaProperties;
import chill.utils.Pair;

import java.util.Map;

import static chill.script.runtime.ChillScriptRuntime.UNDEFINED;


public class PropertyAccessExpression extends Expression implements CanFavorMethods {

    private Expression root;
    private Token property;
    private String propName;
    private boolean favorMethods;

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
            if (favorMethods) {
                ChillMethod method = runtimeType.getMethod(propName);
                if (method != null) {
                    return new BoundChillMethod(rootVal, method);
                }
            }

            ChillProperty property = runtimeType.getProperty(propName);
            if (property != null) {
                return property.get(rootVal);
            }

            if (rootVal instanceof Map) {
                Map mapInstance = (Map) rootVal;
                return mapInstance.get(this.property.getStringValue());
            }

            if (rootVal instanceof PropertyMissing) {
                PropertyMissing dynamicProperties = (PropertyMissing) rootVal;
                Object result = dynamicProperties.propertyMissing(propName);
                if (result != UNDEFINED) {
                    return result;
                }
            }

            if (rootVal instanceof PropertyMissing) {
                PropertyMissing dynamicProperties = (PropertyMissing) rootVal;
                Object result = dynamicProperties.propertyMissing(propName);
                if (result != UNDEFINED) {
                    return result;
                }
            }

            if (DynaProperties.hasDynaProps(rootVal)) {
                var dynaProps = DynaProperties.forObject(rootVal);
                if (dynaProps.containsKey(propName)) {
                    Object value = dynaProps.get(propName);
                    if (value instanceof Gettable) {
                        Gettable getter = (Gettable) value;
                        return getter.get();
                    } else {
                        return value;
                    }
                }
            }

            if (!favorMethods) {
                ChillMethod method = runtimeType.getMethod(propName);
                if (method != null) {
                    return new BoundChillMethod(rootVal, method);
                }
            }

            throw new NoSuchChillPropertyException(this, rootVal, propName, TypeSystem.getRuntimeType(rootVal));
        }
    }


    public static Expression parse(Pair<ChillScriptParser, Expression> parserAndRoot) {
        ChillScriptParser parser = parserAndRoot.first;
        if (parser.matchAndConsume(TokenType.DOT)) {
            Expression root = parserAndRoot.second;
            PropertyAccessExpression propAccess = new PropertyAccessExpression();
            Token propertyName = parser.require(TokenType.SYMBOL, propAccess, "Expected symbol here!");
            propAccess.setProperty(propertyName);
            propAccess.setRoot(root);
            propAccess.setEnd(propertyName);
            return propAccess;
        }
        return null;
    }

    private void setRoot(Expression root) {
        this.root = addChild(root);
    }

    public Expression getRoot() {
        return root;
    }

    @Override
    public void favorMethods() {
        favorMethods = true;
    }

    public String getPropertyName() {
        return propName;
    }
}