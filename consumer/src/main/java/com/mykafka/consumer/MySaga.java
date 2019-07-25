/*
 * © 2018 CREALOGIX. All rights reserved.
 */
package com.mykafka.consumer;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mykafka.consumer.events.MySagaEndEvent;
import com.mykafka.consumer.events.MySagaStartEvent;

@Saga
@ProcessingGroup("MySagaProcessor")
public class MySaga {

  private static final Logger LOGGER = LoggerFactory.getLogger(MySaga.class);

  @StartSaga
  @SagaEventHandler(associationProperty = "startId")
  public void handleStart(MySagaStartEvent startEvent) {
    SagaLifecycle.associateWith("endId", startEvent.getStartId());
    LOGGER.info("Saga started with startId {}", startEvent.getStartId());
  }

  @SagaEventHandler(associationProperty = "endId")
  @EndSaga
  public void handleEnd(MySagaEndEvent endEvent) {
    LOGGER.info("Saga ended with the endId {}", endEvent.getEndId());
  }
}
