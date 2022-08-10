package chill.script.parser;

import chill.script.commands.*;
import chill.script.expressions.*;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenList;
import chill.script.tokenizer.TokenType;
import chill.script.tokenizer.Tokenizer;
import chill.utils.Pair;

import java.util.*;

public class ChillScriptParser {

    private final Map<String, CommandParser> commands;
    private final Map<String, ExpressionParser> expressions;
    private final List<IndirectExpressionParser> indirectExpressions;
    private final List<IndirectExpressionParser> postFixExpressions;
    private final List<String> primaryExpressions = new LinkedList<>();

    private TokenList tokens;
    private String srcPath;

    public ChillScriptParser() {
        commands = new HashMap<>();
        expressions = new HashMap<>();
        indirectExpressions = new LinkedList<>();
        postFixExpressions = new LinkedList<>();
        initCoreCommands();
        initExpressionCoreGrammar();
    }

    public Object parseProgramOrExpression(String src) {
        initTokens(src);
        Expression expression = parse("expression");
        if (!tokens.hasMoreTokens()) {
            return expression;
        } else {
            return parseProgram(src);
        }
    }

    public ChillScriptProgram parseProgram(String src) throws ChillScriptParseException{
        initTokens(src);
        ChillScriptProgram program = new ChillScriptProgram();
        program.setStart(currentToken());
        program.setBody(parseCommandList());
        program.setEnd(lastMatch());
        if (program.isValid()) {
            return program;
        } else {
            throw new ChillScriptParseException(program);
        }
    }

    public Expression parseExpression(String src) {
        initTokens(src);
        Expression expression = parse("expression");
        if (expression.isValid()) {
            return expression;
        } else {
            throw new ChillScriptParseException(expression);
        }
    }

    protected final void initTokens(String src) {
        Tokenizer tokenizer = getTokenizer(src);
        this.tokens = tokenizer.getTokens();
    }

    protected Tokenizer getTokenizer(String src) {
        Tokenizer tokenizer = new Tokenizer(src);
        tokenizer.setSourcePath(srcPath);
        return tokenizer;
    }

    protected List<Command> parseCommandList() {
        LinkedList<Command> commands = new LinkedList<>();
        while (moreTokens()) {
            Command cmd = parseCommand();
            commands.add(cmd);
            if (cmd instanceof ErrorCommand) {
                panic();
            }
        }
        return commands;
    }

    public void panic() {
        while (!atCheckpoint() && moreTokens()) {
            consumeToken();
        }
    }

    private boolean atCheckpoint() {
        return atCommandStart() || match("end") || match("else");
    }

    private boolean atCommandStart() {
        Token currentToken = currentToken();
        return currentToken.getType() == TokenType.SYMBOL &&
                commands.get(currentToken.getStringValue()) != null;
    }

    public Command parseCommand() {
        var commandParser = getCommandParser(tokens);
        if (commandParser != null) {
            Command command = commandParser.parse(this);
            match("then"); // optional 'then' divider
            return command;
        } else {
            if (currentToken().getType() == TokenType.SYMBOL) {
                return new ErrorCommand("Unknown command: " + currentToken().getStringValue(), currentToken());
            } else {
                return new ErrorCommand("Unexpected token: " + currentToken().getStringValue(), currentToken());

            }
        }
    }

    private CommandParser getCommandParser(TokenList tokens) {
        Token currentToken = currentToken();
        if (currentToken.getType() == TokenType.SYMBOL) {
            return commands.get(currentToken.getStringValue());
        }
        return null;
    }

    private void initCoreCommands() {
        registerCommand("print", PrintCommand::parse);
        registerCommand("set", SetCommand::parse);
        registerCommand("if", IfCommand::parse);
        registerCommand("for", ForCommand::parse);
        registerCommand("repeat", RepeatCommand::parse);
    }

    private void initExpressionCoreGrammar() {
        // Core grammar
        registerExpression("expression", this::parsePostfixExpressions);
        registerExpression("equalityExpression", EqualityExpression::parse);
        registerExpression("logicalExpression", LogicalExpression::parse);
        registerExpression("comparisonExpression", ComparisonExpression::parse);
        registerExpression("additiveExpression", AdditiveExpression::parse);
        registerExpression("factorExpression", (parser) -> parser.parse("unaryExpression"));
        registerExpression("unaryExpression", UnaryExpression::parse);
        registerExpression("indirectExpression", this::parseIndirectExpressions);
        registerExpression("primaryExpression", (parser) -> parser.parse(primaryExpressions));

        // Core primary expressions
        registerPrimaryExpression("string", StringLiteralExpression::parse);
        registerPrimaryExpression("number", NumberLiteralExpression::parse);
        registerPrimaryExpression("identifier", IdentifierExpression::parse);
        registerPrimaryExpression("listLiteral", ListLiteralExpression::parse);
        registerPrimaryExpression("urlLiteral", URLLiteralExpression::parse);
        registerPrimaryExpression("pathLiteral", PathLiteralExpression::parse);
        registerPrimaryExpression("parenthesizedExpression", ParenthesizedExpression::parse);

        // Core indirect expressions
        registerIndirectExpression(ArrayAccessExpression::parse);
        registerIndirectExpression(PropertyAccessExpression::parse);
        registerIndirectExpression(PropertyReferenceExpression::parse);
        registerIndirectExpression(MethodCallExpression::parse);
    }

    private Expression parseIndirectExpressions(ChillScriptParser chillScriptParser) {
        var urRoot = parse("primaryExpression");
        var root = urRoot;
        Expression indirectExpression = parseIndirectExpression(root);
        while (indirectExpression != null) {
            indirectExpression.setStart(urRoot.getStart());
            root = indirectExpression;
            indirectExpression = parseIndirectExpression(root);
        }
        return root;
    }

    private Expression parseIndirectExpression(Expression root) {
        for (IndirectExpressionParser parser : indirectExpressions) {
            var indirectExpression = parser.parse(Pair.of(this, root));
            if (indirectExpression != null) {
                return indirectExpression;
            }
        }
        return null;
    }

    private Expression parsePostfixExpressions(ChillScriptParser chillScriptParser) {
        var urRoot = parse("equalityExpression");
        var root = urRoot;
        Expression postfixExpression = parsePostfixExpression(root);
        while (postfixExpression != null) {
            postfixExpression.setStart(urRoot.getStart());
            root = postfixExpression;
            postfixExpression = parsePostfixExpression(root);
        }
        return root;
    }

    private Expression parsePostfixExpression(Expression root) {
        for (IndirectExpressionParser parser : postFixExpressions) {
            var indirectExpression = parser.parse(Pair.of(this, root));
            if (indirectExpression != null) {
                return indirectExpression;
            }
        }
        return null;
    }

    public void registerIndirectExpression(IndirectExpressionParser parser) {
        indirectExpressions.add(parser);
    }

    public void registerPostfixExpression(IndirectExpressionParser parser) {
        postFixExpressions.add(parser);
    }

    public void registerCommand(String start, CommandParser cmdParser) {
        commands.put(start, cmdParser);
    }

    public void registerExpression(String name, ExpressionParser exprParser) {
        expressions.put(name, exprParser);
    }

    public void registerPrimaryExpression(String name, ExpressionParser exprParser) {
        registerExpression(name, exprParser);
        primaryExpressions.add(name);
    }

    public Expression requireExpression(ParseElement parent, String... expressionTypes) {
        Expression parsed = parse(expressionTypes);
        if (parsed == null) {
            parent.addError(currentToken(), "Expected " + Arrays.toString(expressionTypes));
        }
        return parsed;
    }

    public Expression parse(String... expressionTypes) {
        for (String exprType : expressionTypes) {
            ExpressionParser exprParser = expressions.get(exprType);
            Expression expr = exprParser.parse(this);
            if (expr != null) {
                return expr;
            }
        }
        return null;
    }

    public Expression parse(List<String> expressionTypes) {
        for (String exprType : expressionTypes) {
            Expression expr = expressions.get(exprType).parse(this);
            if (expr != null) {
                return expr;
            }
        }
        return null;
    }


    //=====================================================================
    // Tokenization helpers
    //=====================================================================

    public Token lastMatch() {
        return tokens.lastMatch();
    }

    public Token currentToken() {
        return tokens.getCurrentToken();
    }

    public boolean match(String identifier) {
        return tokens.match(identifier);
    }

    public boolean matchNext(String identifier) {
        return tokens.match(1, identifier);
    }

    public boolean match(TokenType... types) {
        return tokens.match(types);
    }

    public Token consumeToken() {
        return tokens.consumeToken();
    }

    public boolean matchAndConsume(String type) {
        if (match(type)) {
            consumeToken();
            return true;
        } else {
            return false;
        }
    }


    public boolean matchSequence(Object... tokenSeq) {
        return tokens.matchSequence(tokenSeq);
    }

    public boolean matchAndConsume(TokenType type) {
        if (match(type)) {
            consumeToken();
            return true;
        } else {
            return false;
        }
    }

    public boolean moreTokens() {
        return tokens.hasMoreTokens();
    }

    public Token require(TokenType type, ParseElement elt, String errorMessage) {
        if(tokens.match(type)){
            return tokens.consumeToken();
        } else {
            elt.addError(currentToken(), errorMessage);
            return currentToken();
        }
    }

    public Token require(String symbol, ParseElement elt, String errorMessage) {
        if(tokens.match(symbol)){
            return tokens.consumeToken();
        } else {
            elt.addError(currentToken(), errorMessage);
            return currentToken();
        }
    }

    public void setSourcePath(String srcPath) {
        this.srcPath = srcPath;
    }

    public String getSourcePath() {
        return srcPath;
    }
}
