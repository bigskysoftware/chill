package chill.web;

import chill.script.templates.ChillTemplateCustomRenderer;
import chill.script.templates.commands.macros.ChillTemplateMacro;
import chill.script.types.*;
import chill.utils.ChillLogs;
import chill.utils.NiceList;
import chill.utils.TheMissingUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static chill.web.WebServer.Utils.*;

public class ChillHelper {

    static private ChillLogs.LogCategory LOG = ChillLogs.get(ChillHelper.class);

    private NiceList<Class<?>> helpers = new NiceList<>();
    private Map<String, Object> helperSymbols;

    // singleton
    public static final ChillHelper INSTANCE = new ChillHelper(true);
    protected ChillHelper(){}
    private ChillHelper(boolean flag){
        reload();
    }

    public void reload() {
        helperSymbols = new HashMap<>();
        // base stuff
        putStaticSymbols(BaseHelper.class, helperSymbols);
        try {
            ChillHelper concreteHelper = TheMissingUtils.newInstance("web.Helper");
            putStaticSymbols(concreteHelper.getClass(), helperSymbols);
            for (var helper : concreteHelper.helpers) {
                putStaticSymbols(helper, helperSymbols);
            }
        } catch (Exception e) {
            LOG.error("Unable to load application helpers!", e);
        }
    }

    public void init() {
        // runs the static initializers
    }

    private void putStaticSymbols(Class<?> helper, Map<String, Object> helperSymbols) {
        ChillType type = TypeSystem.getType(helper);

        List<ChillMethod> methods = type.getDeclaredMethods();
        for (ChillMethod method : methods) {
            if (method.isStatic()) {
                helperSymbols.put(method.getName(), method);
            }
        }

        List<ChillProperty> properties = type.getDeclaredProperties();
        for (ChillProperty prop : properties) {
            if (prop.isStatic()) {
                helperSymbols.put(prop.getCanonicalName(), prop);
            }
        }
    }

    public void include(Class<?> helperClass) {
        helpers.add(helperClass);
    }

    public Map<String, ?> getHelperSymbols() {
        return helperSymbols;
    }

    protected void registerMacro(Class<? extends ChillTemplateMacro> clazz) {
        ChillTemplateMacro.register(clazz);
    }

    public static class BaseHelper {
        public static UnifiedParams getParams(){
            return params;
        }
        public static SessionMap getSession(){
            return session;
        }
        public static FlashMap getFlash(){
            return flash;
        }
        public static HeaderMap getHeaders(){
            return headers;
        }
        public static String getPath(){
            return ctx.get().path();
        }
        public static ChillTemplateCustomRenderer raw(String str) {
            return (context, body) -> context.append(str);
        }
    }
}
