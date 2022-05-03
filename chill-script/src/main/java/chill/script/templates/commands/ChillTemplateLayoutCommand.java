package chill.script.templates.commands;

import chill.script.expressions.PathLiteralExpression;
import chill.script.templates.ChillTemplate;
import chill.script.templates.ChillTemplateRuntime;
import chill.script.templates.ChillTemplates;

import java.io.IOException;
import java.util.List;

public class ChillTemplateLayoutCommand extends ChillTemplateCommand {

    private PathLiteralExpression expr;

    public void setLayout(PathLiteralExpression expr) {
        this.expr = expr;
    }

    @Override
    public void render(ChillTemplateRuntime context) {
        // do nothing
    }

    public void renderAsLayout(ChillTemplateRuntime context) {
        String path = String.valueOf(expr.evaluate(context));
        ChillTemplate layout = getTemplateEngine().get(path);
        layout.renderAsLayout(context, this.getTemplate());
    }

    public void setExpr(PathLiteralExpression pathLiteral) {
        expr = pathLiteral;
    }
}
