package chill.script.parser;

import java.util.List;

public class ChillScriptParseException extends RuntimeException {
    private final List<ParseError> parseErrors;
    private final ParseElement parseElement;
    private String source = null;

    public ChillScriptParseException(ParseElement elt) {
        this.parseErrors = elt.collectAllParseErrors();
        parseElement = elt;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder("\n\nThe following ChillScript parse errors were found")
                .append(getSourceString())
                .append(":\n\n");
        for (ParseError error : getParseErrors()) {
            sb.append(error.getFullMessage()).append("\n\n");
        }
        return sb.toString();
    }

    private String getSourceString() {
        if (source != null) {
            return " in " + source;
        } else {
            return "";
        }
    }

    public void setSource(String source) {
        this.source = source;
    }

    public ParseElement getParseElement() {
        return parseElement;
    }

    public List<ParseError> getParseErrors() {
        return parseErrors;
    }
}
