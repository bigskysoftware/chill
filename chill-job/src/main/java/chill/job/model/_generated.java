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
    
    private static final JobEntity instance = new JobEntity();
    
    public static chill.db.ChillQuery<JobEntity> where(Object... args) {
      return find.where(args);
    }
    
    public static chill.db.ChillQuery<JobEntity> join(chill.db.ChillField.FK fk) {
      return find.join(fk);
    }
    
    public static chill.db.ChillQuery<JobEntity> select(chill.db.ChillField... fields) {
      return find.select(fields);
    }
    
    public static class to {
    }
    
    public static class field {
      public static final chill.db.ChillField<JobEntity> ALL = new chill.db.ChillField<>(instance, "*", JobEntity.class);
      public static final chill.db.ChillField<java.lang.String> id = instance.id;
      public static final chill.db.ChillField<java.lang.String> jobJson = instance.jobJson;
      public static final chill.db.ChillField<java.lang.String> jobClass = instance.jobClass;
    }
    public static chill.db.ChillField<JobEntity> allFields() {
      return field.ALL;
    }
    public static chill.db.ChillField<java.lang.String> id() {
      return instance.id;
    }
    public static chill.db.ChillField<java.lang.String> jobJson() {
      return instance.jobJson;
    }
    public static chill.db.ChillField<java.lang.String> jobClass() {
      return instance.jobClass;
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
    
    public static chill.db.ChillQuery<QueueEntity> forJobEntity(JobEntity jobId) {
      return new QueueEntity().jobId.reverse(jobId);
    }
    
    public chill.job.model.JobEntity.Status getStatus() {
      return self.status.get();
    }
    
    public void setStatus(chill.job.model.JobEntity.Status status) {
      self.status.set(status);
    }
    
    public QueueEntity withStatus(chill.job.model.JobEntity.Status status) {
      setStatus(status);
      return self;
    }
    
    public java.lang.String getWorkerId() {
      return self.workerId.get();
    }
    
    public void setWorkerId(java.lang.String workerId) {
      self.workerId.set(workerId);
    }
    
    public QueueEntity withWorkerId(java.lang.String workerId) {
      setWorkerId(workerId);
      return self;
    }
    
    public static final chill.db.ChillRecord.Finder<QueueEntity> find = finder(QueueEntity.class);
    
    private static final QueueEntity instance = new QueueEntity();
    
    public static chill.db.ChillQuery<QueueEntity> where(Object... args) {
      return find.where(args);
    }
    
    public static chill.db.ChillQuery<QueueEntity> join(chill.db.ChillField.FK fk) {
      return find.join(fk);
    }
    
    public static chill.db.ChillQuery<QueueEntity> select(chill.db.ChillField... fields) {
      return find.select(fields);
    }
    
    public static class to {
      public static final chill.db.ChillField.FK jobId = instance.jobId;
    }
    
    public static class field {
      public static final chill.db.ChillField<QueueEntity> ALL = new chill.db.ChillField<>(instance, "*", QueueEntity.class);
      public static final chill.db.ChillField<java.lang.Long> id = instance.id;
      public static final chill.db.ChillField<chill.job.model.JobEntity> jobId = instance.jobId;
      public static final chill.db.ChillField<chill.job.model.JobEntity.Status> status = instance.status;
      public static final chill.db.ChillField<java.lang.String> workerId = instance.workerId;
    }
    public static chill.db.ChillField<QueueEntity> allFields() {
      return field.ALL;
    }
    public static chill.db.ChillField<java.lang.Long> id() {
      return instance.id;
    }
    public static chill.db.ChillField<chill.job.model.JobEntity> jobId() {
      return instance.jobId;
    }
    public static chill.db.ChillField<chill.job.model.JobEntity.Status> status() {
      return instance.status;
    }
    public static chill.db.ChillField<java.lang.String> workerId() {
      return instance.workerId;
    }
    }
    
}
