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
import chill.script.templates.HasDisplayString;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
        if (forExpr instanceof PropertyAccessExpression) {
            PropertyAccessExpression pae = (PropertyAccessExpression) forExpr;
            String name = "";
            String id = "";
            Object value = null;

            Object rootVal = pae.getRoot().evaluate(context);
            ChillType runtimeType = TypeSystem.getRuntimeType(rootVal);
            ChillProperty property = runtimeType.getProperty(pae.getPropertyName());

            if (property == null) {
                throw new IllegalStateException("No property '" + pae.getPropertyName() + "' found on " + rootVal);
            }

            Class propertyClass = property.getType().getBackingClass();

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
                value = getArgs().get("value").evaluate(context);
            } else {
                value = property.get(rootVal);
            }

            Object labelValue = null;
            if (label != null) {
                labelValue = label.evaluate(context);
            }

            if (!Boolean.FALSE.equals(labelValue)) {
                if (labelValue instanceof String) {
                    context.append("<label for=\"").append(id).append("\">").append(String.valueOf(labelValue)).append("</label>");
                } else {
                    context.append("<label for=\"").append(id).append("\">").append(TheMissingUtils.capitalize(name)).append("</label>");
                }
            }

            if (Boolean.class.equals(propertyClass)) {
                context.append("<input type=\"checkbox\" id=\"").append(id).append("\" name=\"").append(name).append("\" value=\"true\"");
                if (Boolean.TRUE.equals(value)) {
                    context.append("checked ");
                }
                appendAttributes(context, getArgs().entrySet());
                context.append("/>");
            } else if (propertyClass.isEnum()) {
                context.append("<select id=\"").append(id).append("\" name=\"").append(name).append("\" ");
                appendAttributes(context, getArgs().entrySet());
                context.append(">");
                for (Object enumConstant : propertyClass.getEnumConstants()) {
                    Enum e = (Enum) enumConstant;
                    context.append("<option value=\"").append(e.name()).append("\" ");
                    if (e.equals(value)) {
                        context.append(" selected");
                    }
                    context.append(">");
                    if (e instanceof HasDisplayString) {
                        HasDisplayString webDisplay = (HasDisplayString) e;
                        context.append(webDisplay.getDisplayString());
                    } else {
                        context.append(e.toString());
                    }
                    context.append("</option>");
                }
                context.append("</select>");
            } else {
                if (value == null) {
                    value = "";
                }
                context.append("<input id=\"").append(id).append("\" name=\"").append(name).append("\" value=\"").append(String.valueOf(value)).append("\" ");
                appendAttributes(context, getArgs().entrySet());
                context.append(">");
            }


        } else {
            throw new IllegalStateException("TODO - handle non-property expressions or error on them");
        }
    }

    private void appendAttributes(ChillTemplateRuntime context, Set<Map.Entry<String, Expression>> attributes) {
        for (var arg : attributes) {
            context.append(arg.getKey()).append("=\"").append(String.valueOf(arg.getValue().evaluate(context))).append("\"");
        }
    }

}
