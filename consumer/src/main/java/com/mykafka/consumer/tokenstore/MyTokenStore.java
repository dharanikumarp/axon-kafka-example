package com.mykafka.consumer.tokenstore;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.eventhandling.tokenstore.UnableToClaimTokenException;
import org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore;
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry;
import org.axonframework.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

public class MyTokenStore extends JpaTokenStore implements DisposableBean {

	public static final int NUM_RETRIES = 5;
	public static final int SLEEP_TIME_MILLIS = 10000;
	private final RetryPolicy RP = new ExponentialBackoffRetry(SLEEP_TIME_MILLIS, NUM_RETRIES);

	private Builder builder;
	private EntityManagerProvider entityManagerProvider;
	private Serializer serializer;
	private int startingSegment = 0;
	private Set<String> processorNames = new HashSet<>();

	private static final Logger LOGGER = LoggerFactory.getLogger(MyTokenStore.class);

	public MyTokenStore(Builder builder, Serializer serializer, EntityManagerProvider emp) {
		super(builder);
		LOGGER.info("MyTokenStore.MyTokenStore");
		this.builder = builder;
		this.entityManagerProvider = emp;
		this.serializer = serializer;
		startingSegment = getDistributedAtomicInt();
	}

	@Override
	public void initializeTokenSegments(String processorName, int segmentCount, TrackingToken initialToken)
			throws UnableToClaimTokenException {
		LOGGER.info("MyTokenStore.initializeTokenSegments " + segmentCount + ", startingSegment " + this.startingSegment
				+ ", initialToken " + initialToken + ", serializer " + serializer);
		EntityManager entityManager = this.entityManagerProvider.getEntityManager();
		if (fetchSegments(processorName).length > 0) {
			throw new UnableToClaimTokenException("Could not initialize segments. Some segments were already present.");
		}

		for (int segment = startingSegment; segment < (startingSegment + segmentCount); segment++) {
			TokenEntry token = new TokenEntry(processorName, segment, initialToken, this.serializer);
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
				.setParameter("processorName", processorName).setParameter("segment", this.startingSegment)
				.getResultList();

		return resultList.stream().mapToInt(i -> i).toArray();
	}

	private int getDistributedAtomicInt() {
		LOGGER.info("MyTokenStore.getDistributedAtomicInt");
		int segmentId = 0;

//		if(1 < 10) {
//			return segmentId;
//		}

		try {
			CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", RP);
			client.start();
			client.blockUntilConnected();

			DistributedAtomicInteger dAI = new DistributedAtomicInteger(client, "/segment", RP);
			AtomicValue<Integer> av = dAI.increment();
			if (av.succeeded()) {
				segmentId = av.postValue();
				LOGGER.info("segmentId after incrementing " + segmentId);
			}

			client.close();

		} catch (Exception e) {
			LOGGER.error("Exception while connecting with zookeeper");
		}

		return segmentId;
	}

	/**
	 * Return the segmentId for another client to use
	 */
	@Override
	@PreDestroy
	public void destroy() {
		LOGGER.info("MyTokenStore.destroy");
		// tm.executeInTransaction(() -> processorNames.stream().forEach(pn ->
		// super.releaseClaim(pn, this.startingSegment)));

		try {

			CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", RP);
			client.start();
			client.blockUntilConnected();

			DistributedAtomicInteger dAI = new DistributedAtomicInteger(client, "/segment", RP);
			AtomicValue<Integer> av = dAI.get();
			if (av.preValue() == this.startingSegment) {
				av = dAI.decrement();
				if (av.succeeded()) {
					LOGGER.info("segmentId after decrementing " + av.postValue());
				}
			}
			
			client.close();

		} catch (Exception e) {
			LOGGER.error("Exception while connecting with zookeeper");
		}
	}

}
