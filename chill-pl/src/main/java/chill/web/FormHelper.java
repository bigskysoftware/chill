package chill.web;

import chill.script.templates.ChillTemplateCustomRenderer;

public class FormHelper {

    public static ChillTemplateCustomRenderer form(String name) {
        return (context, body) -> {
            context.append("<form name='" + name + "'>");
            for (var cmd : body) {
                cmd.render(context);
            }
            context.append("</form>");
        };
    }

}
