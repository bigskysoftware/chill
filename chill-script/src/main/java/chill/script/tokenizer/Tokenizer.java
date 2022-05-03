package chill.script.tokenizer;

import chill.utils.TheMissingUtils;

import static chill.script.tokenizer.TokenType.*;
import static java.lang.Character.isWhitespace;

public class Tokenizer {

    TokenList tokenList;
    String src;
    int position = 0;
    int line = 1;
    int lineOffset = 0;

    public enum Mode {
        NORMAL,
        TEMPLATE
    }

    enum TemplateMode {
        TEMPLATE,
        COMMAND,
        EXPRESSION
    }

    Mode mode;
    TemplateMode templateMode = TemplateMode.TEMPLATE;
    int curlyCount = 0;

    public Tokenizer(String source) {
        this(source, Mode.NORMAL);
    }

    public Tokenizer(String source, Mode mode) {
        this.mode = mode;
        src = source;
        if (peek() == '#') {
            templateMode = TemplateMode.COMMAND;
        } else if(peek() == '$') {
            templateMode = TemplateMode.EXPRESSION;
        }
        tokenList = new TokenList(this);
        tokenize();
    }

    private void tokenize() {
        if (mode == Mode.TEMPLATE) {
            while (!tokenizationEnd()) {
                if (templateMode == TemplateMode.TEMPLATE) {
                    scanTemplateContent();
                }  else if(templateMode == TemplateMode.COMMAND) {
                    scanCommand();
                }  else {
                    consumeWhitespace();
                    if (!tokenizationEnd()) {
                        scanToken();
                    }
                }
            }
        } else {
            while (!tokenizationEnd()) {
                consumeWhitespace();
                scanToken();
                consumeWhitespace();
            }
        }
        tokenList.addToken(EOF, "<EOF>", position, position, line, lineOffset);
    }

    private void scanToken() {
        if (scanBeforeAny()) {
            return;
        }
        if(scanNumber()) {
            return;
        }
        if(scanString()) {
            return;
        }
        if(scanAbsoluteURL()) {
            return;
        }
        if(scanPath()) {
            return;
        }
        if(scanIdentifier()) {
            return;
        }
        if (scanBeforeSyntax()) {
            return;
        }
        scanSyntax();
    }

    protected boolean scanBeforeSyntax() {
        return false;
    }

    protected boolean scanBeforeAny() {
        return false;
    }

    private boolean scanAbsoluteURL() {
        if( peekMatch("http://") || peekMatch("https://")) {
            int start = position;
            while (!tokenizationEnd() && !isWhitespace(peek()) && !syntaxTerminal(peek())) {
                takeChar();
            }
            String value = src.substring(start, position);
            tokenList.addToken(TokenType.ABSOLUTE_URL, value, start, position, line, lineOffset);
            return true;
        } else {
            return false;
        }
    }

    private boolean syntaxTerminal(char c) {
        return c == ']' || c == ')' || c == '}';
    }

    private boolean scanPath() {
        if (scanTokenWithStart("/", PATH)) {
            return true;
        }
        return scanTokenWithStart("./", PATH);
    }

    private boolean scanString() {
        if(peek() == '"' || peek() == '\'') {
            int start = position;
            char c = takeChar();
            int originalLineOffset = lineOffset;
            char stringStart = c;
            do {
                if (tokenizationEnd()) {
                    tokenList.addToken(ERROR, src.substring(start + 1, position), start, position, line, lineOffset);
                    return true;
                }
                c = takeChar();
                if (c == '\\') {
                    if (!tokenizationEnd()) {
                        takeChar(); // skip the next char as escaped
                    }
                }
            } while (c != stringStart);
            tokenList.addToken(STRING, processString(originalLineOffset, src.substring(start + 1, position - 1)), start, position, line, lineOffset);
            return true;
        } else {
            return false;
        }
    }

    private String processString(int stripLeadingSpaces, String str) {
        if (str.contains("\n")) {
            String[] lines = str.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (line.length() > stripLeadingSpaces &&
                        line.substring(0, stripLeadingSpaces).matches("\s*")) {
                    lines[i] = line.substring(stripLeadingSpaces);
                }
            }
            str = TheMissingUtils.join(lines, "\n");
        }
        str = str.replace("\\n", "\n");
        str = str.replace("\\t", "\t");
        str = str.replace("\\r", "\r");
        str = str.replace("\\f", "\f");
        str = str.replace("\\b", "\b");
        str = str.replace("\\\"", "\"");
        str = str.replace("\\'", "'");
        str = str.replace("\\\\", "\\");
        return str;
    }

    private boolean scanTemplateContent() {
        int start = position;
        var buffer = new StringBuilder();
        while(!tokenizationEnd()) {
            if (commandStart()) {
                buffer.append("\n"); // consume newline as part of the template content
                templateMode = TemplateMode.COMMAND;
                break;
            }
            if (peek() == '$' && peek(1) == '{') {
                templateMode = TemplateMode.EXPRESSION;
                break;
            }
            buffer.append(takeChar());
        }
        tokenList.addToken(TEMPLATE, buffer.toString(), start, position, line, lineOffset);
        return true;
    }

    private boolean scanCommand() {

        int start = position;
        matchAndConsume('#');
        tokenList.addToken(SHARP, "#", start, position, line, lineOffset);
        if(!scanIdentifier()){
            tokenList.addToken(ERROR, src.substring(start + 1, position), start, position, line, lineOffset);
        }

        while (!tokenizationEnd() && peek() != '\n') {
            consumeWhitespace();
            if (!tokenizationEnd() && peek() != '\n') {
                scanToken();
            }
        }
        if (!tokenizationEnd()) {
            if (!commandStart()) {
                takeChar(); // take the command terminating newline
                templateMode = TemplateMode.TEMPLATE;
            }
        }
        return true;
    }

    private boolean commandStart() {
        if (peek() == '\n') {
            int offset = position;
            while (offset < src.length() && isWhitespace(src.charAt(offset))) {
                offset++;
            }
            if (offset < src.length() && src.charAt(offset) == '#') {
                position = offset;
                return true;
            }
        }
        return false;
    }

    private boolean scanIdentifier() {
        if( isAlpha(peek())) {
            int start = position;
            while (isAlphaNumeric(peek())) {
                takeChar();
            }
            String value = src.substring(start, position);
            tokenList.addToken(SYMBOL, value, start, position, line, lineOffset);
            return true;
        } else {
            return false;
        }
    }

    private boolean scanNumber() {
        if(isDigit(peek())) {
            int start = position;
            while (isDigit(peek())) {
                takeChar();
            }
            tokenList.addToken(NUMBER, src.substring(start, position), start, position, line, lineOffset);
            return true;
        } else {
            return false;
        }
    }

    private void scanSyntax() {

        int start = position;
        if(matchAndConsume('(')) {
            tokenList.addToken(LEFT_PAREN, "(", start, position, line, lineOffset);
        } else if(matchAndConsume(')')) {
            tokenList.addToken(RIGHT_PAREN, ")", start, position, line, lineOffset);
        } else if(matchAndConsume('[')) {
            tokenList.addToken(LEFT_BRACKET, "[", start, position, line, lineOffset);
        } else if(matchAndConsume(']')) {
            tokenList.addToken(RIGHT_BRACKET, "]", start, position, line, lineOffset);
        } else if(matchAndConsume('{')) {
            curlyCount++;
            tokenList.addToken(LEFT_BRACE, "{", start, position, line, lineOffset);
        } else if(matchAndConsume('}')) {
            curlyCount--;
            if (curlyCount == 0) {
                templateMode = TemplateMode.TEMPLATE;
            }
            tokenList.addToken(RIGHT_BRACE, "}", start, position, line, lineOffset);
        } else if(matchAndConsume('$')) {
            tokenList.addToken(DOLLAR, "$", start, position, line, lineOffset);
        } else if(matchAndConsume('#')) {
            tokenList.addToken(SHARP, "#", start, position, line, lineOffset);
        } else if(matchAndConsume(':')) {
            tokenList.addToken(COLON, ":", start, position, line, lineOffset);
        } else if(matchAndConsume(',')) {
            tokenList.addToken(COMMA, ",", start, position, line, lineOffset);
        } else if(matchAndConsume('.')) {
            tokenList.addToken(DOT, ".", start, position, line, lineOffset);
        } else if(matchAndConsume('+')) {
            tokenList.addToken(PLUS, "+", start, position, line, lineOffset);
        } else if(matchAndConsume('-')) {
            tokenList.addToken(MINUS, "-", start, position, line, lineOffset);
        } else if(matchAndConsume('*')) {
            tokenList.addToken(STAR, "*", start, position, line, lineOffset);
        } else if(matchAndConsume('/')) {
            if (matchAndConsume('/')) {
                // consume to end of line
                while(peek() != '\n' && !tokenizationEnd()) takeChar();
            } else {
                tokenList.addToken(SLASH, "/", start, position, line, lineOffset);
            }
        } else if(matchAndConsume('!')) {
            if (matchAndConsume('=')) {
                tokenList.addToken(BANG_EQUAL, "!=", start, position, line, lineOffset);
            } else {
                tokenList.addToken(ERROR, "<Unexpected Token: [!]>", start, position, line, lineOffset);
            }
        } else if(matchAndConsume('=')) {
            if (matchAndConsume('=')) {
                tokenList.addToken(EQUAL_EQUAL, "==", start, position, line, lineOffset);
            } else {
                tokenList.addToken(EQUAL, "=", start, position, line, lineOffset);
            }
        } else if(matchAndConsume('<')) {
            if (matchAndConsume('=')) {
                tokenList.addToken(LESS_EQUAL, "<=", start, position, line, lineOffset);
            } else {
                tokenList.addToken(LESS, "<", start, position, line, lineOffset);
            }
        } else if(matchAndConsume('>')) {
            if (matchAndConsume('=')) {
                tokenList.addToken(GREATER_EQUAL, ">=", start, position, line, lineOffset);
            } else {
                tokenList.addToken(GREATER, ">", start, position, line, lineOffset);
            }
        } else {
            tokenList.addToken(ERROR, "<Unexpected Token: [" + takeChar() + "]>", start, position, line, lineOffset);
        }
    }

    private void consumeWhitespace() {
        // TODO update line and lineOffsets
        while (!tokenizationEnd()) {
            char c = peek();
            if (c == ' ' || c == '\r' || c == '\t') {
                position++;
                lineOffset++;
                continue;
            } else if (c == '\n') {
                if (templateMode != TemplateMode.COMMAND) {
                    position++;
                    line++;
                    lineOffset = 0;
                    continue;
                }
            }
            break;
        }
    }

    //===============================================================
    // Utility functions
    //===============================================================

    protected char peek() {
        if (tokenizationEnd()) return '\0';
        return src.charAt(position);
    }

    private char peek(int n) {
        if (position + n < src.length()) {
            return src.charAt(position + n);
        } else {
            return '\0';
        }
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    protected char takeChar() {
        char c = src.charAt(position);
        position++;
        lineOffset++;
        if (c == '\n') {
            lineOffset = 0;
            line++;
        }
        return c;
    }

    protected boolean tokenizationEnd() {
        return position >= src.length();
    }

    protected void addToken(TokenType type, String value, int start) {
        tokenList.addToken(type, value, start, position, line, lineOffset);
    }

    protected String sourceFor(int start, int end) {
        return src.substring(start, end);
    }

    protected int getPosition() {
        return position;
    }

    public boolean matchAndConsume(char c) {
        if (peek() == c) {
            takeChar();
            return true;
        }
        return false;
    }

    public boolean peekMatch(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (peek(i) != c) {
                return false;
            }
        }
        return true;
    }

    protected boolean scanTokenWithStart(String startChars, TokenType type) {
        if (peekMatch(startChars)) {
            int start = position;
            while (!tokenizationEnd() && !isWhitespace(peek()) && !syntaxTerminal(peek())) {
                char c = takeChar();
                if (c == '\\' && !tokenizationEnd()) {
                    takeChar();
                }
            }
            String value = src.substring(start, position);
            tokenList.addToken(type, value, start, position, line, lineOffset);
            return true;
        } else {
            return false;
        }
    }

    public TokenList getTokens() {
        return tokenList;
    }

    @Override
    public String toString() {
        if (tokenizationEnd()) {
            return src + "-->[]<--";
        } else {
            return src.substring(0, position) + "-->[" + peek() + "]<--" +
                    ((position == src.length() - 1) ? "" :
                            src.substring(position + 1, src.length()));
        }
    }

}
