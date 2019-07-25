/*
 * © 2018 CREALOGIX. All rights reserved.
 */
package com.mykafka.consumer;

import org.axonframework.extensions.kafka.autoconfig.KafkaAutoConfiguration;
import org.axonframework.extensions.kafka.eventhandling.DefaultKafkaMessageConverter;
import org.axonframework.extensions.kafka.eventhandling.KafkaMessageConverter;
import org.axonframework.extensions.kafka.eventhandling.consumer.Fetcher;
import org.axonframework.extensions.kafka.eventhandling.consumer.KafkaMessageSource;
import org.axonframework.serialization.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
This configuration is only needed because of the issue
https://github.com/AxonFramework/AxonFramework/issues/710
https://github.com/AxonFramework/ReferenceGuide/issues/82
 */
//@Configuration
//@AutoConfigureAfter(KafkaAutoConfiguration.class)
public class MyConfig {
	
//	@Autowired
//	public void registerInterceptors(CommandBus commandbus) {
//		commandbus.registerDispatchInterceptor(new BeanValidationInterceptor<>());
//	}
	
//	@Bean
//	public AggregateSnapshotter getAggregateSnapshotter() {
//		return new SpringAggregateSnapshotterFactoryBean().getObject();
//	}
	
//	@Bean
//	public EventTableFactory getEventTableFactory() {
//		return PostgresEventTableFactory.INSTANCE;
//	}
	
//	@Bean
//	public EventSchema getEventSchema() {
//		return new EventSchema();
//	}
//	
//	@Bean
//	public SagaSqlSchema getSagaSqlSchema() {
//		return new PostgresSagaSqlSchema();
//	}
	
//	@Autowired
//	@Bean
//	public KafkaMessageSource kafkaMessageSource(Fetcher fetcher) {
//		return new KafkaMessageSource(fetcher);
//	}
//	
//	@ConditionalOnMissingBean
//	@Bean
//	public KafkaMessageConverter<String, byte[]> kafkaMessageConverter(
//			@Qualifier("eventSerializer") Serializer eventSerializer) {
//		return DefaultKafkaMessageConverter.builder().serializer(eventSerializer).build();
//	}

//	@Bean
//	public JpaTokenStore jpaTokenStore(Serializer serializer, EntityManagerProvider emp) {
//		return JpaTokenStore.builder().entityManagerProvider(emp).serializer(serializer).build();
//	}
}
