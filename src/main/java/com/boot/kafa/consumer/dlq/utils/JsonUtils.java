package com.boot.kafa.consumer.dlq.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtils {

	private static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static <T> T convert(Object data, Class<T> type) {
		return mapper.convertValue(data, type);
	}

	public static <T> T convert(String json, Class<T> clz) {
		try {
			return mapper.readValue(json, clz);
		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
			return null;
		}
	}

	public static <T> byte[] toByteArray(T data){
		try {
			return mapper.writeValueAsBytes(data);
		} catch (Exception e) {
			log.error(e.getMessage());
			return null;
		}
	}

	public static <T> void print(T data) {
		try {
			log.info(mapper.writeValueAsString(data));
		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
		}
	}

}
