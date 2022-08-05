package chill.db;

import chill.utils.NiceList;
import chill.utils.TheMissingUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;

import static chill.utils.TheMissingUtils.*;

public class ChillCodeGenerator {

    private static final String CODE_GEN_BOUNDARY =
            "//=================== GENERATED CODE ========================";

    protected static void generateCodeForMyPackage() {
        StackTraceElement trace[] = Thread.currentThread().getStackTrace();
        Class templateClass;
        if (trace.length > 0) {
            templateClass = safely(() -> Class.forName(trace[trace.length - 1].getClassName()));
        } else {
            throw new IllegalStateException("Could not determine class to code gen for!");
        }
        String code = generateCodeForPackage(templateClass.getPackageName());
        if (!updateCodeInline(code)) {
            System.out.println("Unable to update the code inline, here is the generated code:\n\n" + code);
        }
    }

    private static boolean updateCodeInline(String code) {

        Path[] srcDir = new Path[1];
        try {
            Files.walkFileTree(Path.of(".").toAbsolutePath(), Collections.emptySet(), 2, new SimpleFileVisitor<>(){
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path fileName = dir.getFileName();
                    if (fileName.startsWith(".") && !dir.toFile().getName().equals(".")) {
                        return FileVisitResult.SKIP_SUBTREE;
                    } else {
                        return FileVisitResult.CONTINUE;
                    }
                }
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    File fileFile = file.toFile();
                    if (fileFile.getName().equals("src")) {
                        srcDir[0] = file;
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            if (srcDir[0] != null) {
                Path src = srcDir[0];
                System.out.println("Found source directory: " + src.toAbsolutePath());
                Path generatedFilePath = src.resolve("main/java/model/_generated.java");
                File generatedFile = generatedFilePath.toFile();
                if (generatedFile.exists()) {
                    System.out.println("Found generated java file: " + generatedFilePath.toAbsolutePath());
                    String currentSource = Files.readString(generatedFilePath);
                    int codeGenBoundary = currentSource.indexOf(CODE_GEN_BOUNDARY);
                    if (codeGenBoundary > 0) {
                        String currentHeader = currentSource.substring(0, codeGenBoundary);
                        Files.writeString(generatedFilePath, currentHeader + code + "\n}\n");
                        System.out.println("Updated " + generatedFile);
                        return true;
                    } else {
                        System.out.println("Could not find code gen boundary in " + generatedFile);
                    }
                } else {
                    System.out.println("Could not find generated java file: " + generatedFilePath.toAbsolutePath());
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    protected static String generateCodeForPackage(String packageName) {
        String packagePath = packageName.replaceAll("[.]", "/");
        InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(packagePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        StringBuilder sb = new StringBuilder(CODE_GEN_BOUNDARY).append("\n\n");
        nice(reader.lines())
                .filter(line -> line.endsWith(".class"))
                .sort()
                .map(line -> TheMissingUtils.getClass(packageName + "." + line.substring(0, line.lastIndexOf('.'))))
                .each(aClass -> {
                    if (ChillRecord.class.isAssignableFrom(aClass) && !Modifier.isAbstract(aClass.getModifiers())) {
                        codeGen(aClass, sb);
                    }
                });
        return sb.toString();
    }

    public static void codeGen(Class templateClass, StringBuilder sb) {
        Object instance;
        instance = TheMissingUtils.newInstance(templateClass);
        String newLine = "\n    ";

        String className = templateClass.getSimpleName().replace("$", ".");

        sb.append(newLine)
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
        NiceList<Field> fks = new NiceList<>();
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
                    fks.add(javaField);
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

        sb.append("public static chill.db.ChillQuery<").append(className).append("> where(Object... args) {").append(newLine)
                .append("  return find.where(args);")
                .append("}").append(newLine)
                .append(newLine);

        sb.append("public static chill.db.ChillQuery<").append(className).append("> join(chill.db.ChillField.FK fk) {").append(newLine)
                .append("  return find.join(fk);")
                .append("}").append(newLine)
                .append(newLine);

        sb.append("public static class to {").append(newLine)
                .append("  private static final ").append(className).append(" instance = new ").append(className).append("();").append(newLine);

        for (Field fk : fks) {
            sb.append("  public static final chill.db.ChillField.FK ").append(fk.getName()).append(" = instance.").append(fk.getName()).append(";").append(newLine);
        }
        sb.append("}").append(newLine)
        .append(newLine);

        sb.append("}").append(newLine);
    }
}
