package chill.script.types;

import chill.script.expressions.Expression;

import java.util.List;

public interface ChillMethod {
    Object invoke(Object rootVal, List<Object> args);
    String getName();
    String getDisplayName();
    boolean isStatic();
}
