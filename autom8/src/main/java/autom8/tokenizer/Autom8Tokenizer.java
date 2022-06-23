package autom8.tokenizer;

import chill.script.tokenizer.Tokenizer;

import static java.lang.Character.isWhitespace;

public class Autom8Tokenizer extends Tokenizer {

    public Autom8Tokenizer(String source) {
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
        return scanTokenWithStart(".", Autom8TokenType.CLASS_LITERAL);
    }

    private boolean scanIdLiteral() {
        return scanTokenWithStart("#", Autom8TokenType.ID_LITERAL);
    }

    private boolean scanXPathLiteral() {
        if (peekMatch("//")) {
            int start = getPosition();
            while (!tokenizationEnd() && !isWhitespace(peek())) {
                takeChar();
            }
            String value = sourceFor(start, getPosition());
            addToken(Autom8TokenType.XPATH, value, start);
            return true;
        } else {
            return false;
        }
    }

}
