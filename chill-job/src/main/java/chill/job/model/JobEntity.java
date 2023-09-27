package chill.job.model;

import chill.db.ChillField;
import chill.db.ChillRecord;
import chill.job.ChillJob;

import java.util.concurrent.atomic.AtomicReference;

public class JobEntity extends _generated.AbstractJobEntity {
    public enum Status {
        PENDING,
        ASSIGNED,
        RUNNING,
        COMPLETED,
        ERRORED,
        CANCELLED
    }

    {tableName("chill_job_jobs");}

    // chilljob:uuid:tag
    ChillField<String> id = field("id", String.class).primaryKey();
    ChillField<Status> status = field("status", Status.class).required();
    ChillField<String> jobJson = field("job_json", String.class).required();
    ChillField<String> jobClass = field("job_class", String.class).required();

    public static ChillJob dequeue() {
        AtomicReference<ChillJob> job = new AtomicReference<>();
        ChillRecord.inTransaction(() -> {
            JobEntity jobEntity = JobEntity
                    .select(JobEntity.allFields(), QueueEntity.id())
                    .join(QueueEntity.to.jobId)
                    .forUpdate()
                    .skipLocked()
                    .first();

            if (jobEntity == null) {
                return;
            }

            Long queueId = jobEntity.getAdditionalField(QueueEntity.field.id);
            var count = QueueEntity
                    .where("id = ?", queueId)
                    .delete();
            if (count == 0) { // for update, skip locked should prevent this but isn't infallible
                return;
            }

            job.set(ChillJob.fromRecord(jobEntity));
        });
        return job.get();
    }
}
