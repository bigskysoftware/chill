package chill.m8.tokenizer;

import chill.script.tokenizer.Tokenizer;

import static java.lang.Character.isWhitespace;

public class M8TestTokenizer extends Tokenizer {

    public M8TestTokenizer(String source) {
        super(source);
    }

    protected boolean scanBeforeAny() {
        if (scanIdLiteral()) {
            return true;
        }
        return scanXPathLiteral();
    }

    @Override
    protected boolean scanBeforeSyntax() {
        return scanClassLiteral();
    }

    private boolean scanClassLiteral() {
        return scanTokenWithStart(".", ChillTestTokenType.CLASS_LITERAL);
    }

    private boolean scanIdLiteral() {
        return scanTokenWithStart("#", ChillTestTokenType.ID_LITERAL);
    }

    private boolean scanXPathLiteral() {
        if (peekMatch("//")) {
            int start = getPosition();
            while (!tokenizationEnd() && !isWhitespace(peek())) {
                takeChar();
            }
            String value = sourceFor(start, getPosition());
            addToken(ChillTestTokenType.XPATH, value, start);
            return true;
        } else {
            return false;
        }
    }

}
