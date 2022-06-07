package chill.m8;

import chill.m8.parser.M8Parser;
import chill.m8.runtime.ChillTestRuntime;
import io.github.bonigarcia.wdm.WebDriverManager;
import picocli.CommandLine;

import static picocli.CommandLine.*;

@Command()
public class M8 {

    private static boolean initialized = false;
    @Option(names = "--help", usageHelp = true, description = "display this help and exit")
    boolean help;

    public static synchronized void init(String driver) {
        if (!initialized) {
            if (driver != null) {
                System.setProperty("webdriver.chrome.driver", driver);
            } else {
                //TODO - better browser support setup (e.g. support edge, firefox, etc)
                WebDriverManager.chromedriver().setup();
            }
            initialized = true;
        }
    }

    public void init() {
        init(System.getenv("WEBDRIVER"));
    }

    public static void main(String[] args) {
        var m8 = new M8();
        new CommandLine(m8).parseArgs(args);
        CommandLine.usage(m8, System.out);
    }

    public static void execute(String src) {
        var parser = new M8Parser();
        var program = parser.parseProgram(src);
        var runtime = new ChillTestRuntime();
        runtime.execute(program);
    }
}
