package com.boot.kafa.consumer.dlq;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.backoff.FixedBackOff;

import com.boot.kafa.consumer.dlq.model.CustomMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * <spring-kafka>
 *
 * config: application-original.yml
 */
@Slf4j
@Service
@Profile(value = "original")
public class CustomMessageListener {

	@Value(value = "${spring.kafka.bootstrap-servers}")
	private String bootstrapAddress;

	public ProducerFactory<String, Object> dlqProducerFactory() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		return new DefaultKafkaProducerFactory<>(configProps);
	}

	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate() {
		return new KafkaTemplate<>(dlqProducerFactory());
	}

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
			KafkaOperations<String, Object> operations,
			@Qualifier("process-in-0-RetryTemplate") RetryTemplate retryTemplate) {
		ConcurrentKafkaListenerContainerFactory<String, CustomMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(jsonDeserializeConsumerConfigs("custom-message-processor"),
				new StringDeserializer(),
				new JsonDeserializer<>(CustomMessage.class)
		));

		factory.setRetryTemplate(retryTemplate);
		// dlq
		factory.setErrorHandler(new SeekToCurrentErrorHandler(
				new DeadLetterPublishingRecoverer(operations, (cr, e) -> new TopicPartition("custom-message-dlq", cr.partition())),
				new FixedBackOff(1000L, 1)));
		return factory;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, CustomMessage> dltContainerFactory(KafkaTemplate<String, Object> template) {
		ConcurrentKafkaListenerContainerFactory<String, CustomMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(jsonDeserializeConsumerConfigs("content-message-processor-dlt"),
				new StringDeserializer(),
				new JsonDeserializer<>(CustomMessage.class)
		));
		return factory;
	}

	@KafkaListener(
			id = "custom-message-processor",
			topics = "custom-message-topic",
			containerFactory = "customMessageKafkaListenerContainerFactory"
	)
	public void listen(CustomMessage value) {
		log.info("listen() " + value.getData());

		/**
		 * do something
		 */

		throw new RuntimeException();  // exception -> to dead-letter-topic
	}

	@KafkaListener(
			id = "custom-message-processor-dlt",
			topics = "custom-message-dlq",
			containerFactory = "dltContainerFactory"
	)
	public void dltListen(CustomMessage message) {
		log.info("dltListen() " + message.getData());
	}
}
