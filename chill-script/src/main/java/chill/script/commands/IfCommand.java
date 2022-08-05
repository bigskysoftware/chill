package chill.script.commands;

import chill.script.expressions.Expression;
import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class IfCommand extends Command {
    Expression condition;
    List<Command> trueBranch;
    List<Command> falseBranch;

    public static Command parse(ChillScriptParser parser) {
        if (parser.match("if")) {
            var rv = new IfCommand();
            rv.setStart(parser.consumeToken());

            rv.setCondition(parser.requireExpression(rv, "expression"));

            var trueBranch = new ArrayList<Command>(); // TODO(carson): I noticed LinkedList being used, do I need to?

            while(!(parser.match("else") || parser.match("end"))) {
                // TODO: Copied from ChillScriptParser.parseCommandList -- need a method for parsing command lists that
                //       doesn't just recover when it sees an unrecognized token
                Command cmd = parser.parseCommand();
                trueBranch.add(rv.addChild(cmd));
                if (cmd instanceof ErrorCommand) {
                    parser.panic();
                }
            }
            rv.setTrueBranch(trueBranch);

            var falseBranch = new ArrayList<Command>();

            if (parser.matchAndConsume("else")) {
                while(!parser.match("end")) {
                    // TODO: Copied from ChillScriptParser.parseCommandList -- need a method for parsing command lists that
                    //       doesn't just recover when it sees an unrecognized token
                    Command cmd = parser.parseCommand();
                    falseBranch.add(rv.addChild(cmd));
                    if (cmd instanceof ErrorCommand) {
                        // parser.advanceToNextCommandStart();
                        // TODO: Public recovery API. Possibly: parser.panic(TokenType... checkpoints)
                        //       Advances to next command start OR given token type.
                        return new ErrorCommand("Error inside if (very helpful message, we know)", parser.currentToken());
                    }
                }
                rv.setFalseBranch(falseBranch);
            }

            rv.setEnd(parser.consumeToken());
            return rv;
        }
        return null;
    }

    @Override
    public void execute(ChillScriptRuntime context) {
        var conditionValue = condition.evaluate(context);
        if (context.isTruthy(conditionValue)) {
            for (var cmd : trueBranch) cmd.execute(context);
        } else {
            if (falseBranch != null) for (var cmd : falseBranch) cmd.execute(context);
        }
    }

    private void setCondition(Expression cond) { condition = addChild(cond); }

    private void setTrueBranch(List<Command> branch) { trueBranch = addChildren(branch); }

    private void setFalseBranch(List<Command> branch) { falseBranch = addChildren(branch); }
}
