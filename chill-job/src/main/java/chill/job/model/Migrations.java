package chill.job.model;

import chill.db.ChillMigrations;

public class Migrations extends ChillMigrations {
    public static void main(String[] args) {
        generateNewMigration();
    }

    public final ChillMigration migration_2023_09_23_13_21_01 = new ChillMigration("jobs") {
        protected void up() {
            exec("""
                    CREATE TABLE chill_job_jobs (
                        id VARCHAR(256) PRIMARY KEY, -- chilljob:uuid:tag
                        status VARCHAR(16) NOT NULL,
                        created_at TIMESTAMP NOT NULL,
                        updated_at TIMESTAMP NOT NULL,
                        started_at TIMESTAMP,
                        completed_at TIMESTAMP,
                        job_json TEXT NOT NULL,
                        job_class TEXT NOT NULL
                    )""");
        }
        protected void down() {
            exec("DROP TABLE chill_job_jobs");
        }
    };

    public final ChillMigration migration_2023_09_23_16_18_27 = new ChillMigration("chill_job_queue"){
        protected void up() {
            exec("""
                    CREATE TABLE chill_job_pending_queue (
                        id LONG PRIMARY KEY AUTO_INCREMENT,
                        job_id VARCHAR(256) NOT NULL,
                        
                        CONSTRAINT fk_job_id FOREIGN KEY (job_id) REFERENCES chill_job_jobs(id)
                        ON DELETE CASCADE
                    )""");
        }
        protected void down() {
            exec("""
                    DROP TABLE chill_job_pending_queue""");
        }
    };
}
