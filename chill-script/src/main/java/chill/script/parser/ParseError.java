package chill.script.parser;

import chill.script.tokenizer.Token;

public class ParseError {
    private Token location;
    private String message;

    public ParseError(Token location, String errorMessage, Object... args) {
        this.location = location;
        this.message = String.format(errorMessage, args);
    }

    public Token getLocation() {
        return location;
    }

    public String getFullMessage() {
        StringBuilder sb = new StringBuilder();
        String lineStart = "  Line " + location.getLine() + ": ";
        sb.append(lineStart);
        sb.append(location.getLineContent());
        sb.append("\n");
        sb.append(" ".repeat(Math.max(0, lineStart.length() + location.getLineOffset())));
        sb.append("^\n");
        sb.append("    ");
        sb.append(message);
        return sb.toString();
    }

    public String getBareMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}
