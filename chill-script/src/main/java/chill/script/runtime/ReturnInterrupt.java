package chill.script.runtime;

public class ReturnInterrupt extends RuntimeException {
    final Object value;

    public ReturnInterrupt(Object value) {
        this.value = value;
    }

    public ReturnInterrupt() {
        this.value = null;
    }

    public Object getValue() {
        return value;
    }
}
