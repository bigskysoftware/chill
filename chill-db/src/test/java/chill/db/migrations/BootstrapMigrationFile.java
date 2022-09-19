package chill.db.migrations;

import chill.db.ChillMigrations;

public class BootstrapMigrationFile extends ChillMigrations {

    public final ChillMigration migration_2022_03_08_16_02_26 = new ChillMigration("add user table"){

        protected void up() {
            exec("CREATE TABLE user (\n" +
                    "                      id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "                      first_name VARCHAR(250),\n" +
                    "                      last_name VARCHAR(250),\n" +
                    "                      email VARCHAR(250) DEFAULT NULL,\n" +
                    "                      password VARCHAR(250) DEFAULT NULL,\n" +
                    "                      age INTEGER\n" +
                    "                    );","DROP TABLE user;");
        }

    };

}
