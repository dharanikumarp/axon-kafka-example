package com.mykafka.consumer.tokenstore;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class MySegmentId implements DisposableBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(MySegmentId.class);

	public static final int NUM_RETRIES = 5;
	public static final int SLEEP_TIME_MILLIS = 10000;
	private final RetryPolicy RP = new ExponentialBackoffRetry(SLEEP_TIME_MILLIS, NUM_RETRIES);

	private int segmentId = -1;

	public int getSegmentId() {
		if (segmentId == -1) {
			throw new IllegalStateException("Segment is not initialized");
		}
		return this.segmentId;
	}

	@PostConstruct
	public void fetchSegmentId() {
		LOGGER.info("MySegmentId.fetchSegmentId");
		segmentId = 0;

		try (CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", RP)) {
			client.start();
			client.blockUntilConnected();

			DistributedAtomicInteger dAI = new DistributedAtomicInteger(client, "/segment", RP);
			boolean wasSet = dAI.initialize(-1); // Value will be set only for the first time.
			if (wasSet) {
				LOGGER.info("/segment got initialized to -1");
			}

			AtomicValue<Integer> av = dAI.increment();
			if (av.succeeded()) {
				segmentId = av.postValue();
				LOGGER.info("Next segmentId after incrementing " + av.postValue());
			}
		} catch (Exception e) {
			LOGGER.error("Exception while connecting with zookeeper ", e);
		}
	}

	@PreDestroy
	public void destroy() {
		LOGGER.info("MySegmentId.destroy");

		try (CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", RP)) {
			client.start();
			client.blockUntilConnected();

			DistributedAtomicInteger dAI = new DistributedAtomicInteger(client, "/segment", RP);
			AtomicValue<Integer> av = dAI.add(0);
			if (av.succeeded()) {
				if (av.postValue() == segmentId) {
					av = dAI.compareAndSet(segmentId, segmentId - 1);
					if (av.succeeded()) {
						LOGGER.info("Decremented the segmentId to " + av.postValue());
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception while connecting with zookeeper ", e);
		}
	}

}
