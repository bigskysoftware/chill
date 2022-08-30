package chill.db.migrations;

import chill.db.ChillMigrations;

public class MultiStepMigrationFile1 extends ChillMigrations {

    public final ChillMigration migration_2022_08_30_14_37_00 = new ChillMigration("add user table") {
        @Override
        protected void steps() {
            step("""
                      CREATE TABLE user (
                        id INT AUTO_INCREMENT PRIMARY KEY
                      );
                    """, "DROP TABLE user;");
            step("""
                    ALTER TABLE user add column first_name VARCHAR(250);
                    """, """
                    ALTER TABLE user DROP COLUMN first_name;
                    """);

        }
    };

    public final ChillMigration migration_2022_08_30_14_49_00 = new ChillMigration("add more columns with failure") {
        @Override
        protected void steps() {
            step("""
                    ALTER TABLE user add column last_name VARCHAR(250);
                    """, """
                    ALTER TABLE user drop column last_name;
                     """);
            step("""
                    ALTER TABLE user add column email VARCHAR(abc);
                    """, """
                    ALTER TABLE user drop column email;
                     """);
        }
    };
}
