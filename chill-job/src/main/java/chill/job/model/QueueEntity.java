package chill.job.model;

import chill.db.ChillField;
import chill.job.ChillJob;

import java.sql.Timestamp;

public class QueueEntity extends _generated.AbstractQueueEntity {
    {tableName("chill_job_pending_queue");}

    ChillField<Long> id = pk("id");
    ChillField.FK<QueueEntity, JobEntity> jobId = fk("job_id", JobEntity.class);
    ChillField<JobStatus> status = field("status", JobStatus.class).required();
    ChillField<String> workerId = field("worker_id", String.class);
}
