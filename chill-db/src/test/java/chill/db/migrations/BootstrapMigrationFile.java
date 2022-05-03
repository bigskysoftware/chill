package chill.db.migrations;

import chill.db.ChillMigrations;

public class BootstrapMigrationFile extends ChillMigrations {

    public final ChillMigration migration_2022_03_08_16_02_26 = new ChillMigration("add user table"){

        protected void up() {
            exec("""
                    CREATE TABLE user (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      first_name VARCHAR(250),
                      last_name VARCHAR(250),
                      email VARCHAR(250) DEFAULT NULL,
                      password VARCHAR(250) DEFAULT NULL,
                      age INTEGER
                    );
                    """);
        }

        protected void down() {
            exec("DROP TABLE user");
        }
    };

}
