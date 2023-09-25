local worker_healthcheck_key = KEYS[1]
local job_lock_key = KEYS[2]
local active_jobs_key = KEYS[3]
local jobs_meta_key = KEYS[4]
local job_queue_key = KEYS[5]

local worker_id = ARGV[1]
local timestamp = tonumber(ARGV[2])
local job_lock_timeout = tonumber(ARGV[3])

redis.call("hset", worker_healthcheck_key, worker_id, timestamp)

-- if the job-lock is held by another worker, return false
if not redis.call("set", job_lock_key, worker_id, "PX", job_lock_timeout, "NX") then
  return false
end

-- collect all workers that have missed their healthcheck
local worker_healthchecks = redis.call("hgetall", worker_healthcheck_key)
local dead_workers = {}
for worker, last_timestamp in pairs(worker_healthchecks) do
    local last_timestamp = tonumber(last_timestamp)
    if not last_timestamp then
        dead_workers[worker] = worker
    elseif tonumber(last_timestamp) < timestamp - job_lock_timeout then
        dead_workers[worker] = worker
    end
end

-- do nothing if no workers have missed their healthcheck
if #dead_workers > 0 then
    redis.call("hdel", worker_healthcheck_key, unpack(dead_workers))
else
    return true
end

-- move all active jobs from dead workers back to the queue
local active_jobs = redis.call("smembers", active_jobs_key)
for job in pairs(active_jobs) do
    local meta = cjson.decode(redis.call("hget", jobs_meta_key, job))
    -- if meta['worker'] in dead_workers:
    if dead_workers[meta['worker']] then
        redis.call("srem", active_jobs_key, job)
        redis.call("lpush", job_queue_key, job)
    end
end

return true