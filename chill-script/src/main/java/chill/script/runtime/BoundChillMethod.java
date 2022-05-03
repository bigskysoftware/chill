package chill.script.runtime;

import chill.script.types.ChillMethod;
import chill.script.types.ChillProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoundChillMethod {

    private final Object owner;
    private final ChillMethod method;

    public BoundChillMethod(Object root, ChillMethod method) {
        this.owner = root;
        this.method = method;
    }

    public Object getOwner() {
        return owner;
    }

    public ChillMethod getMethod() {
        return method;
    }

    public Object invoke(Object... args) {
        return invoke(Arrays.asList(args));
    }

    public Object invoke(List<Object> argValues) {
        return method.invoke(owner, argValues);
    }
}
