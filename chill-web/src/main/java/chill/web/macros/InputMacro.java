package chill.web.macros;

import chill.script.expressions.Expression;
import chill.script.expressions.PropertyAccessExpression;
import chill.script.templates.ChillTemplateRuntime;
import chill.script.templates.commands.ChillTemplateCommand;
import chill.script.templates.commands.macros.ChillTemplateMacro;
import chill.script.types.ChillProperty;
import chill.script.types.ChillType;
import chill.script.types.TypeSystem;
import chill.utils.NiceMap;
import chill.utils.TheMissingUtils;

import java.util.List;

public class InputMacro extends ChillTemplateMacro {

    // TODO - use an internal template mechanism for laying out inputs
    // TODO - dynamically select widget type based on property information
    // TODO - look at https://github.com/heartcombo/simple_form for ideas

    Expression forExpr;
    private Expression label;

    @Override
    public void init(String leadingWhitespace, NiceMap<String, Expression> args, List<ChillTemplateCommand> body) {
        super.init(leadingWhitespace, args, body);
        forExpr = args.take("for");
        label = args.take("label");
    }

    @Override
    protected String getName() {
        return "input";
    }

    @Override
    public void render(ChillTemplateRuntime context) {
        if (forExpr instanceof PropertyAccessExpression pae) {
            String name = "";
            String id = "";
            String value = "";

            Object rootVal = pae.getRoot().evaluate(context);
            ChillType runtimeType = TypeSystem.getRuntimeType(rootVal);
            ChillProperty property = runtimeType.getProperty(pae.getPropertyName());
            if (property == null) {
                throw new IllegalStateException("No property '" + pae.getPropertyName() + "' found on " + rootVal);
            }

            if (getArgs().containsKey("name")) {
                name = String.valueOf(getArgs().get("name").evaluate(context));
            } else {
                name = pae.getPropertyName();
            }

            if (getArgs().containsKey("id")) {
                id = String.valueOf(getArgs().get("id").evaluate(context));
            } else {
                id = pae.getPropertyName() + "_input";
            }

            if (getArgs().containsKey("value")) {
                value = String.valueOf(getArgs().get("value").evaluate(context));
            } else {
                value = String.valueOf(property.get(rootVal));
            }

            if (label != null) {
                Object labelValue = label.evaluate(context);
                if (!Boolean.FALSE.equals(labelValue)) {
                    context.append("<label for=\"").append(id).append("\">").append(String.valueOf(labelValue)).append("</label>");
                }
            } else {
                context.append("<label for=\"").append(id).append("\">").append(TheMissingUtils.capitalize(name)).append("</label>");
            }

            context.append("<input id=\"").append(id).append("\" name=\"").append(name).append("\" value=\"").append(value).append("\" ");
            for (var arg : getArgs().entrySet()) {
                context.append(arg.getKey()).append("=\"").append(String.valueOf(arg.getValue().evaluate(context))).append("\"");
            }
            context.append(">");
        } else {
            throw new IllegalStateException("TODO - handle non-property expressions or error on them");
        }
    }

}
