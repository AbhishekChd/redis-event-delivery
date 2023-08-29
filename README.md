# Redis Event Delivery

<p>
<img src="https://img.shields.io/github/license/AbhishekChd/redis-event-delivery"
<a href="https://github.com/AbhishekChd/redis-event-delivery/pkgs/container/redis-event-delivery/122973868?tag=main">
   <img src="https://ghcr-badge.egpl.dev/abhishekchd/redis-event-delivery/tags?ignore=sha256*&label=image%20tag">
</a>
<img src="https://ghcr-badge.egpl.dev/abhishekchd/redis-event-delivery/size?tag=main">
</p>

A pub-sub system to fan-out multiple requests/events to multiple destinations based on following features:
1. Durability
2. At-least-once delivery
3. Retry backoff and limit: We have flex
4. Maintaining order
5. Delivery Isolation

### 1. Durability
Once API receives the event, it persists the data before performing any other actions making the event data secure and durable.

### 2. At-least-once delivery
Whenever failure occurs, the system will push to publish it again until it gets at least one successful publish.

### 3. Retry backoff and limit
We have flexible `MAX_RETRY` limit to stop error prone events to block queue.

We further have an **Exponential backoff** algorithm implemented for retries so would not overload the Client API.

### 4. Maintaining order
The events always maintain a FIFO ordering based on `event recieved` time and will always follow the same even in case of failures.

### 5. Delivery Isolation
Current implementation does not support isolation, but is open to extension. Isolation can be achieved by separating channels tp publish, and also the database table based on `destination`. This will make queues isolated and much faster in execution.

---

## Run Application

```shell
# We can change modify image in docker-compose.yml for live image form GitHub
# Or build local project Docker image like below 
docker build . --tag abhishekchd/redis-event-delivery

# Deploy the container to Docker
 docker compose -f ./docker-compose.yml -p redis-event-delivery up -d
 
# Call API to verify app is up and running
curl --location --request POST 'http://localhost:8080/publish-event' \
--header 'Content-Type: application/json' \
--data '{
    "userId": "user-id-1",
    "payload": "Event payload"
}'

# Output on live server
$ Received event: Event(userId=user-id-1, payload=Event payload)
```

### Requirements for local development
1. PostgresSQL 15
2. Redis server

---

## Design

### Event API [Producer]

```
POST /publish-event HTTP/1.1
Host: localhost:8080
Content-Type: application/json
{
    "userId": "<user-id>",
    "payload": "<Event Payload>"
}
``` 

### Success Event

After successful event the data is deleted from database permanently.

```
INFO 46139 --- [pool-3-thread-1] i.g.a.r.scheduler.EventScheduler         : Event Watcher Running
INFO 46139 --- [nio-8080-exec-2] i.g.a.r.c.EventDeliveryController        : Saved entity to database. UUID: 7f000001-8a3c-1f0e-818a-3cef5b3f0000, Entity: EventEntity(sequenceId=7f000001-8a3c-1f0e-818a-3cef5b3f0000, userId=user-id-1, data=Message from Postman, eventCreatedAt=2023-08-28 21:47:14.039, tryCount=0, status=PENDING)
INFO 46139 --- [pool-3-thread-1] i.g.a.r.scheduler.EventScheduler         : Event Watcher Running
INFO 46139 --- [pool-3-thread-1] i.g.a.r.scheduler.EventScheduler         : Loaded event 7f000001-8a3c-1f0e-818a-3cef5b3f0000 from database
INFO 46139 --- [pool-3-thread-1] i.g.a.r.scheduler.EventScheduler         : Scheduled event 7f000001-8a3c-1f0e-818a-3cef5b3f0000 with delay of 502
INFO 46139 --- [pool-3-thread-2] i.g.a.r.publisher.RedisEventPublisher    : Channel: sample-queue, Message: Event(userId=user-id-1, payload=Message from Postman)
INFO 46139 --- [pool-4-thread-1] i.g.a.r.subscriber.EventSubscriber       : Received <Event(userId=user-id-1, payload=Message from Postman)>
INFO 46139 --- [pool-4-thread-1] i.g.a.r.subscriber.EventSubscriber       : Processed Message <{"userId":"user-id-1","payload":"Message from Postman"}>
INFO 46139 --- [pool-3-thread-2] i.g.a.r.scheduler.EventScheduler         : Event 7f000001-8a3c-1f0e-818a-3cef5b3f0000 successfully published in 0 retries
INFO 46139 --- [pool-3-thread-3] i.g.a.r.scheduler.EventScheduler         : Event Watcher Running

```

### Failure Event

We have `Scheduled Events` with delay using backoff algorithm, and always the FIFO ordered retry.

```
INFO 44693 --- [pool-3-thread-1] i.g.a.r.scheduler.EventScheduler         : Event Watcher Running
INFO 44693 --- [pool-3-thread-1] i.g.a.r.scheduler.EventScheduler         : Loaded event 7f000001-8a3c-1e16-818a-3cde32ad0000 from database
INFO 44693 --- [pool-3-thread-1] i.g.a.r.scheduler.EventScheduler         : Scheduled event 7f000001-8a3c-1e16-818a-3cde32ad0000 with delay of 508
INFO 44693 --- [pool-3-thread-2] i.g.a.r.publisher.RedisEventPublisher    : Channel: sample-queue, Message: Event(userId=user-id-1, payload=Message from Postman)
INFO 44693 --- [pool-4-thread-1] i.g.a.r.subscriber.EventSubscriber       : Received <Event(userId=user-id-1, payload=Message from Postman)>
INFO 44693 --- [pool-4-thread-1] i.g.a.r.subscriber.EventSubscriber       : Processed Message <{"userId":"user-id-1","payload":"Message from Postman"}>
INFO 44693 --- [pool-3-thread-2] i.g.a.r.scheduler.EventScheduler         : Event 7f000001-8a3c-1e16-818a-3cde32ad0000 failed - Failed to deliver. Pushing for retry.
INFO 44693 --- [pool-3-thread-3] i.g.a.r.scheduler.EventScheduler         : Event Watcher Running
INFO 44693 --- [pool-3-thread-3] i.g.a.r.scheduler.EventScheduler         : Loaded event 7f000001-8a3c-1e16-818a-3cde32ad0000 from database
INFO 44693 --- [pool-3-thread-3] i.g.a.r.scheduler.EventScheduler         : Scheduled event 7f000001-8a3c-1e16-818a-3cde32ad0000 with delay of 1008
INFO 44693 --- [pool-3-thread-2] i.g.a.r.publisher.RedisEventPublisher    : Channel: sample-queue, Message: Event(userId=user-id-1, payload=Message from Postman)
INFO 44693 --- [pool-4-thread-2] i.g.a.r.subscriber.EventSubscriber       : Received <Event(userId=user-id-1, payload=Message from Postman)>
INFO 44693 --- [pool-4-thread-2] i.g.a.r.subscriber.EventSubscriber       : Processed Message <{"userId":"user-id-1","payload":"Message from Postman"}>
INFO 44693 --- [pool-3-thread-2] i.g.a.r.scheduler.EventScheduler         : Event 7f000001-8a3c-1e16-818a-3cde32ad0000 failed - Failed to deliver. Pushing for retry.
INFO 44693 --- [pool-3-thread-1] i.g.a.r.scheduler.EventScheduler         : Event Watcher Running
INFO 44693 --- [pool-3-thread-1] i.g.a.r.scheduler.EventScheduler         : Loaded event 7f000001-8a3c-1e16-818a-3cde32ad0000 from database
INFO 44693 --- [pool-3-thread-1] i.g.a.r.scheduler.EventScheduler         : Scheduled event 7f000001-8a3c-1e16-818a-3cde32ad0000 with delay of 2004
INFO 44693 --- [pool-3-thread-4] i.g.a.r.publisher.RedisEventPublisher    : Channel: sample-queue, Message: Event(userId=user-id-1, payload=Message from Postman)
INFO 44693 --- [pool-4-thread-3] i.g.a.r.subscriber.EventSubscriber       : Received <Event(userId=user-id-1, payload=Message from Postman)>
INFO 44693 --- [pool-4-thread-3] i.g.a.r.subscriber.EventSubscriber       : Processed Message <{"userId":"user-id-1","payload":"Message from Postman"}>
INFO 44693 --- [pool-3-thread-4] i.g.a.r.scheduler.EventScheduler         : Event 7f000001-8a3c-1e16-818a-3cde32ad0000 failed - Failed to deliver. Pushing for retry.
INFO 44693 --- [pool-3-thread-3] i.g.a.r.scheduler.EventScheduler         : Event Watcher Running
INFO 44693 --- [pool-3-thread-3] i.g.a.r.scheduler.EventScheduler         : Loaded event 7f000001-8a3c-1e16-818a-3cde32ad0000 from database
INFO 44693 --- [pool-3-thread-3] i.g.a.r.scheduler.EventScheduler         : Scheduled event 7f000001-8a3c-1e16-818a-3cde32ad0000 with delay of 3994
INFO 44693 --- [pool-3-thread-5] i.g.a.r.publisher.RedisEventPublisher    : Channel: sample-queue, Message: Event(userId=user-id-1, payload=Message from Postman)
INFO 44693 --- [pool-4-thread-4] i.g.a.r.subscriber.EventSubscriber       : Received <Event(userId=user-id-1, payload=Message from Postman)>
INFO 44693 --- [pool-4-thread-4] i.g.a.r.subscriber.EventSubscriber       : Processed Message <{"userId":"user-id-1","payload":"Message from Postman"}>
INFO 44693 --- [pool-3-thread-5] i.g.a.r.scheduler.EventScheduler         : Event 7f000001-8a3c-1e16-818a-3cde32ad0000 failed - Failed to deliver. Pushing for retry.
INFO 44693 --- [pool-3-thread-2] i.g.a.r.scheduler.EventScheduler         : Event Watcher Running
INFO 44693 --- [pool-3-thread-2] i.g.a.r.scheduler.EventScheduler         : Loaded event 7f000001-8a3c-1e16-818a-3cde32ad0000 from database
INFO 44693 --- [pool-3-thread-2] i.g.a.r.scheduler.EventScheduler         : Scheduled event 7f000001-8a3c-1e16-818a-3cde32ad0000 with delay of 7997
INFO 44693 --- [pool-3-thread-2] i.g.a.r.publisher.RedisEventPublisher    : Channel: sample-queue, Message: Event(userId=user-id-1, payload=Message from Postman)
INFO 44693 --- [pool-4-thread-1] i.g.a.r.subscriber.EventSubscriber       : Received <Event(userId=user-id-1, payload=Message from Postman)>
INFO 44693 --- [pool-4-thread-1] i.g.a.r.subscriber.EventSubscriber       : Processed Message <{"userId":"user-id-1","payload":"Message from Postman"}>
INFO 44693 --- [pool-3-thread-2] i.g.a.r.scheduler.EventScheduler         : Event 7f000001-8a3c-1e16-818a-3cde32ad0000 failed - Failed to deliver. Retry limit of 4 reached.
INFO 44693 --- [pool-3-thread-3] i.g.a.r.scheduler.EventScheduler         : Event Watcher Running
```
