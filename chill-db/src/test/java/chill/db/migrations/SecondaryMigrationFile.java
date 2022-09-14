package chill.db.migrations;

import chill.db.ChillMigrations;

public class SecondaryMigrationFile extends ChillMigrations {

    public final ChillMigration migration_2022_03_09_01_02_27 = new ChillMigration("add user4 table"){
        protected void up() {
            exec("CREATE TABLE user4 (\n" +
                    "                      id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "                      first_name VARCHAR(250),\n" +
                    "                      last_name VARCHAR(250),\n" +
                    "                      email VARCHAR(250) DEFAULT NULL,\n" +
                    "                      password VARCHAR(250) DEFAULT NULL,\n" +
                    "                      age INTEGER\n" +
                    "                    );");
        }
        protected void down() {
            exec("DROP TABLE user4");
        }
    };

}
