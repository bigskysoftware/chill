package chill.script.types;

import chill.script.expressions.Expression;

public interface ChillProperty {
    String getName();
    String getCanonicalName();
    Object get(Object root);
    void set(Object owner, Object val);
    boolean isStatic();
    ChillType getType();

}
