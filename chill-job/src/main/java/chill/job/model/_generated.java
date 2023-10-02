package chill.job.model;

import chill.db.ChillCodeGenerator;
import chill.db.ChillRecord;

public class _generated extends ChillCodeGenerator {
    public static void main(String[] args) {
        generateCodeForMyPackage();
    }


//=================== GENERATED CODE ========================



    public static abstract class AbstractChillJobEntity extends ChillRecord {

        protected final ChillJobEntity self = (ChillJobEntity) (Object) this;


        public ChillJobEntity createOrThrow(){
            if(!create()){
                throw new chill.db.ChillValidation.ValidationException(getErrors());
            }
            return self;
        }

        public ChillJobEntity saveOrThrow(){
            if(!save()){
                throw new chill.db.ChillValidation.ValidationException(getErrors());
            }
            return self;
        }

        public ChillJobEntity firstOrCreateOrThrow(){
            return (ChillJobEntity) firstOrCreateImpl();
        }

        public ChillJobEntity fromWebParams(java.util.Map<String, String> values, String... params) {
            ChillRecord.populateFromWebParams(self, values, params);
            return self;
        }

        public java.lang.String getId() {
            return self.id.get();
        }

        public void setId(java.lang.String id) {
            self.id.set(id);
        }

        public ChillJobEntity withId(java.lang.String id) {
            setId(id);
            return self;
        }


        public chill.job.model.JobStatus getStatus() {
            return self.status.get();
        }

        public void setStatus(chill.job.model.JobStatus status) {
            self.status.set(status);
        }

        public ChillJobEntity withStatus(chill.job.model.JobStatus status) {
            setStatus(status);
            return self;
        }

        public java.lang.Integer getBackoff() {
            return self.backoff.get();
        }

        public void setBackoff(java.lang.Integer backoff) {
            self.backoff.set(backoff);
        }

        public ChillJobEntity withBackoff(java.lang.Integer backoff) {
            setBackoff(backoff);
            return self;
        }

        public java.lang.String getError() {
            return self.error.get();
        }

        public void setError(java.lang.String error) {
            self.error.set(error);
        }

        public ChillJobEntity withError(java.lang.String error) {
            setError(error);
            return self;
        }

        public java.lang.String getWorkerId() {
            return self.workerId.get();
        }

        public void setWorkerId(java.lang.String workerId) {
            self.workerId.set(workerId);
        }

        public ChillJobEntity withWorkerId(java.lang.String workerId) {
            setWorkerId(workerId);
            return self;
        }

        public java.lang.String getJobClass() {
            return self.jobClass.get();
        }

        public void setJobClass(java.lang.String jobClass) {
            self.jobClass.set(jobClass);
        }

        public ChillJobEntity withJobClass(java.lang.String jobClass) {
            setJobClass(jobClass);
            return self;
        }

        public java.lang.String getJobData() {
            return self.jobData.get();
        }

        public void setJobData(java.lang.String jobData) {
            self.jobData.set(jobData);
        }

        public ChillJobEntity withJobData(java.lang.String jobData) {
            setJobData(jobData);
            return self;
        }

        public static final chill.db.ChillRecord.Finder<ChillJobEntity> find = finder(ChillJobEntity.class);

        private static final ChillJobEntity instance = new ChillJobEntity();

        public static chill.db.ChillQuery<ChillJobEntity> where(Object... args) {
            return find.where(args);
        }

        public static chill.db.ChillQuery<ChillJobEntity> join(chill.db.ChillField.FK fk) {
            return find.join(fk);
        }

        public static chill.db.ChillQuery<ChillJobEntity> select(chill.db.ChillField... fields) {
            return find.select(fields);
        }

        public static class to {
        }

        public static class field {
            public static final chill.db.ChillField<ChillJobEntity> ALL = new chill.db.ChillField<>(instance, "*", ChillJobEntity.class);
            public static final chill.db.ChillField<java.lang.String> id = instance.id;
            public static final chill.db.ChillField<chill.job.model.JobStatus> status = instance.status;
            public static final chill.db.ChillField<java.lang.Integer> backoff = instance.backoff;
            public static final chill.db.ChillField<java.lang.String> error = instance.error;
            public static final chill.db.ChillField<java.lang.String> workerId = instance.workerId;
            public static final chill.db.ChillField<java.lang.String> jobClass = instance.jobClass;
            public static final chill.db.ChillField<java.lang.String> jobData = instance.jobData;
        }
        public static chill.db.ChillField<ChillJobEntity> allFields() {
            return field.ALL;
        }
        public static chill.db.ChillField<java.lang.String> id() {
            return instance.id;
        }
        public static chill.db.ChillField<chill.job.model.JobStatus> status() {
            return instance.status;
        }
        public static chill.db.ChillField<java.lang.Integer> backoff() {
            return instance.backoff;
        }
        public static chill.db.ChillField<java.lang.String> error() {
            return instance.error;
        }
        public static chill.db.ChillField<java.lang.String> workerId() {
            return instance.workerId;
        }
        public static chill.db.ChillField<java.lang.String> jobClass() {
            return instance.jobClass;
        }
        public static chill.db.ChillField<java.lang.String> jobData() {
            return instance.jobData;
        }
    }
}
