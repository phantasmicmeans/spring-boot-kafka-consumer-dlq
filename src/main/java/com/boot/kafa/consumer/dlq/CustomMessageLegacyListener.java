package com.boot.kafa.consumer.dlq;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import com.boot.kafa.consumer.dlq.model.CustomMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * Kafka-Binder
 * <spring-cloud-stream-binder-kafka>
 *
 * config: application-legacy.yml
 */
@Slf4j
@Service
@Profile(value = "legacy")
@EnableBinding(Sink.class)
public class CustomMessageLegacyListener {

	@StreamListener(Sink.INPUT)
	public void process(Message<CustomMessage> message) {
		log.info("process() " + message.getPayload().getData());

		/**
		 * do something
		 */

		throw new RuntimeException(); // exception -> to dead-letter-topic
	}
}
