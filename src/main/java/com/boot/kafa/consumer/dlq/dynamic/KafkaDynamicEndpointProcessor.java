package com.boot.kafa.consumer.dlq.dynamic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.KafkaListenerAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;

import com.boot.kafa.consumer.dlq.dynamic.config.KafkaDynamicEndpointBindingConfigurationProperties;
import com.boot.kafa.consumer.dlq.dynamic.config.KafkaDynamicRetryProperties;
import com.boot.kafa.consumer.dlq.utils.JsonUtils;

import lombok.Getter;

@Component
@ConditionalOnProperty(name = "dynamic-kafka.enable", havingValue = "true")
public class KafkaDynamicEndpointProcessor extends KafkaListenerAnnotationBeanPostProcessor<String, Object> implements InitializingBean {
	private final ApplicationContext context;
	private final KafkaDynamicEndpointBindingConfigurationProperties bindingProperties;

	public KafkaDynamicEndpointProcessor(ApplicationContext context, KafkaDynamicEndpointBindingConfigurationProperties bindingProperties) {
		this.context = context;
		this.bindingProperties = bindingProperties;
	}

	@Override
	public void afterPropertiesSet() {
		Class<?> defaultClazzName = bindingProperties.getDefaultProperties().getClassPath();
		String methodName = bindingProperties.getDefaultProperties().getMethodName();

		Tuple<Method, KafkaListener> listenerTuple = findListenerInfo(defaultClazzName, methodName);
		run(listenerTuple);
	}

	public void run(Tuple<Method, KafkaListener> listenerTuple) {
		Object handler = Proxy.getInvocationHandler(listenerTuple.getV());
		Field field;

		try {
			field = handler.getClass().getDeclaredField("memberValues");
			field.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new IllegalStateException(e);
		}

		Class<?> defaultClazzType = bindingProperties.getDefaultProperties().getClassPath();
		assert defaultClazzType != null;
		Object bean = context.getBean(defaultClazzType);

		registerMainListener(listenerTuple, bean);
		Map<String, Object> properties = bindingProperties.getRetry();

		for (String topic : properties.keySet()) {
			KafkaDynamicRetryProperties retryProperties = JsonUtils.convert(properties.get(topic), KafkaDynamicRetryProperties.class);
			Method method = listenerTuple.getK();
			KafkaListener kafkaListener = listenerTuple.getV();

			String methodName = retryProperties.getMethodName();
			Class<?> clazzType = retryProperties.getClassPath();

			if (methodName != null && !methodName.isEmpty()) {
				clazzType = (clazzType == null) ? defaultClazzType : clazzType;
				listenerTuple = findListenerInfo(clazzType, methodName);

				method = listenerTuple.getK();
				kafkaListener = listenerTuple.getV();
				handler = Proxy.getInvocationHandler(kafkaListener);
			}

			Map<String, Object> attributes;
			try {
				attributes = (Map<String, Object>) field.get(handler);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}

			if (!attributes.containsKey("topics")) {
				throw new NoSuchElementException("KafkaListener not contains 'topics' attributes");
			}

			String[] newTopics = topics(topic);

			attributes.put("topics", newTopics);
			attributes.put("id", retryProperties.getId());
			attributes.put("groupId", retryProperties.getGroupId());
			attributes.put("containerFactory", retryProperties.getContainerFactory());

			super.processKafkaListener(kafkaListener, method, bean, retryProperties.getId());
		}
	}

	private Tuple<Method, KafkaListener> findListenerInfo(Class<?> clazz, String methodName) {
		Map<Method, KafkaListener> annotatedMethod = MethodIntrospector.selectMethods(clazz,
				(MethodIntrospector.MetadataLookup<KafkaListener>) method -> AnnotatedElementUtils.findMergedAnnotation(method, KafkaListener.class));

		Set<Method> keys = annotatedMethod.keySet();

		Method listenerMethod = keys.stream()
				.filter(method -> method.getName().equals(methodName))
				.findAny()
				.orElseThrow(() -> new RuntimeException(String.format("No Method named {0}.", methodName)));

		return Tuple.of(listenerMethod, annotatedMethod.get(listenerMethod));
	}

	public void registerMainListener(Tuple<Method, KafkaListener> tuple, Object bean) {
		Map<String, Object> parentAttributes = AnnotatedElementUtils.findMergedAnnotationAttributes(tuple.getK(), KafkaListener.class, true, true);
		assert parentAttributes != null;
		super.processKafkaListener(tuple.getV(), tuple.getK(), bean, (String) parentAttributes.get("id"));
	}

	private String[] topics(String...topic) {
		return topic;
	}

	@Getter
	static class Tuple<K, V> {
		private final K k;
		private final V v;

		private Tuple(K k, V v) {
			this.k = k;
			this.v = v;
		}

		static <K, V> Tuple<K, V> of(K k, V v) {
			return new Tuple<>(k, v);
		}
	}
}