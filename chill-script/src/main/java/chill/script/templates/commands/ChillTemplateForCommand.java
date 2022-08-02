package chill.script.templates.commands;

import chill.script.runtime.ChillScriptRuntime;
import chill.script.templates.ChillTemplateRuntime;
import chill.script.tokenizer.Token;
import chill.script.expressions.Expression;

import java.io.IOException;
import java.util.List;

public class ChillTemplateForCommand extends ChillTemplateCommand {

    private Token identifier;
    private Token indexIdentifier;
    private Expression expr;
    private List<ChillTemplateCommand> body;

    public void setIdentifier(Token identifier) {
        this.identifier = identifier;
    }
    public void setIndexIdentifier(Token identifier) {
        this.indexIdentifier = identifier;
    }
    public void setExpr(Expression expr) {
        this.expr = expr;
    }
    public void setBody(List<ChillTemplateCommand> body) {
        this.body = body;
    }

    @Override
    public void render(ChillTemplateRuntime context) {
        Object iterable = expr.evaluate(context);
        if (iterable != null) {
            if (iterable instanceof Object[] objArray) {
                iterable = List.of(objArray);
            }
            Iterable iter = (Iterable) iterable;
            int index = 0;

            context.pushScope();
            {
                for (Object value : iter) {
                    context.declareSymbol(identifier.getStringValue(), value);
                    if (indexIdentifier != null) {
                        context.declareSymbol(indexIdentifier.getStringValue(), index);
                    }
                    for (ChillTemplateCommand elt : body) {
                        elt.render(context);
                    }
                    index++;
                }
            }
            context.popScope();
        }
    }
}