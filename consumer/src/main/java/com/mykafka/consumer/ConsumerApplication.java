package com.mykafka.consumer;

import java.util.concurrent.CountDownLatch;

import org.axonframework.extensions.kafka.autoconfig.KafkaAutoConfiguration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication(exclude = KafkaAutoConfiguration.class)
@SpringBootApplication
public class ConsumerApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ConsumerApplication.class, args);
	}

	private CountDownLatch latch = new CountDownLatch(1);

	@Override
	public void run(String... args) throws Exception {
		latch.await();
	}
}

