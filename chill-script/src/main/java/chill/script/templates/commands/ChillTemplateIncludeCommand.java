package chill.script.templates.commands;

import chill.script.expressions.Expression;
import chill.script.expressions.PathLiteralExpression;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.templates.ChillTemplate;
import chill.script.templates.ChillTemplateRuntime;
import chill.script.templates.ChillTemplates;
import chill.script.tokenizer.Token;

import java.io.IOException;
import java.util.List;

public class ChillTemplateIncludeCommand extends ChillTemplateCommand {

    private PathLiteralExpression expr;

    public void setExpr(PathLiteralExpression expr) {
        this.expr = expr;
    }

    @Override
    public void render(ChillTemplateRuntime context) {
        String path = String.valueOf(expr.evaluate(context));
        ChillTemplate included = getTemplateEngine().get(path);
        included.render(context);
    }
}
