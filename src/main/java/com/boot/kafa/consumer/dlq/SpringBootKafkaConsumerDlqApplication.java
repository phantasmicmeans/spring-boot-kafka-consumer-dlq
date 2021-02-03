package com.boot.kafa.consumer.dlq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.boot.kafa.consumer.dlq.dynamic.config.KafkaDynamicEndpointBindingConfigurationProperties;

@EnableConfigurationProperties(KafkaDynamicEndpointBindingConfigurationProperties.class)
@SpringBootApplication
public class SpringBootKafkaConsumerDlqApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootKafkaConsumerDlqApplication.class, args);
	}

}
