package chill.script.types;

import java.util.List;

public interface ChillType {
    ChillMethod getMethod(String propName);
    ChillProperty getProperty(String propName);
    String getDisplayName();
    List<ChillMethod> getMethods();
    List<ChillProperty> getProperties();
    List<ChillMethod> getDeclaredMethods();
    List<ChillProperty> getDeclaredProperties();
    Class getBackingClass();
}
