package config;

import chill.config.ChillApp;

public class App extends ChillApp {

    public static void main(String[] args) {
        new App().exec(args);
    }

    static class MigrationConsole {
        public static void main(String[] args) {
            new App().migrationConsole();
        }
    }

    static class ChillConsole {
        public static void main(String[] args) {
            new App().chillConsole();
        }
    }
}
