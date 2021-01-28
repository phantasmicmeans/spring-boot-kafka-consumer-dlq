package com.boot.kafa.consumer.dlq;

import java.util.function.Consumer;

import org.apache.kafka.streams.errors.ProductionExceptionHandler;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import com.boot.kafa.consumer.dlq.model.CustomMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * Kafka-Streams Binder
 * <spring-cloud-stream-binder-kafka-streams>
 *
 * <Retrying critical business logic, exception handling to recovery block>
 *
 * config: application-functional.yml
 */
@Slf4j
@Service
@Profile(value = "functional-retryable")
public class CustomMessageRetryableFunctionalListener {

	/**
	 * retry policy configuration -> {@link com.boot.kafa.consumer.dlq.config.RetryConfig}
	 */
	@Bean
	public Consumer<KStream<String, CustomMessage>> process(@Lazy @Qualifier("process-in-0-RetryTemplate") RetryTemplate retryTemplate) {
		return input -> input.process(() -> new Processor<String, CustomMessage>() {
			@Override
			public void init(ProcessorContext context) { }

			@Override
			public void process(String key, CustomMessage value) {
				retryTemplate.execute(context -> {
					//Critical business logic goes here.
					log.info(value.getData());

					/**
					 * produce message to dead-letter-queue is not work.
					 * it only allows to produce message to DLQ when deserialization error
					 */
					throw new RuntimeException();

					}, context -> {
					/**
					 * [Recovery Block]
					 * When exception occurs without recovery block below, stream thread will die and shutdown
					 * so, you must create producing logic to DLQ when fails
					 */
						int retryCount = context.getRetryCount();
						Throwable throwable = context.getLastThrowable();
						log.error("Kafka message marked as processed although it failed. RetryCount: {}], Err Message: [{}]", retryCount, throwable.getMessage());
						return ProductionExceptionHandler.ProductionExceptionHandlerResponse.CONTINUE;
					}
				);
			}

			@Override
			public void close() { }
		});

	}
}
