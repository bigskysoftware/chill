package chill.script.types;

import java.util.List;

public interface ChillMethod {
    Object invoke(Object rootVal, List<Object> args);
    String getName();
    String getDisplayName();
    boolean isStatic();
    boolean isPublic();

    boolean canInvokeWith(List<Object> args);
}
