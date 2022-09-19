package chill.script.tokenizer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static chill.script.tokenizer.TokenType.SYMBOL;

public class TokenList implements Iterable<Token> {

    private final Tokenizer tokenizer;
    List<Token> tokens = new LinkedList<>();
    int currentToken = 0;
    private Token lastMatch;

    public TokenList(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    void addToken(TokenType type, String stringValue, int start, int end, int line, int lineOffset) {
        tokens.add(new Token(start, end, line, lineOffset - (end - start), stringValue, type, tokenizer));
    }

    public Token getCurrentToken() {
        return tokens.get(currentToken);
    }

    public Token consumeToken() {
        return tokens.get(currentToken++);
    }

    public boolean match(String identifier) {
        return match(0, identifier);
    }

    public boolean match(String... identifiers) {
        return match(0, identifiers);
    }

    public boolean match(int offset, String... identifiers) {
        Token token = nthToken(offset);
        for (var identifier : identifiers) {
            if (token.getType().equals(SYMBOL) &&
                    token.getStringValue().equals(identifier)) {
                return true;
            }
        }
        return false;
    }

    public boolean match(int offset, String identifier) {
        Token token = nthToken(offset);
        if (token.getType().equals(SYMBOL) &&
                token.getStringValue().equals(identifier)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean match(TokenType... type) {
        return match(0, type);
    }

    public boolean match(int offset, TokenType... type) {
        Token token = nthToken(offset);
        for (TokenType tokenType : type) {
            if (token.getType().equals(tokenType)) {
                return true;
            }
        }
        return false;
    }

    private Token nthToken(int offset) {
        int index = currentToken + offset;
        if (tokens.size() <= index) {
            return tokens.get(tokens.size() - 1);
        } else {
            return tokens.get(index);
        }
    }

    public Token lastMatch() {
        return lastMatch;
    }

    public void reset() {
        currentToken = 0;
        lastMatch = null;
    }

    public Stream<Token> stream() {
        return tokens.stream();
    }

    public boolean hasMoreTokens() {
        return currentToken < tokens.size() - 1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (i == currentToken) {
                sb.append("-->[");
            }
            sb.append(token.getStringValue());
            if (i == currentToken) {
                sb.append("]<--");
            }
            sb.append(" ");
        }
        return sb.toString();
    }

    public Token firstToken() {
        return tokens.get(0);
    }

    @Override
    public Iterator<Token> iterator() {
        return tokens.iterator();
    }

    public boolean matchSequence(Object... tokenSeq) {
        for (int i = 0; i < tokenSeq.length; i++) {
            Object o = tokenSeq[i];
            if (o instanceof TokenType) {
                TokenType type = (TokenType) o;
                if(!match(i, type)){
                    return false;
                }
            } else {
                if(!match(i, String.valueOf(o))){
                    return false;
                }
            }
        }
        return true;
    }
}