package chill.job.model;

import chill.db.ChillCodeGenerator;
import chill.db.ChillField;
import chill.db.ChillRecord;
import chill.db.ChillQuery;

public class _generated extends ChillCodeGenerator {
    public static void main(String[] args) {
        generateCodeForMyPackage();
    }


//=================== GENERATED CODE ========================


    public static abstract class AbstractJobEntity extends ChillRecord {

        protected final JobEntity self = (JobEntity) (Object) this;


        public JobEntity createOrThrow(){
            if(!create()){
                throw new chill.db.ChillValidation.ValidationException(getErrors());
            }
            return self;
        }

        public JobEntity saveOrThrow(){
            if(!save()){
                throw new chill.db.ChillValidation.ValidationException(getErrors());
            }
            return self;
        }

        public JobEntity firstOrCreateOrThrow(){
            return (JobEntity) firstOrCreateImpl();
        }

        public JobEntity fromWebParams(java.util.Map<String, String> values, String... params) {
            ChillRecord.populateFromWebParams(self, values, params);
            return self;
        }

        public java.lang.String getId() {
            return self.id.get();
        }

        public void setId(java.lang.String id) {
            self.id.set(id);
        }

        public JobEntity withId(java.lang.String id) {
            setId(id);
            return self;
        }

        public chill.job.model.JobEntity.Status getStatus() {
            return self.status.get();
        }

        public void setStatus(chill.job.model.JobEntity.Status status) {
            self.status.set(status);
        }

        public JobEntity withStatus(chill.job.model.JobEntity.Status status) {
            setStatus(status);
            return self;
        }

        public java.lang.String getJobJson() {
            return self.jobJson.get();
        }

        public void setJobJson(java.lang.String jobJson) {
            self.jobJson.set(jobJson);
        }

        public JobEntity withJobJson(java.lang.String jobJson) {
            setJobJson(jobJson);
            return self;
        }

        public java.lang.String getJobClass() {
            return self.jobClass.get();
        }

        public void setJobClass(java.lang.String jobClass) {
            self.jobClass.set(jobClass);
        }

        public JobEntity withJobClass(java.lang.String jobClass) {
            setJobClass(jobClass);
            return self;
        }

        public static final chill.db.ChillRecord.Finder<JobEntity> find = finder(JobEntity.class);

        public static ChillQuery<JobEntity> where(Object... args) {
            return find.where(args);
        }

        public static ChillQuery<JobEntity> join(chill.db.ChillField.FK fk) {
            return find.join(fk);
        }

        public static ChillQuery<JobEntity> select(chill.db.ChillField... fields) {
            return find.select(fields);
        }

        public static class to {
            private static final JobEntity instance = new JobEntity();
        }

        public static class column {
            private static final JobEntity instance = to.instance;
            public static final chill.db.ChillField<JobEntity> ALL = new ChillField<>(instance, "*", JobEntity.class) {};
            public static final chill.db.ChillField<java.lang.String> Id = instance.id;
            public static final chill.db.ChillField<chill.job.model.JobEntity.Status> Status = instance.status;
            public static final chill.db.ChillField<java.lang.String> JobJson = instance.jobJson;
            public static final chill.db.ChillField<java.lang.String> JobClass = instance.jobClass;
        }

    }

    public static abstract class AbstractQueueEntity extends ChillRecord {

        protected final QueueEntity self = (QueueEntity) (Object) this;


        public QueueEntity createOrThrow(){
            if(!create()){
                throw new chill.db.ChillValidation.ValidationException(getErrors());
            }
            return self;
        }

        public QueueEntity saveOrThrow(){
            if(!save()){
                throw new chill.db.ChillValidation.ValidationException(getErrors());
            }
            return self;
        }

        public QueueEntity firstOrCreateOrThrow(){
            return (QueueEntity) firstOrCreateImpl();
        }

        public QueueEntity fromWebParams(java.util.Map<String, String> values, String... params) {
            ChillRecord.populateFromWebParams(self, values, params);
            return self;
        }

        public java.lang.Long getId() {
            return self.id.get();
        }

        public chill.job.model.JobEntity getJobId() {
            return self.jobId.get();
        }

        public void setJobId(chill.job.model.JobEntity jobId) {
            self.jobId.set(jobId);
        }

        public QueueEntity withJobId(chill.job.model.JobEntity jobId) {
            setJobId(jobId);
            return self;
        }

        public static ChillQuery<QueueEntity> forJobEntity(JobEntity jobId) {
            return new QueueEntity().jobId.reverse(jobId);
        }

        public java.sql.Timestamp getTimestamp() {
            return self.timestamp.get();
        }

        public void setTimestamp(java.sql.Timestamp timestamp) {
            self.timestamp.set(timestamp);
        }

        public QueueEntity withTimestamp(java.sql.Timestamp timestamp) {
            setTimestamp(timestamp);
            return self;
        }

        public static final chill.db.ChillRecord.Finder<QueueEntity> find = finder(QueueEntity.class);

        public static ChillQuery<QueueEntity> where(Object... args) {
            return find.where(args);
        }

        public static ChillQuery<QueueEntity> join(chill.db.ChillField.FK fk) {
            return find.join(fk);
        }

        public static ChillQuery<QueueEntity> select(chill.db.ChillField... fields) {
            return find.select(fields);
        }

        public static class to {
            private static final QueueEntity instance = new QueueEntity();
            public static final chill.db.ChillField.FK jobId = instance.jobId;
        }

        public static class column {
            private static final QueueEntity instance = to.instance;
            public static final chill.db.ChillField<QueueEntity> ALL = new ChillField<>(instance, "*", QueueEntity.class) {};
            public static final chill.db.ChillField<java.lang.Long> Id = instance.id;
            public static final chill.db.ChillField<chill.job.model.JobEntity> JobId = instance.jobId;
            public static final chill.db.ChillField<java.sql.Timestamp> Timestamp = instance.timestamp;
        }

    }
}
