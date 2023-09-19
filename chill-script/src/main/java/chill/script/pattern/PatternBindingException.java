package chill.script.pattern;

public class PatternBindingException extends RuntimeException {
    public PatternBindingException() {
    }

    public PatternBindingException(String message) {
        super(message);
    }

    public PatternBindingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PatternBindingException(Throwable cause) {
        super(cause);
    }

    public PatternBindingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
