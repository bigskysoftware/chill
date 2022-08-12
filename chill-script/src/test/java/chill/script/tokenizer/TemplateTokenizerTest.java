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

public class TemplateTokenizerTest {

    @Test
    public void basicTemplateTokenizesProperly(){
        assertTokensAre("this is a template", TEMPLATE, EOF);
        assertTokensAre("this is a template ${1}", TEMPLATE, DOLLAR, LEFT_BRACE, NUMBER, RIGHT_BRACE, EOF);
        assertTokensAre("this is a template ${1} foo bar $ ${true}", TEMPLATE, DOLLAR, LEFT_BRACE, NUMBER, RIGHT_BRACE, TEMPLATE, DOLLAR, LEFT_BRACE, SYMBOL, RIGHT_BRACE, EOF);
        assertTokensAre("this is a template #for ", TEMPLATE, EOF);
        assertTokensAre("this is a template\n #for ", TEMPLATE, SHARP, SYMBOL, EOF);
        assertTokensAre("this is a template\n #for \nasdf asdf", TEMPLATE, SHARP, SYMBOL, TEMPLATE, EOF);
    }

    @Test
    public void commandsStartOfLine(){
        assertTokensAre("""
                #for x in lst
                  ${x}
                #end""", SHARP, SYMBOL, SYMBOL, SYMBOL, SYMBOL, TEMPLATE, DOLLAR, LEFT_BRACE, SYMBOL, RIGHT_BRACE, TEMPLATE, SHARP, SYMBOL, EOF);
    }

    @Test
    public void multiLineCommands(){
        assertTokensAre("this is a template\n #for (\nasdf asdf)", TEMPLATE, SHARP, SYMBOL, LEFT_PAREN, SYMBOL, SYMBOL, RIGHT_PAREN, EOF);
        assertTokensAre("this is a template\n #for [\nasdf asdf]", TEMPLATE, SHARP, SYMBOL, LEFT_BRACKET, SYMBOL, SYMBOL, RIGHT_BRACKET, EOF);
        assertTokensAre("this is a template\n #for {\nasdf asdf}", TEMPLATE, SHARP, SYMBOL, LEFT_BRACE, SYMBOL, SYMBOL, RIGHT_BRACE, EOF);

        assertTokensAre("this is a template\n #for (\nasdf\n asdf)", TEMPLATE, SHARP, SYMBOL, LEFT_PAREN, SYMBOL, SYMBOL, RIGHT_PAREN, EOF);
        assertTokensAre("this is a template\n #for [\nasdf\n asdf]", TEMPLATE, SHARP, SYMBOL, LEFT_BRACKET, SYMBOL, SYMBOL, RIGHT_BRACKET, EOF);
        assertTokensAre("this is a template\n #for {\nasdf\n asdf}", TEMPLATE, SHARP, SYMBOL, LEFT_BRACE, SYMBOL, SYMBOL, RIGHT_BRACE, EOF);

        assertTokensAre("this is a template\n #for (\nasdf\n asdf)\n test", TEMPLATE, SHARP, SYMBOL, LEFT_PAREN, SYMBOL, SYMBOL, RIGHT_PAREN, TEMPLATE, EOF);
        assertTokensAre("this is a template\n #for [\nasdf\n asdf]\n test", TEMPLATE, SHARP, SYMBOL, LEFT_BRACKET, SYMBOL, SYMBOL, RIGHT_BRACKET, TEMPLATE, EOF);
        assertTokensAre("this is a template\n #for {\nasdf\n asdf}\n test", TEMPLATE, SHARP, SYMBOL, LEFT_BRACE, SYMBOL, SYMBOL, RIGHT_BRACE, TEMPLATE, EOF);
    }

    protected void assertTokensAre(String src, TokenType... expected) {
        TokenList tokens = tokenizeTemplate(src);
        assertEquals(Arrays.asList(expected), tokens.stream().map(Token::getType).collect(Collectors.toList()));
    }

    protected List<Token> getTokensAsList(String src) {
        Tokenizer tokenizer = new Tokenizer(src);
        return tokenizer.getTokens().stream().collect(Collectors.toList());
    }

    protected void assertTokensAre(String src, String... expected) {
        TokenList tokens = tokenizeTemplate(src);
        assertEquals(Arrays.asList(expected), tokens.stream().map(Token::getStringValue).collect(Collectors.toList()));
    }

    protected TokenList tokenizeTemplate(String src) {
        Tokenizer tokenizer = new Tokenizer(src, Tokenizer.Mode.TEMPLATE);
        return tokenizer.getTokens();
    }


}
