package com.boot.kafa.consumer.dlq.producer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.boot.kafa.consumer.dlq.model.CustomMessage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple Producer
 */
@Slf4j
@Component
@AllArgsConstructor
public class CustomMessageProducer {
	private KafkaTemplate<String, Object> kafkaTemplate;

	public void send(CustomMessage message) {
		try {
			kafkaTemplate.send("custom-message-topic", message).get(3, TimeUnit.SECONDS);
		} catch (ExecutionException | TimeoutException | InterruptedException e) {
			log.error(e.getMessage());
		}
	}
}
