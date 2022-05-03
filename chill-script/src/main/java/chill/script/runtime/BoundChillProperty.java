package chill.script.runtime;

import chill.script.expressions.Expression;
import chill.script.types.ChillProperty;

public class BoundChillProperty implements Gettable {

    private final Object owner;
    private final ChillProperty property;

    public BoundChillProperty(Object root, ChillProperty property) {
        this.owner = root;
        this.property = property;
    }

    public Object getOwner() {
        return owner;
    }

    public ChillProperty getProperty() {
        return property;
    }

    public Object get() {
        return property.get(owner);
    }

    public void set(Object val) {
        property.set(owner, val);
    }
}
