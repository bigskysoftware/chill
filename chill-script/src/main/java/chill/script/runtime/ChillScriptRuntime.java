package chill.script.runtime;

import chill.script.commands.Command;
import chill.script.commands.FunctionCommand;
import chill.script.commands.DependOnCommand;
import chill.script.parser.ChillScriptProgram;
import chill.script.parser.ParseElement;
import chill.script.types.ChillType;
import chill.utils.NiceList;
import chill.utils.TheMissingUtils;
import chill.utils.TypedMap;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

import static java.lang.Boolean.FALSE;

public class ChillScriptRuntime {

    static {
        System.setProperty("log4j.logger.org.apache.ivy", "OFF");
    }

    public static final Object UNDEFINED = new Object();

    TypedMap metadata = new TypedMap();
    IvyClassLoader classLoader = null;

    LinkedList<Frame> frames = new LinkedList<>();
    LinkedList<ScopeFrame> scopes = new LinkedList<>();
    IvyClassLoader ivyClassLoader;

    public ChillScriptRuntime(Object... rootValues) {
        this(TheMissingUtils.mapFrom(rootValues));
    }

    public ChillScriptRuntime(Map<String, Object> initialScope) {
        scopes.push(new ScopeFrame(initialScope)); // push root scope

        putMetaData(Sqlite.RT_KEY, new Sqlite());
    }

    public Object getSymbol(String symbol) {
        if (!frames.isEmpty()) {
            return frames.getFirst().getSymbol(symbol);
        } else {
            for (ScopeFrame scope : scopes) {
                if (scope.hasSymbol(symbol)) {
                    return scope.getSymbol(symbol);
                }
            }
        }
        return UNDEFINED;
    }

    public void setSymbol(String symbol, Object initialValue) {
        if (!frames.isEmpty()) {
            frames.getFirst().setSymbol(symbol, initialValue);
        } else {
            scopes.getFirst().setSymbol(symbol, initialValue);
        }
    }

    public void pushScope() {
        if (!frames.isEmpty()) {
            frames.getFirst().pushScope();
        } else {
            scopes.addFirst(new ScopeFrame());
        }
    }

    public void popScope() {
        if (!frames.isEmpty()) {
            frames.getFirst().popScope();
        } else {
            scopes.removeFirst();
        }
    }

    public <T> T getMetaData(TypedMap.Key<T> key) {
        return metadata.get(key);
    }

    public <T> void putMetaData(TypedMap.Key<T> key, T value) {
        metadata.set(key, value);
    }


    public void execute(Command command) {
        beforeExecute(command);
        command.execute(this);
        afterExecute(command);
    }

    public void beforeExecute(Command command) {
        if (command instanceof ChillScriptProgram program && !program.getChildren().isEmpty()) {
            NiceList<DependOnCommand> imports = new NiceList<>();
            classLoader = new IvyClassLoader();

            for (ParseElement child : program.getChildren()) {
                if (child instanceof DependOnCommand importCommand) {
                    ModuleRevisionId mrid = ModuleRevisionId.newInstance(importCommand.getUri(), importCommand.getName(), importCommand.getVersion());
                    classLoader.resolve(mrid);
                    imports.add(importCommand);
                }
            }

            classLoader.load();
            for (DependOnCommand importCommand : imports) {
                for (Command selector : importCommand.getSelectors()) {
                    resolveImportSelector(selector, classLoader);
                }
            }
        }
    }

    public void resolveImportSelector(Command selector, ClassLoader loader) {
        if (selector instanceof DependOnCommand.UseCommand use) {
            for (String className : use.getLinks()) {
                try {
                    Class<?> clazz = loader.loadClass(className);
                    setSymbol(className.substring(className.lastIndexOf(".") + 1), clazz);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Could not load class: " + className, e);
                }
            }
        } else if (selector instanceof DependOnCommand.FromCommand from) {
            if (from.getSymbols().size() == 1 && from.getSymbols().get(0).equals("*")) {
                var items = classLoader.getResourceAsStream(from.getUri().replaceAll(Pattern.quote("."), "/"));
                BufferedReader reader = new BufferedReader(new InputStreamReader(items));
                reader.lines().forEach(line -> {
                    System.out.printf("line in " + from.getUri() + ": %s\n", line);
                });
            } else {
                for (String symbol : from.getSymbols()) {
                    String className = from.getUri() + "." + symbol;
                    try {
                        Class<?> clazz = loader.loadClass(className);
                        setSymbol(symbol, clazz);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("Could not load class: " + className, e);
                    }
                }
            }
        } else {
            throw new RuntimeException("Unknown import selector: " + selector);
        }
    }

    public void afterExecute(Command command) {
    }

    public void print(Object value) {
        System.out.println(String.valueOf(value));
    }

    public ChillType resolveType(String name) {
//        for (String anImport : imports) {
//            if (anImport.endsWith("*")) {
//                try {
//                    return TypeSystem.getType(Class.forName(anImport.substring(0, anImport.length() - 1) + name));
//                } catch (Exception e) {
//                    // ignore
//                }
//            }
//            if (anImport.endsWith("." + name)) {
//                try {
//                    return TypeSystem.getType(Class.forName(anImport));
//                } catch (Exception e) {
//                    // ignore
//                }
//            }
//        }
        return null;

    }
    public boolean isTruthy(Object value) {
        return value != null &&
                !FALSE.equals(value) &&
                !TheMissingUtils.EMPTY_STRING.equals(value);
    }

    public boolean isGlobalScope() {
        return frames.isEmpty() && scopes.size() == 1;
    }

    public void pushFrame(FunctionCommand.Closure function) {
        frames.addFirst(new Frame(function, function.getScope()));
    }

    public void popFrame() {
        frames.removeFirst();
    }

    public LinkedList<ScopeFrame> getCurrentScope() {
        if (!frames.isEmpty()) {
            return frames.getFirst().scopes;
        } else {
            return scopes;
        }
    }

    public interface ExceptionHandler {
        void handle(Command cmd, Exception e);
    }

    private static class Frame {
        final LinkedList<ScopeFrame> scopes;
        final Object thisValue;

        public Frame(Object thisValue) {
            this.scopes = new LinkedList<>();
            this.scopes.push(new ScopeFrame());
            this.thisValue = thisValue;
        }

        public Frame(Object thisValue, LinkedList<ScopeFrame> scope) {
            this.thisValue = thisValue;
            scopes = Objects.requireNonNull(scope);
            if (scope.isEmpty()) scope.push(new ScopeFrame());
        }

        public void pushScope() {
            scopes.addFirst(new ScopeFrame());
        }

        public void popScope() {
            scopes.removeFirst();
        }

        public Object getSymbol(String symbol) {
            for (ScopeFrame scope : scopes) {
                if (scope.hasSymbol(symbol)) {
                    return scope.getSymbol(symbol);
                }
            }
            return UNDEFINED;
        }

        public void setSymbol(String symbol, Object value) {
            for (ScopeFrame scope : scopes) {
                Entry entry = scope.getEntry(symbol);
                if (entry != null) {
                    entry.value(value);
                    return;
                }
            }
            scopes.getFirst().setSymbol(symbol, value);
        }
    }

    public static class ScopeFrame {
        final Map<String, Entry> symbols;

        public ScopeFrame() {
            symbols = new HashMap<>();
        }

        public ScopeFrame(Map<String, Object> initialScope) {
            symbols = new HashMap<>();
            for (Map.Entry<String, Object> entry : initialScope.entrySet()) {
                symbols.put(entry.getKey(), new Entry(entry.getValue()));
            }
        }

        public void setSymbol(String symbol, Object value) {
            if (symbols.containsKey(symbol)) {
                symbols.get(symbol).value(value);
            } else {
                symbols.put(symbol, new Entry(value));
            }
        }

        Object getSymbol(String symbol) {
            return symbols.get(symbol).value();
        }

        public ScopeFrame shallowCopy() {
            var rv = new ScopeFrame();
            rv.symbols.putAll(symbols);
            return rv;
        }

        public boolean hasSymbol(String symbol) {
            return symbols.containsKey(symbol);
        }

        public Entry getEntry(String symbol) {
            return symbols.get(symbol);
        }
    }

    public static class Entry {
        Object value;

        public Entry(Object value) {
            this.value = value;
        }

        public Object value() {
            return value;
        }

        public void value(Object value) {
            this.value = value;
        }
    }
}
