package chill.db.migrations;

import chill.db.ChillMigrations;
import chill.db.ChillRecord;

import java.sql.DriverManager;

public class ScratchMigrationFile extends ChillMigrations {

    {
        include(new SecondaryMigrationFile());
        include(new FileBasedMigrationFile());
    }

    public final ChillMigration migration_2022_03_08_16_02_26 = new ChillMigration("add user table"){
        protected void steps() {
            step("CREATE TABLE user (\n" +
                    "                      id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "                      first_name VARCHAR(250),\n" +
                    "                      last_name VARCHAR(250),\n" +
                    "                      email VARCHAR(250) DEFAULT NULL,\n" +
                    "                      password VARCHAR(250) DEFAULT NULL,\n" +
                    "                      age INTEGER\n" +
                    "                    );","DROP TABLE user");
        }
    };

    public final ChillMigration migration_2022_03_09_16_02_26 = new ChillMigration("add user2 table"){
        protected void steps() {
            step("CREATE TABLE user2 (\n" +
                    "                      id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "                      first_name VARCHAR(250),\n" +
                    "                      last_name VARCHAR(250),\n" +
                    "                      email VARCHAR(250) DEFAULT NULL,\n" +
                    "                      password VARCHAR(250) DEFAULT NULL,\n" +
                    "                      age INTEGER\n" +
                    "                    );","DROP TABLE user2");
        }
    };


    public final ChillMigration migration_2022_03_09_16_02_27 = new ChillMigration("add user3 table"){
        protected void steps() {
            step("CREATE TABLE user3 (\n" +
                    "                      id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "                      first_name VARCHAR(250),\n" +
                    "                      last_name VARCHAR(250),\n" +
                    "                      email VARCHAR(250) DEFAULT NULL,\n" +
                    "                      password VARCHAR(250) DEFAULT NULL,\n" +
                    "                      age INTEGER\n" +
                    "                    );","DROP TABLE user3");
        }
    };


    public static final String TEST_DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;CASE_INSENSITIVE_IDENTIFIERS=true";
    public static void main(String[] args) throws Exception {
        Class.forName("org.h2.Driver");
        ChillRecord.connectionSource = () -> DriverManager.getConnection(TEST_DB_URL);
        ChillMigrations migrations = new ChillMigrations(ScratchMigrationFile.class.getName());
        migrations.console();
    }
}
