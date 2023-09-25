package chill.config;


import chill.db.ChillMigrations;
import chill.env.ChillEnv;
import chill.env.ChillMode;
import chill.script.shell.ChillShell;
import chill.util.DirectoryWatcher;
import chill.utils.TheMissingUtils;
import chill.web.ChillHelper;
import chill.workers.Foreman;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import chill.web.WebServer;

import java.nio.file.*;

import static chill.utils.TheMissingUtils.safely;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static chill.utils.ChillLogs.*;
public class ChillApp {

    public static final String UNSET_VALUE = "unset";
    static String BANNER = "\n" +
            "                                                                                        \\s\n" +
            "                                                                                        \\s\n" +
            "            ==========================================================\n" +
            "                 ________  ___  ___  ___  ___       ___         \\s\n" +
            "                |\\\\   ____\\\\|\\\\  \\\\|\\\\  \\\\|\\\\  \\\\|\\\\  \\\\     |\\\\  \\\\        \\s\n" +
            "                \\\\ \\\\  \\\\___|\\\\ \\\\  \\\\\\\\\\\\  \\\\ \\\\  \\\\ \\\\  \\\\    \\\\ \\\\  \\\\       \\s\n" +
            "                 \\\\ \\\\  \\\\    \\\\ \\\\   __  \\\\ \\\\  \\\\ \\\\  \\\\    \\\\ \\\\  \\\\      \\s\n" +
            "                  \\\\ \\\\  \\\\____\\\\ \\\\  \\\\ \\\\  \\\\ \\\\  \\\\ \\\\  \\\\____\\\\ \\\\  \\\\____ \\s\n" +
            "                   \\\\ \\\\_______\\\\ \\\\__\\\\ \\\\__\\\\ \\\\__\\\\ \\\\_______\\\\ \\\\_______\\\\\n" +
            "                    \\\\|_______|\\\\|__|\\\\|__|\\\\|__|\\\\|_______|\\\\|_______|\\s\n" +
            "                                                                                        \\s\n" +
            "            ==========================================================\n" +
            "                                  Let's chill..";

    @Option(names = {"--console"}, arity = "0..1", description = "start console", defaultValue = UNSET_VALUE, fallbackValue = "jline")
    String console;

    @Option(names = {"--migrations"}, arity = "0..1", description = "Execute a migration command or start the console", defaultValue = UNSET_VALUE, fallbackValue = "console")
    String migrationCommand;

    @Option(names = {"--port"}, arity = "0..1", description = "Port to start the web application on", defaultValue = UNSET_VALUE, fallbackValue = "8800")
    String port;

    @Option(names = {"-m", "--mode"}, description = "The mode that the system is started in")
    ChillMode mode;

    @Option(names = {"-w", "--web"}, description = "Start the chill web server")
    boolean web;

    @Option(names = {"-k", "--worker"}, description = "Start the chill job processor")
    boolean workers;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display a help message")
    boolean helpRequested = false;

    public void migrationConsole() {
        exec(new String[]{"--migrations"});
    }

    public void reload() {
        safely(() -> Thread.sleep(300));
        WebServer.INSTANCE.restart();
        ChillHelper.INSTANCE.reload();
    }

    public final ChillApp exec(String[] args) {

        SLF4JSupport.init();

        CommandLine cmd = new CommandLine(this);

        cmd.parseArgs(args);

        info(BANNER);

        if (helpRequested) {
            cmd.usage(cmd.getOut());
            System.exit(cmd.getCommandSpec().exitCodeOnUsageHelp());
        }

        ChillEnv.MODE.initialize(this);

        // if no arguments are passed in, we are in dev mode
        if (mode == null && !web && !workers) {
            info("No mode, web or worker argument passed in.  Starting in dev mode.");

            workers = true;
            web = true;

            Path tomlFilePath = Path.of("src/main/resources/config/chill.toml");
            if (tomlFilePath.toFile().exists()) {
                Path classesDir = Path.of("target/classes");
                if (classesDir.toFile().exists()) {
                    // reload config on redefinition of /web/* classes
                    DirectoryWatcher.watch(classesDir, evt -> {
                        String modifiedFile = evt.second.toAbsolutePath().toString();
                        if (evt.first.kind() == ENTRY_MODIFY && modifiedFile.endsWith(".class") && modifiedFile.contains("/web/")) {
                            info("Detected change to web config, reloading...");
                            reload();
                        }
                    });
                } else {
                    warn(classesDir + " does not exist, no hot-reloading of routes, etc.");
                }
            } else {
                warn(tomlFilePath + " does not exist, is the app running in the right working directory?");
            }
        }

        // initialize Chill Environment
        ChillEnv.init(this);

        // init db connections
        ChillDataSource.init();

        if (!UNSET_VALUE.equals(migrationCommand)) {
            ChillMigrations.execute(migrationCommand);
            System.exit(0);
            return this;
        }

        if (!UNSET_VALUE.equals(console)) {
            ChillShell chillShell = new ChillShell();
            if ("jline".equals(console)) {
                chillShell.jline();
            } else {
                chillShell.simple();
            }
            System.exit(0);
            return this;
        }

        ChillMigrations.checkPendingMigrations(ChillEnv.MODE.isDev());

        if (web) {
            info("Starting web node");
            ChillHelper.INSTANCE.init();
            WebServer.INSTANCE.start();
        }

        if (workers) {
            info("Starting worker node");
            Foreman.INSTANCE.initWorkersAndWeb();
        } else {
            Foreman.INSTANCE.initClient();
        }

        afterSystemInitialization();

        return this;
    }

    protected void afterSystemInitialization() {
    }

    public void chillConsole() {
        exec(new String[]{"--console", "simple"});
    }

    public String getFieldValue(String field) {
        return (String) TheMissingUtils.getFieldValue(this, field);
    }
}