package com.mykafka.consumer.tokenstore;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.eventhandling.tokenstore.UnableToClaimTokenException;
import org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore;
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry;
import org.axonframework.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom token store to support load balancing with Kafka consumers.
 * 
 * @author dharanikumarp
 *
 */
public class MyTokenStore extends JpaTokenStore {

	private EntityManagerProvider entityManagerProvider;
	private Serializer serializer;
	private int segmentId = 0;
	private Set<String> processorNames = new HashSet<>();

	private static final Logger LOGGER = LoggerFactory.getLogger(MyTokenStore.class);

	public MyTokenStore(Builder builder, Serializer serializer, EntityManagerProvider emp, MySegmentId mySegmentId) {
		super(builder);
		LOGGER.info("MyTokenStore.MyTokenStore");
		this.entityManagerProvider = emp;
		this.serializer = serializer;
		segmentId = mySegmentId.getSegmentId();
	}

	@Override
	public void initializeTokenSegments(String processorName, int segmentCount, TrackingToken initialToken)
			throws UnableToClaimTokenException {
		LOGGER.info("MyTokenStore.initializeTokenSegments " + segmentCount + ", segmentId " + segmentId
				+ ", initialToken " + initialToken + ", serializer " + serializer);
		EntityManager entityManager = entityManagerProvider.getEntityManager();
		if (fetchSegments(processorName).length > 0) {
			throw new UnableToClaimTokenException("Could not initialize segments. Some segments were already present.");
		}

		for (int segment = segmentId; segment < (segmentId + segmentCount); segment++) {
			TokenEntry token = new TokenEntry(processorName, segment, initialToken, serializer);
			entityManager.persist(token);
		}
		entityManager.flush();

		processorNames.add(processorName);
	}

	@Override
	public int[] fetchSegments(String processorName) {
		EntityManager entityManager = entityManagerProvider.getEntityManager();

		final List<Integer> resultList = entityManager
				.createQuery("SELECT te.segment FROM TokenEntry te "
						+ "WHERE te.processorName = :processorName AND te.segment = :segment ORDER BY te.segment ASC",
						Integer.class)
				.setParameter("processorName", processorName).setParameter("segment", segmentId).getResultList();

		return resultList.stream().mapToInt(i -> i).toArray();
	}
}
