package chill.job.model;

import chill.db.ChillField;

import java.util.UUID;

public class ChillJobEntity extends _generated.AbstractChillJobEntity {
    ChillField<String> id = pk("id", String.class);
    ChillField<String> tag = field("tag", String.class);
    ChillField<JobStatus> status = field("status", JobStatus.class).required();
    ChillField<Integer> backoff = field("backoff", Integer.class).withDefault(null);
    ChillField<String> error = field("error", String.class).withDefault(null);
    ChillField<String> workerId = field("worker_id", String.class).withDefault(null);
    ChillField<String> jobClass = field("job_class", String.class).required();
    ChillField<String> jobData = field("job_data", String.class).required();
}
