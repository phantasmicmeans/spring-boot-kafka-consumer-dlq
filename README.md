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
1. Select active profile
   - consume-topic: `custom-message-topic` 
   - dead-letter-topic: `custom-message-dlq`
2. Run application
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

**설정**

`spring-cloud-stream-kafka-binder`와 `spring-cloud-stream-kafka-streams-binder`는 설정 차이가 있으므로 주의

spring-cloud-stream-kafka-binder | spring-cloud-stream-kafka-streams-binder
---------------|------------
spring.cloud.stream.kafka.binder.~ <br> spring.cloud.stream.kafka.binding.`<input>`.consumer~   | spring.cloud.stream.kafka.streams.binder.~ <br> spring.cloud.stream.kafka.streams.binding.`<input>`.consumer~


**~FunctionalListener DLQ**

- ContentHistoryFunctionalListener / CustomHistoryRetryableFunctionalListener는 `spring-cloud-stream-kafka-streams-binder`의 functional 스타일로 코딩되어 있는데, consume 로직 내에서 에러 발생시(e.g. RuntimeException) stream thread가 죽음 -> dlq 전송 X 
- spring.cloud.stream.kafka.streams.binding.<input>.consumer.deserializationExceptionHandler: sendToDlq 옵션으로 consumer가 데이터 역직렬화시(pojo로 받을 때) 에러는 dlq로 전송   

