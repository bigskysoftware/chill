package chill.script.parser;

import chill.script.templates.ChillTemplate;
import chill.script.templates.ChillTemplates;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ErrorsTest {

    @Test
    public void bootstrap(){
        ChillScriptParseException parseException = getParseErrors("""
                foo 10 herbaderb
                print 10
                foo 10 herbaderb
                """);
        assertEquals(2, parseException.getParseErrors().size());
    }

    @Test
    public void template(){
        ChillScriptParseException parseException = getParseErrorsForTemplate("/errors/basic-error.html");
        System.out.println(parseException.getMessage());
        assertNotNull(parseException);
    }

    private ChillScriptParseException getParseErrors(String src) {
        ChillScriptParser parser = new ChillScriptParser();
        try {
            parser.parseProgram(src);
            throw new IllegalStateException("Source should not have compiled:\n\n" + src);
        } catch (ChillScriptParseException e) {
            return e;
        }
    }

    private ChillScriptParseException getParseErrorsForTemplate(String file) {
        ChillTemplates chillTemplates = new ChillTemplates().addToPath("file:src/test/resources/templates");
        try {
            ChillTemplate chillTemplate = chillTemplates.get(file);
            throw new IllegalStateException("Source should not have compiled:\n\n" + chillTemplate.getLineSource());
        } catch (ChillScriptParseException e) {
            return e;
        }
    }

}
