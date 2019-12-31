/*
 * © 2018 CREALOGIX. All rights reserved.
 */
package com.mykafka.consumer;


import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.Configurer;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventsourcing.eventstore.jdbc.EventSchema;
import org.axonframework.eventsourcing.eventstore.jdbc.EventTableFactory;
import org.axonframework.eventsourcing.eventstore.jdbc.PostgresEventTableFactory;
import org.axonframework.extensions.kafka.configuration.KafkaMessageSourceConfigurer;
import org.axonframework.extensions.kafka.eventhandling.KafkaMessageConverter;
import org.axonframework.extensions.kafka.eventhandling.consumer.ConsumerFactory;
import org.axonframework.extensions.kafka.eventhandling.consumer.Fetcher;
import org.axonframework.extensions.kafka.eventhandling.consumer.subscribable.SubscribableKafkaMessageSource;
import org.axonframework.messaging.interceptors.BeanValidationInterceptor;
import org.axonframework.modelling.saga.repository.jdbc.PostgresSagaSqlSchema;
import org.axonframework.modelling.saga.repository.jdbc.SagaSqlSchema;
import org.axonframework.spring.eventsourcing.SpringAggregateSnapshotterFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/*
This configuration is only needed because of the issue
https://github.com/AxonFramework/AxonFramework/issues/710
https://github.com/AxonFramework/ReferenceGuide/issues/82
 */
@Configuration
@AutoConfigureAfter(KafkaAutoConfiguration.class)
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

    @Bean
    public KafkaMessageSourceConfigurer kafkaMessageSourceConfigurer(Configurer configurer) {
        KafkaMessageSourceConfigurer kafkaMessageSourceConfigurer = new KafkaMessageSourceConfigurer();
        configurer.registerModule(kafkaMessageSourceConfigurer);
        return kafkaMessageSourceConfigurer;
    }

    @Bean("subscribableKafkaMessageSource")
    public SubscribableKafkaMessageSource<String, byte[]> subscribableKafkaMessageSource(
            List<String> topics, String groupId, ConsumerFactory<String, byte[]> consumerFactory,
            Fetcher<String, byte[], EventMessage<?>> fetcher,
            KafkaMessageConverter<String, byte[]> messageConverter, int consumerCount,
            KafkaMessageSourceConfigurer kafkaMessageSourceConfigurer) {
        SubscribableKafkaMessageSource<String, byte[]> subscribableKafkaMessageSource =
                SubscribableKafkaMessageSource.<String, byte[]>builder()
                        .topics(topics)                     // Defaults to a collection of "Axon.Events"
                        .groupId(groupId)                   // Hard requirement
                        .consumerFactory(consumerFactory)   // Hard requirement
                        .fetcher(fetcher)                   // Hard requirement
                        .messageConverter(messageConverter) // Defaults to a "DefaultKafkaMessageConverter"
                        .consumerCount(consumerCount)       // Defaults to a single Consumer
                        .build();
        // Registering the source is required to tie into the Configurers lifecycle to start the source at the right stage
        kafkaMessageSourceConfigurer.registerSubscribableSource(configuration -> subscribableKafkaMessageSource);
        return subscribableKafkaMessageSource;
    }

    public void configureSubscribableKafkaSource(EventProcessingConfigurer eventProcessingConfigurer,
                                                 String processorName,
                                                 SubscribableKafkaMessageSource<String, byte[]> subscribableKafkaMessageSource) {
        eventProcessingConfigurer.registerSubscribingEventProcessor(
                processorName,
                configuration -> subscribableKafkaMessageSource
        );
    }


//	@ConditionalOnMissingBean
//	@Bean
//	public KafkaMessageConverter<String, byte[]> kafkaMessageConverter(
//			@Qualifier("eventSerializer") Serializer eventSerializer) {
//		return DefaultKafkaMessageConverter.builder().serializer(eventSerializer).build();
//	}
}
