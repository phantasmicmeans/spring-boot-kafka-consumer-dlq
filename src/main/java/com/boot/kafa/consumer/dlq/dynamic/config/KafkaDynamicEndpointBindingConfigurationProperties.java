package com.boot.kafa.consumer.dlq.dynamic.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "dynamic-kafka")
public class KafkaDynamicEndpointBindingConfigurationProperties {
	@JsonProperty("default")
	private Default defaultProperties;
	private Map<String, Object> retry;
	private String dlq;

	@Getter
	@Setter
	public static class Default {
		private Class<?> classPath;
		private String methodName;
	}

	@JsonIgnore
	public Default getDefaultProperties() {
		return defaultProperties;
	}

	public Default getDefault() {
		return this.defaultProperties;
	}

	public void setDefault(Default aDefault) {
		this.defaultProperties = aDefault;
	}
}