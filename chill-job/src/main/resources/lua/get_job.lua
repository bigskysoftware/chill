
local pending_queue_key = KEYS[1]
local active_jobs_key = KEYS[2]
local jobs_meta_key = KEYS[3]

local worker_id = ARGV[1]

local element = redis.call("lpop", pending_queue_key)
if not element then
    return nil
end

local timestamp = redis.call("TIME")
redis.call("sadd", active_jobs_key, element)

redis.call("json.mset", jobs_meta_key .. ":" .. element,
    ".status", "ASSIGNED",
    ".worker", worker_id,
    ".timestamp", timestamp[1])

return element