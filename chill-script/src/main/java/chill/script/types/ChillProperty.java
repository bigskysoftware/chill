package chill.script.types;

public interface ChillProperty {
    String getName();
    String getCanonicalName();
    Object get(Object root);
    void set(Object owner, Object val);
    boolean isStatic();
    ChillType getType();

}
