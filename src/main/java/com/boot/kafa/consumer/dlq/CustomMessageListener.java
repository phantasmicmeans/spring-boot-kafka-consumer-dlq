package com.boot.kafa.consumer.dlq;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Service;
import org.springframework.util.backoff.FixedBackOff;

import com.boot.kafa.consumer.dlq.model.CustomMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <spring-kafka>
 *
 * config: application-original.yml
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Profile(value = "original")
public class CustomMessageListener {

	@Value(value = "${spring.kafka.bootstrap-servers}")
	private String bootstrapAddress;

	@Value(value = "${dynamic-kafka.dlq}")
	private String dlqTopic;

	public Map<String, Object> jsonDeserializeConsumerConfigs(String groupId) {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
		return props;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, CustomMessage> customMessageKafkaListenerContainerFactory(
			KafkaOperations<String, Object> operations) {
		ConcurrentKafkaListenerContainerFactory<String, CustomMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(jsonDeserializeConsumerConfigs("custom-message-processor"),
				new StringDeserializer(),
				new JsonDeserializer<>(CustomMessage.class)
		));

		factory.setErrorHandler(new SeekToCurrentErrorHandler(
				new DeadLetterPublishingRecoverer(operations, (cr, e) -> new TopicPartition("history-5m-retry", cr.partition())),
				new FixedBackOff(1000L, 1)));
		return factory;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, CustomMessage> retry5mKafkaListenerContainerFactory(
			KafkaOperations<String, Object> operations) {
		ConcurrentKafkaListenerContainerFactory<String, CustomMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(jsonDeserializeConsumerConfigs(""),
				new StringDeserializer(),
				new JsonDeserializer<>(CustomMessage.class)
		));

		factory.setErrorHandler(new SeekToCurrentErrorHandler(
				new DeadLetterPublishingRecoverer(operations, (cr, e) -> new TopicPartition("history-10m-retry", cr.partition())),
				new FixedBackOff(1000L, 1)));
		return factory;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, CustomMessage> retry10mKafkaListenerContainerFactory(
			KafkaOperations<String, Object> operations) {
		ConcurrentKafkaListenerContainerFactory<String, CustomMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(jsonDeserializeConsumerConfigs(""),
				new StringDeserializer(),
				new JsonDeserializer<>(CustomMessage.class)
		));

		factory.setErrorHandler(new SeekToCurrentErrorHandler(
				new DeadLetterPublishingRecoverer(operations, (cr, e) -> new TopicPartition("custom-message-dlq", cr.partition())),
				new FixedBackOff(1000L, 1)));
		return factory;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, CustomMessage> retry20mKafkaListenerContainerFactory(
			KafkaOperations<String, Object> operations) {
		ConcurrentKafkaListenerContainerFactory<String, CustomMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(jsonDeserializeConsumerConfigs(""),
				new StringDeserializer(),
				new JsonDeserializer<>(CustomMessage.class)
		));

		factory.setErrorHandler(new SeekToCurrentErrorHandler(
				new DeadLetterPublishingRecoverer(operations, (cr, e) -> new TopicPartition(dlqTopic, cr.partition())),
				new FixedBackOff(1000L, 1)));
		return factory;
	}

	@KafkaListener(
			id = "custom-message-processor",
			topics = "custom-message-topic",
			containerFactory = "customMessageKafkaListenerContainerFactory"
	)
	public void listen(CustomMessage value) {
		log.info("\nlisten() " + value.getData() + "\n");

		/**
		 * do something
		 */

		throw new RuntimeException();  // exception -> to dead-letter-topic
	}

	@KafkaListener
	public void listener5m(CustomMessage message) {
		log.info("\nlistener5m() " + message.getData() + "\n");
	}
}
