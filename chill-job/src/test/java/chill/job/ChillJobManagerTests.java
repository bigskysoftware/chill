package chill.job;

import chill.db.ChillRecord;
import chill.job.impl.DefaultChillJobWorker;
import chill.job.model.JobStatus;
import chill.utils.TheMissingUtils;
import org.junit.jupiter.api.*;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

import static chill.utils.TheMissingUtils.n;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private static final String DELETE = """
            DELETE FROM CHILL_JOB_MANAGER_TESTS_ENTITY""";

    ChillJobWorker worker;

    @BeforeAll
    public void setup() {
        ChillRecord.connectionSource = () -> DriverManager.getConnection("jdbc:h2:./chill_job_tests");
        worker = new DefaultChillJobWorker();

        try (var conn = ChillRecord.connectionSource.getConnection();
             var stmt = conn.prepareStatement(UP)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    public void afterEach() {
        try (var conn = ChillRecord.connectionSource.getConnection();
             var stmt = conn.prepareStatement(DELETE)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
//        JobEntity.find.deleteAll();
//        QueueEntity.find.deleteAll(); // this should be redundant since we delete all jobs and have a cascade delete
    }

    @AfterAll
    public void afterAll() {
        worker.shutdown();
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

        public SleepJob(long timeout, ChillJobWorker worker) {
            super(worker);
            this.timeout = timeout;
        }

        @Override
        public void run() throws Exception {
            try (var conn = ChillRecord.connectionSource.getConnection();
                 var stmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setObject(1, timeout);

                assert stmt.executeUpdate() == 1;
                var keys = stmt.getGeneratedKeys();
                keys.next();
                System.out.println("pkey: " + keys.getObject(1));
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
        var job = new SleepJob(timeout, worker);
        job.submit();
        return job.getJobId();
    }

    @Test
    public void test() throws InterruptedException {
        var jobId = submitJob(1000);
        Thread.sleep(3000);

        var status = worker.getJobStatus(jobId);
        System.out.println("Test got status: " + status);
    }

    @Test
    public void testManyJobs() {
        final int numJobs = 25;
        ChillJobId ids[] = new ChillJobId[numJobs];
        ChillRecord.inTransaction(() -> {
            for (int i = 0; i < numJobs; i++) {
                ids[i] = submitJob(2500);
            }
        });
        var jobs = Arrays.asList(ids);
        var pendingJobs = new HashSet<>(jobs);

        // wait for all jobs to finish
        JobStatus unterminatedJobs[] = new JobStatus[]{JobStatus.PENDING, JobStatus.ASSIGNED, JobStatus.RUNNING};
        long startTs = System.currentTimeMillis();
        while (!pendingJobs.isEmpty() && System.currentTimeMillis() - startTs < 1000 * 60 * 5) {
            TheMissingUtils.safely(() -> {
                Thread.sleep(5000);

                String pendingJobIds[] = new String[pendingJobs.size()];
                int i = 0;
                for (var id : pendingJobs) {
                    pendingJobIds[i++] = id.toString();
                }

//                var completedBatch = QueueEntity
//                        .where("job_id in ?", pendingJobIds)
//                        .and("not (status in ?)", unterminatedJobs)
//                        .select(QueueEntity.jobId())
//                        .toList();

//                int start = pendingJobs.size();
//                for (var job : completedBatch) {
//                    ChillJobId id = ChillJobId.fromString(job.getJobId().getId());
//                    pendingJobs.remove(id);
//                }

//                System.out.println("removed " + (start - pendingJobs.size()) + " jobs");
            });
        }

        TheMissingUtils.safely(() -> {
            var pendingList = n(jobs.size()).map(i -> "?").join(", ");
            var query = """
                    select job_id, status from chill_job_pending_queue
                    where job_id in (%s)""".formatted(pendingList);
            var errors = new LinkedList<>();

            try (var quietly = ChillRecord.quietly();
                 var conn = ChillRecord.connectionSource.getConnection();
                 var stmt = conn.prepareStatement(query)) {
                var i = 1;
                for (var id : jobs) {
                    stmt.setObject(i++, id.toString());
                }
                var rs = stmt.executeQuery();
                while (rs.next()) {
                    ChillJobId id = ChillJobId.fromString(rs.getString(1));
                    JobStatus status = JobStatus.valueOf(rs.getString(2));

                    if (status != JobStatus.COMPLETED) {
                        errors.add("Job %s is in status %s".formatted(id, status));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            for (var error : errors) {
                System.out.println(error);
            }
            assertTrue(errors.isEmpty(), "There were errors");
        });

        System.out.println("All jobs done");
    }
}
