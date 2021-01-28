package com.boot.kafa.consumer.dlq.model;

import org.apache.kafka.common.serialization.Serde;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;

public class CustomSerdes {

	@Bean
	public Serde<CustomMessage> message() {
		return new JsonSerde<>(CustomMessage.class);
	}
}
