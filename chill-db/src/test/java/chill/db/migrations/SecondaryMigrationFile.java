package chill.db.migrations;

import chill.db.ChillMigrations;

public class SecondaryMigrationFile extends ChillMigrations {

    public final ChillMigration migration_2022_03_09_01_02_27 = new ChillMigration("add user4 table"){
        protected void steps() {
            step("""
                    CREATE TABLE user4 (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      first_name VARCHAR(250),
                      last_name VARCHAR(250),
                      email VARCHAR(250) DEFAULT NULL,
                      password VARCHAR(250) DEFAULT NULL,
                      age INTEGER
                    );
                    """, "DROP TABLE user4;");
        }
    };

}
