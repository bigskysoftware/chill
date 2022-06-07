package chill.m8.parser;

import chill.m8.parsetree.commands.*;
import chill.m8.parsetree.expressions.BodyExpression;
import chill.m8.parsetree.expressions.ButtonPostfixExpression;
import chill.m8.parsetree.expressions.InputPostfixExpression;
import chill.m8.tokenizer.M8TestTokenizer;
import chill.script.parser.ChillScriptParser;
import chill.script.tokenizer.Tokenizer;

public class M8Parser extends ChillScriptParser {

    public M8Parser() {
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
        return new M8TestTokenizer(src);
    }

}
