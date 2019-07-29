/*
 * © 2018 CREALOGIX. All rights reserved.
 */
package com.mykafka.consumer;

import java.time.Duration;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore;
import org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore.Builder;
import org.axonframework.eventsourcing.eventstore.jdbc.EventSchema;
import org.axonframework.eventsourcing.eventstore.jdbc.EventTableFactory;
import org.axonframework.eventsourcing.eventstore.jdbc.PostgresEventTableFactory;
import org.axonframework.extensions.kafka.eventhandling.DefaultKafkaMessageConverter;
import org.axonframework.extensions.kafka.eventhandling.KafkaMessageConverter;
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

import com.mykafka.consumer.tokenstore.MyTokenStore;

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
	public Builder jpaTokenStoreBuilder(Serializer serializer, EntityManagerProvider emp) {
		System.out.println("MyConfig.jpaTokenStoreBuilder");
		return JpaTokenStore.builder().entityManagerProvider(emp).serializer(serializer).claimTimeout(Duration.ofMillis(700));
	}

	@Bean
	public MyTokenStore myTokenStore(Serializer serializer, EntityManagerProvider emp, Builder builder) {
		System.out.println("MyConfig.myTokenStore");
		MyTokenStore tokenStore = new MyTokenStore(builder, serializer, emp);
		return tokenStore;
	}

//	@Bean
//	public JpaTokenStore jpaTokenStore(Serializer serializer, EntityManagerProvider emp, Builder builder) {
//	JpaTokenStore jpaTokenStore =
	// builder.entityManagerProvider(emp).serializer(serializer)
//					.claimTimeout(Duration.ofMillis(500)).build();
//		System.out.println("Inside jpaTokenStore bean initialization");
//		return builder.build();
//	}
}
