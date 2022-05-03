package chill.script;

import chill.script.expressions.Expression;
import chill.script.expressions.PropertyAccessExpression;
import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.templates.ChillTemplate;
import chill.script.templates.ChillTemplates;
import chill.utils.TheMissingUtils;
import demo.Book;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class PerfTest {

    @Test
    @Disabled
    public void properties() throws NoSuchFieldException, IllegalAccessException {
        int ITERATIONS = 1000000;
        ChillScriptParser chillScriptParser = new ChillScriptParser();
        Expression propAccess = chillScriptParser.parseExpression("str.length");
        ChillScriptRuntime runtime = new ChillScriptRuntime("str", "foo");

        long time3 = TheMissingUtils.time(() -> {
            for (int i = 0; i < ITERATIONS; i++) {
                propAccess.evaluate(runtime);
            }
        });
        System.out.println("Time typesystem : " + time3);

    }

    @Test
    @Disabled
    public void templates()  {
        int BOOKS = 25;
        int ITERATIONS = 200000;

        List<Book> books = new LinkedList<>();
        for (int i = 0; i < BOOKS; i++) {
            books.add(new Book());
        }

        // Tell jte where your template files are located
        var codeResolver = new DirectoryCodeResolver(Path.of("src", "test", "resources"));

        // Create the template engine (usually once per application)
        var templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);


        // Render
        long time = TheMissingUtils.time(() -> {
            for (int i = 0; i < ITERATIONS; i++) {
                var output = new StringOutput();
                templateEngine.render("view/perf/books.jte", books, output);
            }
        });
        System.out.println( "JTE time :           " + time);
        System.out.println( "JTE templates per second :           " + (double) ITERATIONS / (double) time * 1000.);

        ChillTemplates chillTemplates = new ChillTemplates()
                .withCaching()
                .addToPath("file:src/test/resources/view");

        time = TheMissingUtils.time(() -> {
                    for (int i = 0; i < ITERATIONS; i++) {
                        ChillTemplate chillTemplate = chillTemplates.get("/perf/books.html");
                        chillTemplate.render("books", books);
                    }
                }
        );
        System.out.println("ChillTemplate time : " + time);
        System.out.println( "ChillTemplate templates per second :           " + (double) ITERATIONS / (double) time * 1000.0);
    }

}
