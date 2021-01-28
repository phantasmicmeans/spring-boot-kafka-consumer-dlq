package com.boot.kafa.consumer.dlq.producer;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.boot.kafa.consumer.dlq.model.CustomMessage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@AllArgsConstructor
public class ProducingHandler {
	private CustomMessageProducer producer;

	public Mono<ServerResponse> execute(ServerRequest request) {
		producer.send(CustomMessage.builder()
				.data("test")
				.count(1)
				.build()
		);

		return ServerResponse.ok().build();
	}
}
