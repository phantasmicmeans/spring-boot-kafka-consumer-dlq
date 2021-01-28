package com.boot.kafa.consumer.dlq;

import java.util.function.Consumer;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.boot.kafa.consumer.dlq.model.CustomMessage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka-Streams Binder
 * <spring-cloud-stream-binder-kafka-streams>
 *
 * config: application-functional.yml
 */
@Slf4j
@Service
@Profile(value = "functional")
@AllArgsConstructor
public class CustomMessageFunctionalListener {

	@Bean
	public Consumer<KStream<String, CustomMessage>> process() {
		return input -> input.foreach((key, value) -> {
			//Critical business logic goes here.
			log.info(value.getData());

			/**
			 * produce message to dead-letter-queue is not work.
			 * it only allows to produce message to DLQ when deserialization error
			 *
			 * <this case>
			 * exception -> stream thread will die -> shutdown
			 *
			 * must create producing logic to DLQ when fails
			 */
			throw new RuntimeException();
		});

	}
}