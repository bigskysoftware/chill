package chill.db.migrations;

import chill.db.ChillMigrations;

public class FileBasedMigrationFile extends ChillMigrations {

    public final ChillMigration migration_2022_03_05_01_02_27 = new ChillMigration("add user5 table"){
        protected void steps() {
            file("/migrations/user5.sql", "/migrations/drop_user5.sql");
        }
    };

}
