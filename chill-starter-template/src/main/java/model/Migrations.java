package model;

import chill.db.ChillMigrations;

public class Migrations extends ChillMigrations {

    public final ChillMigration migration_2022_03_08_22_26_44 = new ChillMigration("user"){
        protected void up() {
            exec("""
                    CREATE TABLE user (
                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                  created_at TIMESTAMP,
                                  updated_at TIMESTAMP,
                                  email VARCHAR(250) DEFAULT NULL,
                                  password VARCHAR(250) DEFAULT NULL,
                                  uuid VARCHAR(250) DEFAULT NULL
                                );
                    """);
        }
        protected void down() {
            exec("DROP TABLE user");
        }
    };

    public static void main(String[] args) {
        generateNewMigration();
    }
}
