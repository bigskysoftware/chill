package chill.script.tokenizer;

import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenList;
import chill.script.tokenizer.TokenType;
import chill.script.tokenizer.Tokenizer;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static chill.script.tokenizer.TokenType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TokenizerTest {

    @Test
    public void basics(){
        assertTokensAre("set x to 10", SYMBOL, SYMBOL, SYMBOL, NUMBER, EOF);
    }

    @Test
    public void strings(){
        assertTokensAre("'foo'", STRING, EOF);
        assertTokensAre("\"foo\"", STRING, EOF);
    }

    @Test
    public void urls(){
        assertTokensAre("http://foo.com", ABSOLUTE_URL, EOF);
        assertTokensAre("https://foo.com", ABSOLUTE_URL, EOF);
        assertTokensAre("(https://foo.com)", LEFT_PAREN, ABSOLUTE_URL, RIGHT_PAREN, EOF);
        assertTokensAre("[https://foo.com]", LEFT_BRACKET, ABSOLUTE_URL, RIGHT_BRACKET, EOF);
        assertTokensAre("{https://foo.com}", LEFT_BRACE, ABSOLUTE_URL, RIGHT_BRACE, EOF);
    }

    @Test
    public void paths(){
        assertTokensAre("/foo/bar.html", PATH, EOF);
        assertTokensAre("/foo/\\ bar.html", PATH, EOF);
        assertTokensAre("(/foo/bar.html)", LEFT_PAREN, PATH, RIGHT_PAREN, EOF);
        assertTokensAre("[/foo/bar.html]", LEFT_BRACKET, PATH, RIGHT_BRACKET, EOF);
        assertTokensAre("{/foo/bar.html}", LEFT_BRACE, PATH, RIGHT_BRACE, EOF);
    }

    @Test
    public void relativePaths(){
        assertTokensAre("./foo/bar.html", PATH, EOF);
        assertTokensAre("./foo/\\ bar.html", PATH, EOF);
        assertTokensAre("(./foo/bar.html)", LEFT_PAREN, PATH, RIGHT_PAREN, EOF);
        assertTokensAre("[./foo/bar.html]", LEFT_BRACKET, PATH, RIGHT_BRACKET, EOF);
        assertTokensAre("{./foo/bar.html}", LEFT_BRACE, PATH, RIGHT_BRACE, EOF);
    }

    protected void assertTokensAre(String src, TokenType... expected) {
        TokenList tokens = tokenize(src);
        assertEquals(Arrays.asList(expected), tokens.stream().map(Token::getType).collect(Collectors.toList()));
    }

    protected void assertTokensAre(String src, String... expected) {
        TokenList tokens = tokenize(src);
        assertEquals(Arrays.asList(expected), tokens.stream().map(Token::getStringValue).collect(Collectors.toList()));
    }

    protected TokenList tokenize(String src) {
        Tokenizer tokenizer = new Tokenizer(src);
        return tokenizer.getTokens();
    }

}
