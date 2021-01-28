package com.boot.kafa.consumer.dlq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomMessage {
	String data;
	Integer count;
}
