package chill.script.templates;

import chill.script.templates.commands.ChillTemplateFragmentCommand;
import chill.script.templates.commands.ChillTemplateCommand;
import chill.script.templates.commands.ChillTemplateLayoutCommand;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChillTemplate extends ChillTemplateParseElement {

    private List<ChillTemplateCommand> body;
    private ChillTemplateLayoutCommand layout;
    private ChillTemplates.TemplateLoader loader;
    private Map<String, ChillTemplateFragmentCommand> fragments = new HashMap<>();

    public String render(Map<String, Object> context) {
        ChillTemplateRuntime runtime = new ChillTemplateRuntime(context);
        render(runtime);
        return runtime.getContent();
    }

    public String render(Object... args) {
        ChillTemplateRuntime runtime = new ChillTemplateRuntime(args);
        render(runtime);
        return runtime.getContent();
    }

    public void render(ChillTemplateRuntime context) {
        if (layout != null) {
            layout.renderAsLayout(context);
        } else {
            renderAsContent(context);
        }
    }

    public void renderAsContent(ChillTemplateRuntime context) {
        for (var element : getBody()) {
            element.render(context);
        }
    }

    public String renderFragment(String fragmentName, Map<String, Object> symbols) {
        ChillTemplateRuntime context = new ChillTemplateRuntime(symbols);
        renderFragment(fragmentName, context);
        return context.getContent();
    }

    public String renderFragment(String fragmentName, Object... args) {
        ChillTemplateRuntime context = new ChillTemplateRuntime(args);
        renderFragment(fragmentName, context);
        return context.getContent();
    }

    public void renderFragment(String fragmentName, ChillTemplateRuntime context) {
        ChillTemplateFragmentCommand fragment = fragments.get(fragmentName);
        fragment.render(context);
    }

    private List<ChillTemplateCommand> getBody() {
        return body;
    }

    public void setBody(List<ChillTemplateCommand> body) {
        this.body = body;
        for (ChillTemplateCommand lekkerTemplateElement : body) {
            addChild(lekkerTemplateElement);
        }
    }

    public void renderAsLayout(ChillTemplateRuntime context, ChillTemplate content) {
        context.pushContentTemplate(content);
        render(context);
    }

    public void setLayout(ChillTemplateLayoutCommand layout) {
        this.layout = layout;
    }

    public void setSource(ChillTemplates.TemplateLoader loader) {
        this.loader = loader;
    }

    @Override
    public String toString() {
        if (loader != null) {
            return loader.getFullPath();
        } else {
            return super.toString();
        }
    }

    public void addFragment(ChillTemplateFragmentCommand fragmentCommand) {
        this.fragments.put(fragmentCommand.getFragmentName(), fragmentCommand);
    }


}
