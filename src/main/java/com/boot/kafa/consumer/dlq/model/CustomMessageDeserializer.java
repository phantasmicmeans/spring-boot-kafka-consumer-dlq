package com.boot.kafa.consumer.dlq.model;

import java.nio.charset.Charset;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;

import com.boot.kafa.consumer.dlq.utils.JsonUtils;

public class CustomMessageDeserializer implements Deserializer<CustomMessage> {

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) { }

	@Override
	public CustomMessage deserialize(String topic, byte[] data) {
		String contentMessage = new String(data, Charset.forName("UTF-8"));
		return JsonUtils.convert(contentMessage, CustomMessage.class);
	}

	@Override
	public void close() { }
}
