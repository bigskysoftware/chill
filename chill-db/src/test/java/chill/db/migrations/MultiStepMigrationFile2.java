package chill.db.migrations;

import chill.db.ChillMigrations;

public class MultiStepMigrationFile2 extends ChillMigrations {

    public final ChillMigration migration_2022_08_30_14_37_00 = new ChillMigration("add user table") {
        @Override
        protected void steps() {
            step("CREATE TABLE user (\n"+
                        "id INT AUTO_INCREMENT PRIMARY KEY,\n"+
                        "first_name VARCHAR(250)\n"+
                      ");", "DROP TABLE user;");
            step("ALTER TABLE user add column last_name VARCHAR(250);","ALTER TABLE user DROP COLUMN lastname;");

        }
    };
}
