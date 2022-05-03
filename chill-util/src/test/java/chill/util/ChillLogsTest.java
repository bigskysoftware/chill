package chill.util;

import chill.utils.NiceList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static chill.utils.ChillLogs.*;
import static org.junit.jupiter.api.Assertions.*;

public class ChillLogsTest {

    private TestLogAdapter testAdapter;

    @BeforeEach
    void setTestAdapter() {
        this.testAdapter = new TestLogAdapter();
        setAdapter(testAdapter);
    }

    @AfterEach
    void setStdOutAdapter() {
        setAdapter(new StdOutAdapter());
    }

    @Test
    void bootstrap(){
        info("test");
        assertLastLogEndsWith("test");

        info(()-> "test2");
        assertLastLogEndsWith("test2");

        info("test %s", "test");
        assertLastLogEndsWith("test test");

        info("test {}", "test2");
        assertLastLogEndsWith("test test2");
    }

    @Test
    void ctx(){
        try (var ignore = establishCtx()) {
            addContext("email", "foo@bar.com");
            info("test");
            assertLastLogEndsWith("{email=foo@bar.com} test");
        }
        info("test");
        assertLastLogEndsWith("test");
    }

    @Test
    void throwable(){
        error(new Throwable("foo"));
        assertLastLogEndsWith("An exception occurred: foo");
    }

    private void assertLastLogEndsWith(String str) {
        String last = testAdapter.getLastLog();
        assertNotNull(last);
        assertTrue(last.endsWith(" - " + str), "Expected " + last + " to end with " + str);
    }

    static class TestLogAdapter extends StdOutAdapter {
        private final NiceList<String> logMessages = new NiceList<>();
        @Override
        public void print(String finalLogMessage) {
            logMessages.add(finalLogMessage);
            super.print(finalLogMessage);
        }

        public NiceList<String> getLogs() {
            return logMessages;
        }

        public String getLastLog() {
            return logMessages.last();
        }
    }
}
