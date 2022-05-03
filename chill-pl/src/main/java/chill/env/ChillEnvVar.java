package chill.env;

import chill.config.ChillApp;
import chill.utils.ChillLogs;
import org.tomlj.TomlTable;

import java.util.Arrays;
import java.util.stream.Collectors;

import static chill.utils.TheMissingUtils.safely;

public class ChillEnvVar<T> {

    public static ChillLogs.LogCategory LOG = ChillLogs.get(ChillEnvVar.class);

    // bootstrap mode
    private final String name;
    private final Class<T> type;
    private T defaultValue;
    private boolean valueSet;
    private T value = null;
    private String source;
    private String manualStackTrace;
    private String manualComment;

    protected ChillEnvVar(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }

    public ChillEnvVar<T> withDefault(T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }


    public T get() {
        return value;
    }

    public void setManualValue(T value, String comment) {
        this.value = value;
        this.valueSet = true;
        this.source = "Manually Set";
        this.manualStackTrace = Arrays.stream(new RuntimeException().getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n"));
        this.manualComment = comment;
    }

    public T require() {
        if (!valueSet) {
            throw new IllegalStateException("No values for environment variable " + name + " was set, and it is required");
        }
        return value;
    }

    public boolean isSet() {
        return valueSet;
    }

    public void initialize(ChillApp cmdLine) {
        LOG.debug("Initializing {}", this);

        // if the value has been set pre-initialization, do not overwrite
        if (valueSet) {
            LOG.debug("{} already set manually, exiting", this);
            return;
        }

        // first resolve from the command line object
        String fromCmdLine = resolveFromCmdLine(cmdLine);
        if (fromCmdLine != null) {
            this.source = "Command Line";
            this.value = convertFromString(fromCmdLine);
            this.valueSet = true;
            return;
        }

        // next resolve via environment variable
        String fromEnv = System.getenv(name);
        if (fromEnv != null) {
            this.source = "Environment Variable";
            this.value = convertFromString(fromEnv);
            this.valueSet = true;
            return;
        }

        var tomlFile = ChillEnv.INSTANCE.getToml();
        if (tomlFile != null) {

            // next via environment-specific TOML
            String mode = ChillEnv.INSTANCE.getMode();
            if (tomlFile.isTable(mode)) {
                TomlTable table = tomlFile.getTable(mode);
                Object o = table.get(name);
                if (o != null) {
                    this.source = "TOML file (environment specific: " + mode + ")";
                    this.value = convertFromString(String.valueOf(o));
                    this.valueSet = true;
                    return;
                }
            }

            // top level TOML file
            Object o = tomlFile.get(name);
            if (o != null) {
                this.source = "TOML file";
                this.value = convertFromString(String.valueOf(o));
                this.valueSet = true;
                return;
            }
        }

        if (this.defaultValue != null) {
            this.value = this.defaultValue;
            this.source = "Default Value";
        }
    }

    private T convertFromString(String strValue) {
        if (type.equals(Boolean.class)) {
            return (T) Boolean.valueOf(strValue);
        }
        if (type.equals(String.class)) {
            return (T) strValue;
        }
        if (type.equals(Integer.class)) {
            return (T) Integer.valueOf(strValue);
        }
        if (type.isEnum()) {
            return (T) safely(() -> type.getMethod("valueOf", String.class).invoke(strValue));
        }
        throw new IllegalStateException("Don't know how to convert a string to " + type.getName());
    }

    private String resolveFromCmdLine(ChillApp cmdLine) {
        // TODO implement
        return null;
    }

    @Override
    public String toString() {
        return name + "=" + value + sourceStr();
    }

    private String sourceStr() {
        if (source != null) {
            return " (source:" + source + ")";
        } else {
            return " <unset>";
        }
    }
}
