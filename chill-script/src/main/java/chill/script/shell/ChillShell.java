package chill.script.shell;

import chill.script.expressions.Expression;
import chill.script.parser.ChillScriptParser;
import chill.script.parser.ChillScriptProgram;
import chill.script.runtime.ChillScriptRuntime;
import chill.utils.NiceList;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static chill.utils.TheMissingUtils.safely;

public class ChillShell {

    public static final String PROMPT = "chill > ";
    private final ChillScriptRuntime runtime;
    private final ChillScriptParser parser;

    public ChillShell() {
        runtime = new ChillScriptRuntime();
        parser = new ChillScriptParser();
    }

    public static void main(String[] args) {
        new ChillShell().simple();
    }

    public void simple() {
        safely(() -> {
            var reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                // Reading data using readLine
                print(PROMPT);
                String line = reader.readLine();
                if (execLine(line)){
                    break;
                }
            }
        });
    }

    public void drive(InputStreamReader source) {
        safely(() -> {
            var reader = new BufferedReader(source);
            while (true) {
                // Reading data using readLine
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (execLine(line)){
                    break;
                }
            }
        });
    }

    private boolean execLine(String line) {
        try {
            String stripped = line.strip();
            if (stripped.equals("quit") || stripped.equals("exit")) {
                return true;
            }
            Object programOrExpression = parser.parseProgramOrExpression(line);
            if (programOrExpression instanceof ChillScriptProgram) {
                ChillScriptProgram program = (ChillScriptProgram) programOrExpression;
                runtime.execute(program);
            } else if(programOrExpression instanceof Expression) {
                Expression expr = (Expression) programOrExpression;
                println(expr.evaluate(runtime));
            }
        } catch (Exception e) {
            println(e.getMessage());
            println(stacktraceAsString(e));
        }
        return false;
    }

    private String stacktraceAsString(Exception e) {
        return new NiceList(e.getStackTrace()).map(Object::toString).join("\n");
    }

    public void jline() {
        safely(() -> {
            Terminal terminal = TerminalBuilder.builder().build();
            terminal.echo(false);
            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();
            while(true){
                String line = reader.readLine(PROMPT);
                if (execLine(line)) {
                    break;
                }
            }
        });
    }

    private void print(String prompt) {
        System.out.print(prompt);
    }

    private void println(Object message) {
        System.out.println(message);
    }
}
