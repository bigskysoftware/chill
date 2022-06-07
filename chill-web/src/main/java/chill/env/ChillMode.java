package chill.env;

public class ChillMode extends ChillEnvVar<ChillMode.Modes>{

    ChillMode() {
        super("MODE", ChillMode.Modes.class);
        withDefault(Modes.DEV);
    }

    public boolean isDev() {
        return get() == Modes.DEV;
    }

    public boolean isTest() {
        return get() == Modes.TEST;
    }

    public boolean isQA() {
        return get() == Modes.QA;
    }

    public boolean isStaging() {
        return get() == Modes.STAGING;
    }

    public boolean isProduction() {
        return get() == Modes.PRODUCTION;
    }

    public String stringValue() {
        return get().toString();
    }

    public static enum Modes {
        DEV,
        TEST,
        QA,
        STAGING,
        PRODUCTION
    }
}
