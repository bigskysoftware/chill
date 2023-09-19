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
    public static final class Location {
        private final int index;

        public Location(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    private final Map<String, CommandParser> commands;
    private final Map<String, ExpressionParser> expressions;
    private final List<IndirectExpressionParser> indirectExpressions;
    private final List<IndirectExpressionParser> postFixExpressions;
    private final List<String> primaryExpressions = new LinkedList<>();

    private TokenList tokens;
    private String src;
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

        List<Command> children = new LinkedList<>();
        while (matchSequence("depend", "on")) {
            children.add(DependOnCommand.parse(this));
        }

        children.addAll(parseCommandList());
        program.setBody(children);
        program.setEnd(lastMatch());
        if (program.isValid()) {
            return program;
        } else {
            throw new ChillScriptParseException(program);
        }
    }

    public static Expression parseExpression(String src) {
        ChillScriptParser parser = new ChillScriptParser();
        parser.initTokens(src);
        Expression expression = parser.parse("expression");
        if (expression.isValid()) {
            return expression;
        } else {
            throw new ChillScriptParseException(expression);
        }
    }

    public static Command parseCommand(String src) {
        ChillScriptParser parser = new ChillScriptParser();
        parser.initTokens(src);
        Command command = parser.parseCommand();
        if (command.isValid()) {
            return command;
        } else {
            throw new ChillScriptParseException(command);
        }
    }

    protected final void initTokens(String src) {
        Tokenizer tokenizer = getTokenizer(src);
        this.src = src;
        this.tokens = tokenizer.getTokens();
    }

    protected Tokenizer getTokenizer(String src) {
        Tokenizer tokenizer = new Tokenizer(src);
        tokenizer.setSourcePath(srcPath);
        return tokenizer;
    }

    public List<Command> parseCommandList(String... delimiters) {
        LinkedList<Command> commands = new LinkedList<>();

        while (moreTokens() && !match(delimiters)) {
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
            if (command == null) {
                return new ErrorCommand("Command parser returned null", currentToken());
            }
            match("then"); // optional 'then' divider
            return command;
        } else {
            Location location = this.getCurrentLocation();
            try {
                Expression expression = this.parse("expression");
                if (expression != null) {
                    return new ExpressionCommand(expression);
                }
            } catch (Throwable ignored) {}
            this.restoreLocation(location);

            if (currentToken().getType() == TokenType.SYMBOL) {
                return new ErrorCommand("Unknown command: " + currentToken().getStringValue(), currentToken());
            } else {
                return new ErrorCommand("Unexpected token: " + currentToken().getStringValue(), currentToken());
            }
        }
    }

    public Location getCurrentLocation() {
        return new Location(tokens.getCurrentTokenIndex());
    }

    public void restoreLocation(Location location) {
        tokens.setCurrentTokenIndex(location.getIndex());
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
        registerCommand("println", PrintCommand::parse);
        registerCommand("set", SetCommand::parse);
        registerCommand("let", SetCommand::parse);
        registerCommand("if", IfCommand::parse);
        registerCommand("for", ForCommand::parse);
        registerCommand("repeat", RepeatCommand::parse);
        registerCommand("fun", FunctionCommand::parse);
        registerCommand("return", ReturnCommand::parse);
    }

    private void initExpressionCoreGrammar() {
        // Core grammar
        registerExpression("expression", this::parsePostfixExpressions);
        registerExpression("equalityExpression", parser -> ComparisonExpression.parse(parser, ComparisonExpression.Level.Equality));
        registerExpression("logicalExpression", parser -> ComparisonExpression.parse(parser, ComparisonExpression.Level.Logical));
        registerExpression("ordinalExpression", parser -> ComparisonExpression.parse(parser, ComparisonExpression.Level.Ordinal));
        registerExpression("collectionExpression", parser -> ComparisonExpression.parse(parser, ComparisonExpression.Level.Collection));
        registerExpression("additiveExpression", AdditiveExpression::parse);
        registerExpression("factorExpression", FactorExpression::parse);
        registerExpression("unaryExpression", UnaryExpression::parse);
        registerExpression("indirectExpression", this::parseIndirectExpressions);
        registerExpression("primaryExpression", (parser) -> parser.parse(primaryExpressions));

        // Core primary expressions
        registerPrimaryExpression("string", StringLiteralExpression::parse);
        registerPrimaryExpression("number", NumberLiteralExpression::parse);
        registerPrimaryExpression("boolean", BooleanLiteralExpression::parse);
        registerPrimaryExpression("constructor", ConstructorExpression::parse);
        registerPrimaryExpression("sql", SqlExpression::parse);
        registerPrimaryExpression("identifier", IdentifierExpression::parse);
        registerPrimaryExpression("listLiteral", ListLiteralExpression::parse);
        registerPrimaryExpression("mapLiteral", MapLiteralExpression::parse);
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
            Expression expr = exprParser.parseExpression(this);
            if (expr != null) {
                return expr;
            }
        }
        return null;
    }

    public Expression parse(List<String> expressionTypes) {
        for (String exprType : expressionTypes) {
            Expression expr = expressions.get(exprType).parseExpression(this);
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

    public boolean match(String... identifier) {
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

    public void advance(int n) {
        tokens.advance(n);
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
            return consumeToken();
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

    public Token produceToken() {
        return tokens.produceToken();
    }

    public String getSubstringBetween(Token start, Token end) {
        return src.substring(start.getStart(), end.getStart());
    }

    public boolean matchAndConsumeSequence(Object... items) {
        if (matchSequence(items)) {
            advance(items.length);
            return true;
        } else {
            return false;
        }
    }
}
