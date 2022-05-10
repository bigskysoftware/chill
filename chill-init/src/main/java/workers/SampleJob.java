package workers;

import chill.utils.TheMissingUtils;

public class SampleJob extends chill.workers.Job<SampleJob> {
    @Override
    public void doWork() {
        for (int i = 0; i < 5; i++) {
            info("Doing some (fake) work...");
            TheMissingUtils.sleep(1000);
        }
    }
}
