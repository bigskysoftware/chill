package chill.script.templates.commands;

import chill.script.runtime.ChillScriptRuntime;
import chill.script.expressions.Expression;
import chill.script.templates.ChillTemplateRuntime;

import java.io.IOException;
import java.util.LinkedList;

public class ChillTemplateIfCommand extends ChillTemplateCommand {

    private Expression expr;
    private LinkedList<ChillTemplateCommand> body;
    private ChillTemplateCommand elseCmd;

    public void setElseCmd(ChillTemplateCommand elseCmd) {
        this.elseCmd = elseCmd;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }

    public void setBody(LinkedList<ChillTemplateCommand> body) {
        this.body = body;
    }

    @Override
    public void render(ChillTemplateRuntime context) {
        Object evaluate = expr.evaluate(context);
        if (evaluate != null && !Boolean.FALSE.equals(evaluate)) {
            for (ChillTemplateCommand elt : body) {
                elt.render(context);
            }
        } else if (elseCmd != null) {
            elseCmd.render(context);
        }
    }
}
