package chill.script.runtime;

import chill.script.commands.Command;
import chill.script.types.ChillType;
import chill.script.types.TypeSystem;
import chill.utils.TheMissingUtils;
import chill.utils.TypedMap;

import java.util.*;

import static chill.utils.TheMissingUtils.safely;
import static java.lang.Boolean.FALSE;

public class ChillScriptRuntime {

    public static final Object UNDEFINED = new Object();

    TypedMap metadata = new TypedMap();

    LinkedList<Map<String, Object>> scopes = new LinkedList<>();
    private Object currentThis = null;

    private List<String> imports = new LinkedList<>();

    public ChillScriptRuntime(Object... rootValues) {
        this(TheMissingUtils.mapFrom(rootValues));
    }

    public ChillScriptRuntime(Map<String, Object> initialScope) {
        scopes.push(initialScope); // push root scope
    }

    // TODO - scope semantics
    public Object getSymbol(String symbol) {
        Iterator<Map<String, Object>> mapIterator = scopes.descendingIterator();
        while (mapIterator.hasNext()) {
            Map<String, Object> scope = mapIterator.next();
            if (scope.containsKey(symbol)) {
                return scope.get(symbol);
            }
        }
        return UNDEFINED;
    }

    public void setSymbolValue(String symbol, Object value) {
        scopes.getLast().put(symbol, value);
    }
    public void pushScope(){
        scopes.push(new HashMap<>());
    }
    public void popScope(){
        scopes.pop();
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

    public void beforeExecute(Command command) {}
    public void afterExecute(Command command) {}

    public void print(Object value) {
        System.out.println(String.valueOf(value));
    }

    public Object getThis() {
        return currentThis;
    }

    public ChillType resolveType(String name) {
        for (String anImport : imports) {
            if (anImport.endsWith("*")) {
                try {
                    return TypeSystem.getType(Class.forName(anImport.substring(0, anImport.length() - 1) + name));
                } catch (Exception e) {
                    // ignore
                }
            }
            if (anImport.endsWith("." + name)) {
                try {
                    return TypeSystem.getType(Class.forName(anImport));
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return null;
    }

    public void addImport(String importStr) {
        imports.add(importStr);
    }

    public boolean isTruthy(Object value) {
        return value != null &&
                !FALSE.equals(value) &&
                !TheMissingUtils.EMPTY_STRING.equals(value);
    }

    public interface ExceptionHandler{
        void handle(Command cmd, Exception e);
    }
}
