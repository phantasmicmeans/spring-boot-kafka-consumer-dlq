package com.boot.kafa.consumer.dlq.model;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import com.boot.kafa.consumer.dlq.utils.JsonUtils;

public class CustomMessageSerde implements Serde<CustomMessage> {

	CustomMessageDeserializer deserializer = new CustomMessageDeserializer();

	@Override
	public Serializer<CustomMessage> serializer() {
		return (topic, data1) -> JsonUtils.toByteArray(data1);
	}

	@Override
	public Deserializer<CustomMessage> deserializer() {
		return deserializer;
	}
}
