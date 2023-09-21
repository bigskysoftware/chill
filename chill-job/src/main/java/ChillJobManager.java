public interface ChillJobManager {
    ChillJob getJob(ChillJob.Id id);

    ChillJob.Id submit(ChillJob job);
    void fetch(ChillJob job);
    void cancel(ChillJob job);
    void update(ChillJob job);

    void ensureSubmitted(ChillJob job) throws AssertionError;
    void ensureNotSubmitted(ChillJob job) throws AssertionError;
}
