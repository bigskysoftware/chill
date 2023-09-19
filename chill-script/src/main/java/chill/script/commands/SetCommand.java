package chill.script.commands;

import chill.script.pattern.Pattern;
import chill.script.expressions.Expression;
import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.TokenType;

public class SetCommand extends Command {

    private Pattern pattern;
    private Expression value;

    @Override
    public void execute(ChillScriptRuntime runtime) {
        pattern.bind(runtime, value.evaluate(runtime));
    }

    public static Command parse(ChillScriptParser parser) {
        if (parser.match("set")) {
            SetCommand setCommand = new SetCommand();
            setCommand.setStart(parser.consumeToken());

            Pattern pattern = Pattern.parsePattern(parser);
            if (pattern != null) {
                setCommand.setPattern(pattern);
            } else {
                setCommand.addError(parser.lastMatch(), "Expected pattern");
            }

            if (parser.match(TokenType.EQUAL)) setCommand.addError(parser.consumeToken(), "Use 'to' when setting variables");
            parser.require("to", setCommand, "Expected a 'to'");
            setCommand.setValue(parser.requireExpression(setCommand, "expression"));
            setCommand.setEnd(parser.lastMatch());
            return setCommand;
        } else if (parser.match("let")) {
            SetCommand setCommand = new SetCommand();
            setCommand.setStart(parser.consumeToken());

            Pattern pattern = Pattern.parsePattern(parser);
            if (pattern != null) {
                setCommand.setPattern(pattern);
            } else {
                setCommand.addError(parser.lastMatch(), "Expected pattern");
            }

            if (parser.match(TokenType.EQUAL)) setCommand.addError(parser.consumeToken(), "Use 'be' or 'equal' when using let-variables");

            if (!parser.match("be", "equal")) {
                setCommand.addError(parser.consumeToken(), "Expected a 'be' or 'equal'");
            } else {
                parser.consumeToken();
            }

            setCommand.setValue(parser.requireExpression(setCommand, "expression"));
            setCommand.setEnd(parser.lastMatch());
            return setCommand;
        }
        return null;
    }

    private void setValue(Expression expression) {
        value = addChild(expression);
    }

    private void setPattern(Pattern pattern) {
        this.pattern = addChild(pattern);
    }
}
