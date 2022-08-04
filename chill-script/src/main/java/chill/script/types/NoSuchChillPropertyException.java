package chill.script.types;

import chill.script.expressions.Expression;

public class NoSuchChillPropertyException extends RuntimeException {
    public NoSuchChillPropertyException(Expression expr, Object root, String propName, ChillType runTimeType) {
        super("Could not find property " + propName + " on " + root + " of type " +
                runTimeType.getDisplayName() + " in\n\n" +
                expr.getLineSource() + "\n\n" +
                expr.getSourceLocation() + "\n\n");

    }
}
