package chill.script.templates;

public class TestHelpers {
    public static String renderTemplate(String str, Object... args) {
        var parser = new ChillTemplateParser();
        var template = parser.parseTemplate(str);
        String render = template.render(args);
        return render;
    }
}
