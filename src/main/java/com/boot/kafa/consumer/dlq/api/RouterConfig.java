package com.boot.kafa.consumer.dlq.api;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.boot.kafa.consumer.dlq.producer.ProducingHandler;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class RouterConfig {
	private final ProducingHandler producingHandler;

	@Bean
	public RouterFunction<ServerResponse> routesProduce() {
		return RouterFunctions.route(GET("/produce").and(accept(MediaType.APPLICATION_JSON)), producingHandler::execute);
	}
}
