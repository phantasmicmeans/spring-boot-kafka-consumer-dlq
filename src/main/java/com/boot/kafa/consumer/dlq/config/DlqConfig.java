package com.boot.kafa.consumer.dlq.config;

import org.springframework.cloud.stream.binder.kafka.utils.DlqDestinationResolver;
import org.springframework.cloud.stream.binder.kafka.utils.DlqPartitionFunction;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DlqConfig {

	/**
	 * code base dlq destination
	 *@Bean
	 */
	public DlqDestinationResolver destinationResolver() {
		return ((consumerRecord, e) -> {
			if (consumerRecord.topic().equals("something")) {
				return "something-dlq";
			}
			else {
				return "default-dlq";
			}
		});
	}

	/**
	 * dlq는 default로 input topic의 partition 정책 따름
	 * 따라서 record를 보낼 dlq partition 번호 지정 가능
	 *@Bean
	 */
	public DlqPartitionFunction partitionFunction() {
		return (group, record, throwable) -> 0;
	}

}
