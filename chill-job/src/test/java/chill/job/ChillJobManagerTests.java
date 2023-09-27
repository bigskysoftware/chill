package chill.job;

import chill.db.ChillRecord;
import chill.job.impl.DefaultChillJobWorker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.DriverManager;
import java.sql.SQLException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ChillJobManagerTests {
    private static final String UP = """
            CREATE TABLE IF NOT EXISTS CHILL_JOB_MANAGER_TESTS_ENTITY (
                id INTEGER PRIMARY KEY AUTO_INCREMENT,
                time_ms INTEGER NOT NULL,
                done BOOLEAN DEFAULT false
            )""";

    private static final String DOWN = """
            DROP TABLE IF EXISTS CHILL_JOB_MANAGER_TESTS_ENTITY""";

    private static final String INSERT = """
            INSERT INTO CHILL_JOB_MANAGER_TESTS_ENTITY (time_ms) VALUES (?)
            """;

    private static final String UPDATE = """
            UPDATE CHILL_JOB_MANAGER_TESTS_ENTITY SET done = ? WHERE id = ?""";

    ChillJobWorker worker;

    @BeforeAll
    public void setup() {
        ChillRecord.connectionSource = () -> DriverManager.getConnection("jdbc:h2:./chill_job");
        worker = new DefaultChillJobWorker();

        try (var conn = ChillRecord.connectionSource.getConnection();
             var stmt = conn.prepareStatement(UP)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    public void afterAll() {
        try (var conn = ChillRecord.connectionSource.getConnection();
             var stmt = conn.prepareStatement(DOWN)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static class SleepJob extends ChillJob {
        long timeout;
        Long pkey;

        public SleepJob(long timeout) {
            super("chill-job-with-" + timeout);
            this.timeout = timeout;
        }

        @Override
        public void run() throws Exception {
            try (var conn = ChillRecord.connectionSource.getConnection();
                 var stmt = conn.prepareStatement(INSERT)) {
                stmt.setObject(1, timeout);

                assert stmt.executeUpdate() == 1;
                var keys = stmt.getGeneratedKeys();
                keys.next();
                pkey = keys.getLong(1);
            }

            Thread.sleep(timeout);

            try (var conn = ChillRecord.connectionSource.getConnection();
                 var stmt = conn.prepareStatement(UPDATE)) {
                stmt.setObject(1, true);
                stmt.setObject(2, pkey);
            }
        }
    }

    public ChillJobId submitJob(long timeout) {
        var job = new SleepJob(timeout);
        job.submit();
        return job.getJobId();
    }

    @Test
    public void test() throws InterruptedException {
        var jobId = submitJob(1000);
        Thread.sleep(2000);

        var job = worker.getJobStatus(jobId);
    }
}
