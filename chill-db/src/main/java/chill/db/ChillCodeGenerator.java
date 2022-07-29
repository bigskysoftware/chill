package chill.db;

import chill.utils.TheMissingUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static chill.utils.TheMissingUtils.*;

public class ChillCodeGenerator {

    protected static void generateCodeForMyPackage() {
        StackTraceElement trace[] = Thread.currentThread().getStackTrace();
        Class templateClass;
        if (trace.length > 0) {
            templateClass = safely(() -> Class.forName(trace[trace.length - 1].getClassName()));
        } else {
            throw new IllegalStateException("Could not determine class to code gen for!");
        }
        generateCodeForPackage(templateClass.getPackageName());
    }

    protected static void generateCodeForPackage(String packageName) {
        String packagePath = packageName.replaceAll("[.]", "/");
        InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(packagePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        nice(reader.lines())
                .filter(line -> line.endsWith(".class"))
                .sort()
                .map(line -> TheMissingUtils.getClass(packageName + "." + line.substring(0, line.lastIndexOf('.'))))
                .each(aClass -> {
                    if (ChillRecord.class.isAssignableFrom(aClass) && !Modifier.isAbstract(aClass.getModifiers())) {
                        codeGen(aClass);
                    }
                });
    }

    public static void codeGen(Class templateClass) {
        Object instance;
        instance = TheMissingUtils.newInstance(templateClass);
        String newLine = "\n    ";

        String className = templateClass.getSimpleName().replace("$", ".");

        StringBuilder sb = new StringBuilder()
                .append(newLine)
                .append("public static abstract class Abstract").append(templateClass.getSimpleName()).append(" extends ChillRecord {").append(newLine)
                .append(newLine);

        sb.append("protected final " + className + " self = (" + className + ") (Object) this;\n").append(newLine)
                .append(newLine);

        // createOrThrow()
        sb.append("public ").append(className).append(" createOrThrow(){").append(newLine)
                .append("  if(!create()){").append(newLine)
                .append("    throw new chill.db.ChillValidation.ValidationException(getErrors());")
                .append(newLine).append("  }").append(newLine)
                .append("  return self;").append(newLine)
                .append("}").append(newLine)
                .append(newLine);

        // saveOrThrow()
        sb.append("public ").append(className).append(" saveOrThrow(){").append(newLine)
                .append("  if(!save()){").append(newLine)
                .append("    throw new chill.db.ChillValidation.ValidationException(getErrors());")
                .append(newLine).append("  }").append(newLine)
                .append("  return self;").append(newLine)
                .append("}").append(newLine)
                .append(newLine);

        // firstOrCreateOrThrow()
        sb.append("public ").append(className).append(" firstOrCreateOrThrow(){").append(newLine)
                .append("  return (").append(className).append(") firstOrCreateImpl();").append(newLine)
                .append("}").append(newLine)
                .append(newLine);

        // fromWebParams()
        sb.append("public ")
                .append(className).append(" fromWebParams(java.util.Map<String, String> values, String... params) {").append(newLine)
                .append("  ChillRecord.populateFromWebParams(self, values, params);").append(newLine)
                .append("  return self;").append(newLine)
                .append("}").append(newLine)
                .append(newLine);

        java.lang.reflect.Field[] classFields = templateClass.getDeclaredFields();
        for (java.lang.reflect.Field javaField : classFields) {
            if (ChillField.Many.class.isAssignableFrom(javaField.getType())) {
                javaField.setAccessible(true);
                ChillField.Many chillField = (ChillField.Many) safely(() -> javaField.get(instance));
                String propName = TheMissingUtils.capitalize(javaField.getName());
                sb.append("public ")
                        .append(chillField.getTypeName()).append(" get")
                        .append(propName).append("() {").append(newLine)
                        .append("  return self.").append(javaField.getName()).append(".get();").append(newLine)
                        .append("}").append(newLine)
                        .append(newLine);
            } else if (ChillField.class.isAssignableFrom(javaField.getType())) {
                javaField.setAccessible(true);
                ChillField chillField = (ChillField) safely(() -> javaField.get(instance));

                String propName = TheMissingUtils.capitalize(javaField.getName());

                String getterPrefix = chillField.isBoolean() ? "is" : "get";
                sb.append("public ")
                        .append(chillField.getTypeName()).append(" " + getterPrefix)
                        .append(propName).append("() {").append(newLine)
                        .append("  return self.").append(javaField.getName()).append(".get();").append(newLine)
                        .append("}").append(newLine)
                        .append(newLine);

                if (!chillField.isReadOnly()) {
                    sb.append("public").append(" void set")
                            .append(propName)
                            .append("(").append(chillField.getTypeName()).append(" ")
                            .append(javaField.getName()).append(") {").append(newLine)
                            .append("  self.").append(javaField.getName()).append(".set(").append(javaField.getName()).append(");").append(newLine)
                            .append("}").append(newLine)
                            .append(newLine);

                    sb.append("public ")
                            .append(className).append(" with")
                            .append(propName)
                            .append("(").append(chillField.getTypeName()).append(" ")
                            .append(javaField.getName()).append(") {").append(newLine)
                            .append("  set").append(propName).append("(").append(javaField.getName()).append(");").append("").append(newLine)
                            .append("  return self;").append(newLine)
                            .append("}").append(newLine)
                            .append(newLine);
                }
                if (chillField instanceof ChillField.FK) {
                    sb.append("public static chill.db.ChillQuery<")
                            .append(className).append("> for")
                            .append(chillField.getType().getSimpleName())
                            .append("(").append(chillField.getType().getSimpleName()).append(" ")
                            .append(javaField.getName()).append(") {").append(newLine)
                            .append("  return new ").append(className).append("().").append(javaField.getName()).append(".reverse(").append(javaField.getName()).append(");").append(newLine)
                            .append("}").append(newLine)
                            .append(newLine);
                }
                if (chillField.isPassword()) {
                    sb.append("@chill.db.ChillRecord.Generated ")
                            .append("public boolean ")
                            .append(javaField.getName())
                            .append("Matches")
                            .append("(String passwd) {").append(newLine)
                            .append("  return self.").append(javaField.getName()).append(".passwordMatches(passwd);").append(newLine)
                            .append("}").append(newLine)
                            .append(newLine);
                }
            }
        }
        sb.append("public static final chill.db.ChillRecord.Finder<").append(className).append("> find = finder(").append(className).append(".class);").append(newLine)
                .append(newLine);

        sb.append("}").append(newLine);

        System.out.println(sb.toString());
    }
}
