package chill.job;

import chill.db.ChillMigrations;
import chill.db.ChillRecord;
import chill.job.impl.DefaultChillJobWorker;
import chill.job.model.ChillJobEntity;
import chill.job.model.JobStatus;
import chill.job.model.Migrations;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.DriverManager;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ChillJobManagerTests {
    private ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    @BeforeAll
    public void beforeAll() {
        ChillRecord.connectionSource = () -> DriverManager.getConnection("jdbc:h2:./chill-job-tests");
        ChillJobWorker.setDefaultInstance(new DefaultChillJobWorker());

        new ChillMigrations(Migrations.class).up();

        ChillJobEntity
                .where()
                .deleteAll();
    }

    class SimpleJob extends ChillJob {

        @Override
        public void run() throws Exception {
            var lock = locks.get(getJobId().toString());
            assertNotNull(lock);

            while (!lock.tryLock(250, TimeUnit.MILLISECONDS)) {
                System.out.printf("[%s] Waiting for lock%n", getJobId());
            }

            try {
                System.out.printf("[%s] Running job%n", getJobId());
                Thread.sleep(1000);
            } finally {
                lock.unlock();
            }
        }
    }

    @Test
    public void testSingleJob() throws InterruptedException {
        SimpleJob job = new SimpleJob();
        var lock = new ReentrantLock();
        lock.lock();
        locks.put(job.getJobId().toString(), lock);

        job.submit();

        try (var shh = ChillRecord.quietly()) {
            while (job.getStatus() != JobStatus.RUNNING) {
                System.out.printf("[%s] Waiting for job to start%n", job.getJobId());
                Thread.sleep(250);
            }
        }
        lock.unlock();

        try (var shh = ChillRecord.quietly()) {
            while (job.getStatus() == JobStatus.RUNNING) {
                System.out.printf("[%s] Waiting for job to complete%n", job.getJobId());
                Thread.sleep(250);
            }
        }

        assertEquals(job.getStatus(), JobStatus.COMPLETED);
    }
}