package chill.script.templates;

import chill.script.parser.ParseElement;

public class ChillTemplateParseElement extends ParseElement {
    ChillTemplates engine;

    public void setTemplateEngine(ChillTemplates templateEngine) {
        engine = templateEngine;
    }

    public ChillTemplates getTemplateEngine() {
        ChillTemplate template = getTemplate();
        if (template == null || template.engine == null) {
            throw new IllegalStateException("No ChillTemplate Engine associated with this template!");
        } else {
            return template.engine;
        }
    }
}
