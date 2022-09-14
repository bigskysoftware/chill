package chill.script.templates.commands;

import chill.script.expressions.Expression;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.templates.ChillTemplateCustomRenderer;
import chill.script.templates.ChillTemplateRuntime;
import chill.script.templates.HasDisplayString;
import org.owasp.encoder.Encode;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Boolean.TRUE;

public class ChillTemplateExpressionElement extends ChillTemplateCommand {
    private Expression expr;
    private Expression condition;
    private List<ChillTemplateCommand> body;

    public void setExpr(Expression expr) {
        this.expr = expr;
    }

    @Override
    public void render(ChillTemplateRuntime context) {
        if (condition != null) {
            if (!TRUE.equals(condition.evaluate(context))) {
                return;
            }
        }
        Object value = expr.evaluate(context);
        if (value != null) {
            if (value instanceof ChillTemplateCustomRenderer) {
                ChillTemplateCustomRenderer  customRenderer = (ChillTemplateCustomRenderer) value;
                customRenderer.render(context, body);
            } else if (value instanceof HasDisplayString) {
                HasDisplayString hasDisplayString = (HasDisplayString) value;
                context.append(Encode.forHtml(hasDisplayString.getDisplayString()));
            } else {
                context.append(Encode.forHtml(String.valueOf(value)));
            }
        }
    }

    public void setConditional(Expression expression) {
        this.condition = expression;
    }

    public void setBody(List<ChillTemplateCommand> body) {
        this.body = body;
    }
}
