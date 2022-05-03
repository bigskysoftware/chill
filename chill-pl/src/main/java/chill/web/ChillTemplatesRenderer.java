package chill.web;

import chill.script.templates.ChillTemplate;
import chill.script.templates.ChillTemplates;
import io.javalin.http.Context;
import io.javalin.plugin.rendering.FileRenderer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ChillTemplatesRenderer implements FileRenderer {

    private final ChillTemplates engine = new ChillTemplates();

    public static final ChillTemplatesRenderer INSTANCE = new ChillTemplatesRenderer();

    private ChillTemplatesRenderer() {}

    @Override
    public String render(@NotNull String filePath, @NotNull Map<String, Object> model, Context context) {
        String[] nameAndFragment = filePath.split("#");
        ChillTemplate chillTemplate = engine.get(filePath);
        model.putIfAbsent("context", context);
        model.putAll(ChillHelper.INSTANCE.getHelperSymbols());
        if (nameAndFragment.length == 1) {
            return chillTemplate.render(model);
        } else {
            return chillTemplate.renderFragment(nameAndFragment[1], model);
        }
    }
}
