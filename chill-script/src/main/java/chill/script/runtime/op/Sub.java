package chill.script.runtime.op;

import chill.script.runtime.ChillScriptRuntime;

public interface Sub {
    Object sub(ChillScriptRuntime runtime, Object right);
}
