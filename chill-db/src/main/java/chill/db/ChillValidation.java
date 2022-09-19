package chill.db;

import chill.utils.TheMissingUtils;

import java.util.*;

import static chill.utils.TheMissingUtils.safely;

public class ChillValidation<T> {

    private final Validator<T> validator;
    private final String errorMessage;

    public ChillValidation(Validator<T> validator, String errorMessage) {
        this.validator = validator;
        this.errorMessage = errorMessage;
    }

    public static boolean validEmail(String str) {
        return str != null && str.matches("^[^@]+@[^@]+\\.[^@]+$");
    }

    public void validate(Errors errors, ChillField<T> field){
        T value = field.get();
        if (!validator.validate(value)) {
            errors.addError(field, String.format(errorMessage, field.getColumnName(), value));
        }
    }

    public interface Validator<T> {
        public boolean validate(T val);
    }

    public static class Errors {

        ChillRecord record;
        Map<ChillField, List<String>> errors = new LinkedHashMap<>();

        public Errors(ChillRecord record) {
            this.record = record;
        }

        public void addError(ChillField field, String formatted) {
            List<String> errorStrings = errors.computeIfAbsent(field, field1 -> new LinkedList<>());
            errorStrings.add(formatted);
        }

        public boolean hasErrors() {
            return errors.size() > 0;
        }

        public List<String> getErrorsFor(String name) {
            try {
                java.lang.reflect.Field javaField = record.getClass().getDeclaredField(name);
                javaField.setAccessible(true);
                return errors.get(javaField.get(record));
            } catch (Exception e) {
                throw TheMissingUtils.forceThrow(e);
            }
        }

        public String toErrorString() {
            var sb = new StringBuilder("The Following validation errors were found on ");
            sb.append(record.toString()).append("\n\n");
            for (ChillField field : errors.keySet()) {
                sb.append("  field ").append(field.getColumnName()).append(":").append("\n");
                List<String> errors = this.errors.get(field);
                for (String error : errors) {
                    sb.append("    - ").append(error).append("\n");
                }
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    public static class ValidationException extends RuntimeException {
        public ValidationException(Errors errors) {
            super(errors.toErrorString());
        }
    }
}
