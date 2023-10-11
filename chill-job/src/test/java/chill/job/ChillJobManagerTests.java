package chill.job;

import chill.db.ChillMigrations;
import chill.db.ChillRecord;
import chill.job.impl.DefaultChillJobWorker;
import chill.job.model.ChillJobEntity;
import chill.job.model.JobStatus;
import chill.job.model.Migrations;
import chill.utils.TheMissingUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.DriverManager;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ChillJobManagerTests {
    private static CountDownLatch latch;

    @BeforeAll
    public void beforeAll() {
        ChillRecord.connectionSource = () -> DriverManager.getConnection("jdbc:h2:./chill-job-tests");
        ChillJobWorker.setDefaultInstance(new DefaultChillJobWorker());

        new ChillMigrations(Migrations.class).up();

        ChillJobEntity
                .where()
                .deleteAll();
    }

    static class SimpleJob extends ChillJob {
        @Override
        public void run() throws Exception {
            System.out.println("I'm running!");
            TheMissingUtils.sleep(1000);
            System.out.println("I'm done!");
            latch.countDown();
        }
    }

    @Test
    public void testSingleJob() throws InterruptedException {
        latch = new CountDownLatch(1);
        SimpleJob job = new SimpleJob();
        System.out.println("using job: " + job);
        job.submit();

        while (!latch.await(1, TimeUnit.SECONDS)) {
            System.out.println("Waiting for job to finish...");
        }

        TheMissingUtils.sleep(2500);

        assertEquals(job.getStatus(), JobStatus.COMPLETED);
    }
}