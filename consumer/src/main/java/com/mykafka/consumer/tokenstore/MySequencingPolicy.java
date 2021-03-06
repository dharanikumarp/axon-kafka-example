package com.mykafka.consumer.tokenstore;

import org.axonframework.eventhandling.async.SequencingPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("MySequencingPolicy")
public class MySequencingPolicy<T> implements SequencingPolicy<T> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MySegmentId.class);
	
	@Autowired
	private MySegmentId mySegmentId;
	
	@Override
	public Object getSequenceIdentifierFor(T event) {
		LOGGER.info("MySequencingPolicy.getSequenceIdentifierFor event ", event);
		return mySegmentId.getSegmentId();
	}

}
