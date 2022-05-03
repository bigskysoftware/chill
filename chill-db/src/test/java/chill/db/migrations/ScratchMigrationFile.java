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

    public final ChillMigration migration_2022_03_09_16_02_26 = new ChillMigration("add user2 table"){
        protected void up() {
            exec("""
                    CREATE TABLE user2 (
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
            exec("DROP TABLE user2");
        }
    };


    public final ChillMigration migration_2022_03_09_16_02_27 = new ChillMigration("add user3 table"){
        protected void up() {
            exec("""
                    CREATE TABLE user3 (
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
            exec("DROP TABLE user3");
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
