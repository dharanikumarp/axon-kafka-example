package com.mykafka.producer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

import com.mykafka.consumer.events.MyEvent;
import com.mykafka.consumer.events.MySagaEndEvent;
import com.mykafka.consumer.events.MySagaStartEvent;

@SpringBootApplication(exclude = KafkaAutoConfiguration.class)
public class ProducerApplication implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProducerApplication.class);
	@Autowired
	private Sender sender;

	public static void main(String[] args) {
		SpringApplication.run(ProducerApplication.class, args);

	}

	@Override
	public void run(String... args) throws Exception {
		String processName = ManagementFactory.getRuntimeMXBean().getName();
		String pid = processName.substring(0, processName.indexOf("@"));
		LOGGER.info("pid {}", pid);
		TimeUnit.SECONDS.sleep(1);

		LOGGER.info("Start sending");
		for (int i = 1; i <= 20; i++) {
			TimeUnit.SECONDS.sleep(2);
			sender.send(new MyEvent(String.format("Hi there %d %s", i, new Date())));
		}
		LOGGER.info("Send completed");
	}
}
