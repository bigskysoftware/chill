package chill.script.runtime;

public class ReturnInterrupt extends RuntimeException {
    final Object value;

    public ReturnInterrupt(Object constant) {
        this.value = constant;
    }

    public ReturnInterrupt() {
        this.value = null;
    }

    public Object getValue() {
        return value;
    }
}
