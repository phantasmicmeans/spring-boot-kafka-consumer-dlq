package com.boot.kafa.consumer.dlq.dynamic.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KafkaDynamicRetryProperties {
	private String interval;
	private String id;
	private String containerFactory;
	private String groupId;
	private Class<?> classPath;
	private String methodName;
}
