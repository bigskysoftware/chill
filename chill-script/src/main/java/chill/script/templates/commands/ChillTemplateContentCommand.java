package chill.script.templates.commands;

import chill.script.expressions.PathLiteralExpression;
import chill.script.templates.ChillTemplate;
import chill.script.templates.ChillTemplateRuntime;
import chill.script.templates.ChillTemplates;

import java.io.IOException;

public class ChillTemplateContentCommand extends ChillTemplateCommand {

    private PathLiteralExpression expr;

    public void setExpr(PathLiteralExpression expr) {
        this.expr = expr;
    }

    @Override
    public void render(ChillTemplateRuntime context) {
        ChillTemplate content = context.popContentTemplate();
        content.renderAsContent(context);
    }
}
