package chill.job;

import chill.db.ChillRecord;
import chill.job.impl.DefaultChillJobWorker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.DriverManager;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ChillJobManagerTests {
    ChillJobWorker worker;

    @BeforeAll
    public void setup() {
        ChillRecord.connectionSource = () -> DriverManager.getConnection("jdbc:h2:./chill_job");
        worker = new DefaultChillJobWorker();
    }

    @Test
    public void test() throws InterruptedException {
        Thread.sleep(15000);
    }
}
