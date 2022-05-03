package chill.workers;

import chill.utils.ChillLogs;
import chill.utils.TheMissingUtils;
import org.jobrunr.jobs.context.JobRunrDashboardLogger;
import org.jobrunr.jobs.lambdas.JobRequest;
import org.jobrunr.jobs.lambdas.JobRequestHandler;
import org.jobrunr.scheduling.BackgroundJobRequest;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import java.time.Instant;

import static chill.utils.TheMissingUtils.lazyStr;

public abstract class Job<T extends Job> implements JobRequest, JobRequestHandler<T> {

    private int timeDelay;
    private static Logger jobRunnrLogger = new JobRunrDashboardLogger(new NOPLogger(){});
    private ChillLogs.LogCategory chillLogger = ChillLogs.get(this.getClass());

    @Override
    public Class<? extends JobRequestHandler> getJobRequestHandler() {
        return this.getClass();
    }

    @Override
    public final void run(T jobRequest) throws Exception {
        jobRequest.doWork();
    }

    public abstract void doWork() throws Exception;

    public T run(){
        if (timeDelay == 0) {
            BackgroundJobRequest.enqueue(this);
        } else {
            BackgroundJobRequest.schedule(Instant.now().plusSeconds(timeDelay), this);
        }
        return (T) this;
    }

    public T runIn(int time) {
        timeDelay = time;
        return (T) this;
    }

    public T seconds(){
        return run();
    }

    public T minutes(){
        timeDelay = timeDelay * 60;
        return run();
    }


    //=====================================
    // Log delegation
    //=====================================
    protected void trace(TheMissingUtils.ToString msg) {
        if (jobRunnrLogger.isTraceEnabled()) {
            jobRunnrLogger.trace(String.valueOf(lazyStr(msg)));
        }
        chillLogger.trace(msg);
    }

    protected void trace(Object msg) {
        if (jobRunnrLogger.isTraceEnabled()) {
            jobRunnrLogger.trace(String.valueOf(msg));
        }
        chillLogger.trace(msg);
    }
    protected void trace(String format, Object... argArray) {
        jobRunnrLogger.trace(format, argArray);
        chillLogger.trace(format, argArray);
    }

    protected void debug(TheMissingUtils.ToString msg) {
        if (jobRunnrLogger.isDebugEnabled()) {
            jobRunnrLogger.debug(String.valueOf(lazyStr(msg)));
        }
        chillLogger.debug(msg);
    }

    protected void debug(Object msg) {
        if (jobRunnrLogger.isDebugEnabled()) {
            jobRunnrLogger.debug(String.valueOf(msg));
        }
        chillLogger.debug(msg);
    }

    protected void debug(String format, Object... argArray) {
        jobRunnrLogger.debug(format, argArray);
    }

    protected void info(TheMissingUtils.ToString msg) {
        if (jobRunnrLogger.isInfoEnabled()) {
            jobRunnrLogger.info(String.valueOf(lazyStr(msg)));
        }
        chillLogger.info(msg);
    }
    protected void info(Object msg) {
        if (jobRunnrLogger.isInfoEnabled()) {
            jobRunnrLogger.info(String.valueOf(msg));
        }
        chillLogger.info(msg);
    }
    protected void info(String format, Object... argArray) {
        jobRunnrLogger.info(format, argArray);
    }

    protected void warn(TheMissingUtils.ToString msg) {
        if (jobRunnrLogger.isWarnEnabled()) {
            jobRunnrLogger.warn(String.valueOf(lazyStr(msg)));
        }
        chillLogger.warn(msg);
    }
    protected void warn(Object msg) {
        if (jobRunnrLogger.isWarnEnabled()) {
            jobRunnrLogger.warn(String.valueOf(msg));
        }
        chillLogger.warn(msg);
    }
    protected void warn(String format, Object... argArray) {
        jobRunnrLogger.warn(format, argArray);
    }

    protected void error(Throwable throwable) {
        jobRunnrLogger.error(throwable.getMessage(), throwable);
        chillLogger.error(throwable);
    }
    protected void error(TheMissingUtils.ToString msg) {
        if (jobRunnrLogger.isErrorEnabled()) {
            jobRunnrLogger.error(String.valueOf(lazyStr(msg)));
        }
        chillLogger.error(msg);
    }
    protected void error(Object msg) {
        if (jobRunnrLogger.isErrorEnabled()) {
            jobRunnrLogger.error(String.valueOf(msg));
        }
        chillLogger.error(msg);
    }
    protected void error(String format, Object... argArray) {
        jobRunnrLogger.error(format, argArray);
    }

}
