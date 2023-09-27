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
            var results = JobEntity
                    .select(JobEntity.column.ALL, QueueEntity.column.Id)
                    .join(QueueEntity.to.jobId)
                    .first();

            if (results == null) {
                return;
            }

            Long queueId = results.one(QueueEntity.column.Id);
            var count = QueueEntity
                    .where("id = ?", queueId)
                    .delete();
            if (count == 0) {
                throw new RuntimeException("Failed to delete queue entry, must've been stolen");
            }

            job.set(ChillJob.fromRecord(results.one(column.ALL)));
        });
        return job.get();
    }
}
