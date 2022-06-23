package autom8.parser;

import autom8.parsetree.commands.*;
import autom8.parsetree.expressions.BodyExpression;
import autom8.parsetree.expressions.ButtonPostfixExpression;
import autom8.parsetree.expressions.InputPostfixExpression;
import autom8.tokenizer.Autom8Tokenizer;
import chill.script.parser.ChillScriptParser;
import chill.script.tokenizer.Tokenizer;

public class Autom8Parser extends ChillScriptParser {

    public Autom8Parser() {
        initCommands();
        initExpressions();
    }

    private void initCommands() {
        registerCommand("go", GoToCommand::parse);
        registerCommand("take", ScreenshotCommand::parse);
        registerCommand("wait", WaitCommand::parse);
        registerCommand("put", PutCommand::parse);
        registerCommand("click", ClickCommand::parse);
        registerCommand("expect", ExpectCommand::parse);
    }

    private void initExpressions() {
        registerPrimaryExpression("bodyExpression", BodyExpression::parse);
        registerPostfixExpression(InputPostfixExpression::parse);
        registerPostfixExpression(ButtonPostfixExpression::parse);
    }

    @Override
    protected Tokenizer getTokenizer(String src) {
        return new Autom8Tokenizer(src);
    }

}
