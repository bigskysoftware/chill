package chill.script.templates;

import chill.script.runtime.ChillScriptRuntime;

import java.util.LinkedList;
import java.util.Map;

public class ChillTemplateRuntime extends ChillScriptRuntime {
    StringBuilder sb = new StringBuilder();
    LinkedList<ChillTemplate> contentStack = new LinkedList<>();

    public ChillTemplateRuntime(Object... rootValues) {
        super(rootValues);
    }

    public ChillTemplateRuntime(Map<String, Object> rootScope) {
        super(rootScope);
    }

    public void append(String str) {
        sb.append(str);
    }

    public String getContent() {
        return sb.toString();
    }

    public void pushContentTemplate(ChillTemplate template) {
        contentStack.push(template);
    }

    public ChillTemplate popContentTemplate(){
        return contentStack.pop();
    }

}
