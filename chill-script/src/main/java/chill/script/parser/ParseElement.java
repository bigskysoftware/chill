package chill.script.parser;

import chill.script.tokenizer.Token;
import chill.script.templates.ChillTemplate;

import java.util.LinkedList;
import java.util.List;

public class ParseElement {

    protected ParseElement parent;
    private Token start;
    private Token end;
    private List<ParseElement> children;
    private List<ParseError> parseErrors;

    public ParseElement() {
        this.children = new LinkedList<>();
        this.parseErrors = new LinkedList<>();
    }

    public ChillTemplate getTemplate() {
        if (this.getParent() instanceof ChillTemplate) {
            return (ChillTemplate) this.getParent();
        } else {
            return getParent().getTemplate();
        }
    }

    public void setStart(Token start) {
        this.start = start;
    }

    public void setEnd(Token end) {
        this.end = end;
    }

    public void setToken(Token token) {
        setStart(token);
        setEnd(token);
    }

    public ParseElement getParent() {
        return parent;
    }

    public Token getStart() {
        return start;
    }

    public Token getEnd() {
        return end;
    }

    protected <T extends ParseElement> T addChild(T element) {
        element.parent = this;
        children.add(element);
        return element;
    }

    public <T extends ParseElement> List<T> addChildren(List<T> list) {
        for (ParseElement pe : list) {
            addChild(pe);
        }
        return list;
    }

    public List<ParseElement> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    public void addError(ErrorType errorType, Object... args) {
        addError(errorType, getStart(), args);
    }

    public void addError(ErrorType errorMessage, Token token, Object... args) {
        String errorMessage1 = errorMessage.toString();
        addError(token, errorMessage1, args);
    }

    public void addError(Token token, String message, Object... args) {
        parseErrors.add(new ParseError(token, message, args));
    }

    public String getLineSource() {
        return start != null ? start.getLineContent() : "<none>";
    }

    public String getSourceLocation() {
        return start != null ? start.getSourceLocation() : "";
    }

    public List<ParseError> getParseErrors() {
        return parseErrors;
    }

    public boolean isValid() {
        if (parseErrors.size() > 0) {
            return false;
        } else {
            for (ParseElement child : children) {
                if (!child.isValid()) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<ParseError> collectAllParseErrors() {
        LinkedList<ParseError> errors = new LinkedList<>();
        collectAllParseErrors(errors);
        return errors;
    }

    private void collectAllParseErrors(LinkedList<ParseError> errors) {
        errors.addAll(getParseErrors());
        for (ParseElement child : children) {
            child.collectAllParseErrors(errors);
        }
    }

    public String getSource() {
        return start.getSourceTo(end);
    }
}
