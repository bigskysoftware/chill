
local job_id = ARGV[1]
local worker_id = ARGV[2]
local timestamp = tonumber(ARGV[3])
local error_value = ARGV[4]
local backoff_max = tonumber(ARGV[5])

local jobs_meta_key = KEYS[1]
local active_jobs_key = KEYS[2]
local errored_jobs_key = KEYS[3]
local backoff_queue_key = KEYS[4]
local failed_jobs_key = KEYS[5]

local meta = cjson.decode(redis.call("hget", jobs_meta_key, job_id))

if meta['status'] ~= 'ASSIGNED' and meta['status'] ~= 'RUNNING' then
    return false
end

if meta['worker'] ~= worker_id then
    return false
end

meta['status'] = 'FAILED'
meta['timestamp'] = timestamp
meta['worker'] = nil
meta['error'] = error_value
meta['retries'] = (meta['retries'] or 0) + 1

redis.call("hset", jobs_meta_key, job_id, cjson.encode(meta))
redis.call("srem", active_jobs_key, job_id)

if meta['retries'] < backoff_max then
    redis.call("lpush", backoff_queue_key, job_id)
else
    redis.call("sadd", failed_jobs_key, job_id)
end