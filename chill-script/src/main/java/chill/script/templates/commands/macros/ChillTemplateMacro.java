package chill.script.templates.commands.macros;

import chill.script.expressions.Expression;
import chill.script.templates.ChillTemplateRuntime;
import chill.script.templates.commands.ChillTemplateCommand;
import chill.utils.NiceMap;
import chill.utils.TheMissingUtils;

import java.util.List;
import java.util.Map;

public abstract class ChillTemplateMacro extends ChillTemplateCommand {

    private static Map<String, Class<? extends ChillTemplateMacro>> REGISTRY = new NiceMap<>();

    private String leadingWhitespace;
    private NiceMap<String, Expression> args;
    private List<ChillTemplateCommand> body;

    public String getLeadingWhitespace() {
        return leadingWhitespace;
    }

    public NiceMap<String, Expression> getArgs() {
        return args;
    }

    public List<ChillTemplateCommand> getBody() {
        return body;
    }

    public void init(String leadingWhitespace, NiceMap<String, Expression> args, List<ChillTemplateCommand> body) {
        this.leadingWhitespace = leadingWhitespace;
        this.args = args;
        this.body = body;
    }

    public boolean hasBody() {
        return false;
    }

    public static void register(Class<? extends ChillTemplateMacro> clazz) {
        ChillTemplateMacro chillTemplateMacro = TheMissingUtils.newInstance(clazz);
        REGISTRY.put(chillTemplateMacro.getName(), clazz);
    }

    protected abstract String getName();

    public static ChillTemplateMacro get(String name) {
        var clazz = REGISTRY.get(name);
        if (clazz == null) {
            return null;
        } else {
            return TheMissingUtils.newInstance(clazz);
        }
    }

}
