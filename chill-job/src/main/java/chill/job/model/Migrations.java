package chill.job.model;

import chill.db.ChillMigrations;

public class Migrations extends ChillMigrations {
    public static void main(String[] args) {
        generateNewMigration();
    }

    public final ChillMigration migration_2023_10_01_20_29_02 = new ChillMigration("ChillJob") {
        protected void up() {
            exec("""
                    create table chill_job_jobs (
                        id varchar(128) not null primary key,
                        status varchar(32) not null,
                        timestamp TIMESTAMP not null,
                        backoff integer default null,
                        worker_id varchar(128) default null,
                        error text default null,
                        job_class text not null,
                        job_data text not null
                    );
                                        
                    create index chill_job_jobs_status_idx ON chill_job_jobs (status);
                    """);
        }

        protected void down() {
            exec("""
                    drop index chill_job_jobs_status_idx;
                    drop table chill_job_jobs;
                    """);
        }
    };
}
