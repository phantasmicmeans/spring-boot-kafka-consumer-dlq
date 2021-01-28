package com.boot.kafa.consumer.dlq.config;

import org.springframework.cloud.stream.annotation.StreamRetryTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RetryConfig {

	@Bean("process-in-0-RetryTemplate") // name for multiple input binding
	@StreamRetryTemplate
	public RetryTemplate retryTemplate() {
		RetryTemplate retryTemplate = new RetryTemplate();
		RetryPolicy retryPolicy = new SimpleRetryPolicy(3);
		FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
		backOffPolicy.setBackOffPeriod(1000);

		retryTemplate.setBackOffPolicy(backOffPolicy);
		retryTemplate.setRetryPolicy(retryPolicy);

		return retryTemplate;
	}
}