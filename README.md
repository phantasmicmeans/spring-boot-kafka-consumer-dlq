# Spring Boot, Kafka-Dlq-Template

&nbsp;

## Consumers

consumer | library | etc 
------|----------|------
CustomMessageListener | spring-kafka | dlq, retry
CustomMessageLegacyListener | spring-cloud-stream-kafka-binder | dlq
CustomMessageFunctionalListener | spring-cloud-stream-kafka-streams-binder | dlq (only deserialization err)
CustomMessageRetryableFunctionalListener | spring-cloud-stream-kafka-streams-binder | dlq (only deserialization err), retry

&nbsp;

## Run 
1. Select an active profile
   - consume-topic: `custom-message-topic` 
   - dead-letter-topic: `custom-message-dlq`
2. Run the application
3. Trigger message
```
GET http://localhost:8080/produce 
```
4. Monitor `custom-message-dlq` topic 

```
 bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic custom-message-dlq
```

&nbsp;

## Settings 

**application.yml**
```yml
spring:
  profiles:
    active: functional
  kafka:
    bootstrap-servers: localhost:9092 # producer

#  active profile list
# 1. original - [CustomMessageListener] - [spring-kafka]
# 2. legacy - [CustomMessageLegacyListener] - [spring-cloud-stream-binder-kafka]
# 3. functional - [CustomMessageFunctionalListener] - [spring-cloud-stream-binder-kafka-stream]
# 4. functional-retryable - [CustomMessageRetryableFunctionalListener] - [spring-cloud-stream-binder-kafka-stream]


#  select listener to media.kafka.consumer below
# 1. CustomMessageListener (spring-kafka, dlq, retry O)
# 2. CustomMessageLegacyListener (spring-cloud-stream-kafka-binder, dlq)
# 3. CustomMessageFunctionalListener (spring-cloud-stream-kafka-streams-binder, dlq (only deserialization err))
# 4. CustomMessageRetryableFunctionalListener (spring-cloud-stream-kafka-streams-binder, dlq (only deserialization err), retry O)
```

&nbsp;

## Note

Do not confuse `spring-cloud-stream-kafka-binder` with `spring-cloud-stream-kafka-streams-binder`. 

**difference**

spring-cloud-stream-kafka-binder | spring-cloud-stream-kafka-streams-binder
---------------|------------
spring.cloud.stream.kafka.binder.~ <br> spring.cloud.stream.kafka.binding.`<input>`.consumer~   | spring.cloud.stream.kafka.streams.binder.~ <br> spring.cloud.stream.kafka.streams.binding.`<input>`.consumer~


**~FunctionalListener DLQ**

`ContentHistoryFunctionalListener`, `CustomHistoryRetryableFunctionalListener` are consist of functional style of `spring-cloud-stream-kafka-streams-binder`.
When an error occurs (e.g. RuntimeException) within the consume logic, stream thread dies and is subsequently shut down.

It only sends DLQ for deserialization error by the setting below, so it needs to be handled separately within consume logic.

- `spring.cloud.stream.kafka.streams.binding.<input>.consumer.deserializationExceptionHandler: sendToDlq`

