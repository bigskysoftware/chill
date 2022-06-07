package chill.m8.parsetree.commands;

import chill.m8.runtime.ChillTestRuntime;
import chill.script.commands.Command;
import chill.script.expressions.Expression;
import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;

public class ScreenshotCommand extends Command {

    private Expression name;

    @Override
    public void execInternal(ChillScriptRuntime runtime) {
        ChillTestRuntime chillTests = (ChillTestRuntime) runtime;
        chillTests.takeScreenshot(String.valueOf(name.evaluate(runtime)));
    }

    private void setName(Expression name) {
        this.name = addChild(name);
    }

    public static Command parse(ChillScriptParser chillTestsParser) {
        if (chillTestsParser.match("take")) {
            ScreenshotCommand screenshotCommand = new ScreenshotCommand();
            screenshotCommand.setStart(chillTestsParser.consumeToken());
            chillTestsParser.matchAndConsume("a"); // optional
            chillTestsParser.matchAndConsume("screenshot"); // optional
            chillTestsParser.matchAndConsume("named"); // optional
            screenshotCommand.setName(chillTestsParser.requireExpression(screenshotCommand, "expression"));
            return screenshotCommand;
        }
        return null;
    }
}
