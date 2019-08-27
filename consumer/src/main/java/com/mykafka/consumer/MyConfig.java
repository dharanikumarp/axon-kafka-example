/*
 * © 2018 CREALOGIX. All rights reserved.
 */
package com.mykafka.consumer;


import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.axonframework.eventhandling.async.FullConcurrencyPolicy;
import org.axonframework.eventsourcing.eventstore.jdbc.EventSchema;
import org.axonframework.eventsourcing.eventstore.jdbc.EventTableFactory;
import org.axonframework.eventsourcing.eventstore.jdbc.PostgresEventTableFactory;
import org.axonframework.extensions.kafka.eventhandling.DefaultKafkaMessageConverter;
import org.axonframework.extensions.kafka.eventhandling.KafkaMessageConverter;
import org.axonframework.extensions.kafka.eventhandling.consumer.Fetcher;
import org.axonframework.extensions.kafka.eventhandling.consumer.KafkaMessageSource;
import org.axonframework.messaging.interceptors.BeanValidationInterceptor;
import org.axonframework.modelling.saga.repository.jdbc.PostgresSagaSqlSchema;
import org.axonframework.modelling.saga.repository.jdbc.SagaSqlSchema;
import org.axonframework.serialization.Serializer;
import org.axonframework.spring.eventsourcing.SpringAggregateSnapshotterFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
This configuration is only needed because of the issue
https://github.com/AxonFramework/AxonFramework/issues/710
https://github.com/AxonFramework/ReferenceGuide/issues/82
 */
@Configuration
//@AutoConfigureAfter(KafkaAutoConfiguration.class)
public class MyConfig {

	@Autowired
	public void registerInterceptors(CommandBus commandbus) {
		commandbus.registerDispatchInterceptor(new BeanValidationInterceptor<>());
	}

	@Bean
	public SpringAggregateSnapshotterFactoryBean springAggregateSnapshotterFactoryBean() {
		return new SpringAggregateSnapshotterFactoryBean();
	}

	@Bean
	public EventTableFactory getEventTableFactory() {
		return PostgresEventTableFactory.INSTANCE;
	}

	@Bean
	public EventSchema getEventSchema() {
		return new EventSchema();
	}

	@Bean
	public SagaSqlSchema getSagaSqlSchema() {
		return new PostgresSagaSqlSchema();
	}
	
	@ConditionalOnMissingBean
	@Bean
	public KafkaMessageConverter<String, byte[]> kafkaMessageConverter(
			@Qualifier("eventSerializer") Serializer eventSerializer) {
		return DefaultKafkaMessageConverter.builder().serializer(eventSerializer).build();
	}
	
	@Bean
	public KafkaMessageSource kms(Fetcher fetcher) {
		return new KafkaMessageSource(fetcher);
	}
	
	@Autowired
	public EventProcessingConfigurer configureEPC(EventProcessingConfigurer epc, KafkaMessageSource kms) {
		return epc.registerTrackingEventProcessor("MyProcessor", 
				c -> kms, 
				c -> TrackingEventProcessorConfiguration.forParallelProcessing(4));
	}
}
