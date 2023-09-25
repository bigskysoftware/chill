# chill.job

## architecture

### get-job
```redis
-- move job from beginning of the pending queue to the end of the processing queue 
lmove $pending_queue $processing_queue LEFT RIGHT
```

### finish-job on failure
```redis

```