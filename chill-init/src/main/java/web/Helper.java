package web;

import chill.web.ChillHelper;
import chill.web.macros.InputMacro;

public class Helper extends ChillHelper {
    {
        registerMacro(InputMacro.class);
    }
}
