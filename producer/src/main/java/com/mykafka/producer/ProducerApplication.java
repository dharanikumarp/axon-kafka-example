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

	public static final boolean USE_CONSOLE_INPUT = false;

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
		TimeUnit.SECONDS.sleep(5);

		LOGGER.info("Start sending");
		final long processId = Long.parseLong(pid);
		sender.send(new MySagaStartEvent(processId));

		if (USE_CONSOLE_INPUT) {

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String line = null;
			int i = 0;
			LOGGER.info("Waiting for input");
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.equals("send")) {
					sender.send(new MyEvent(String.format("Hi there %d %s", i++, new Date())));
				} else {
					break;
				}
				LOGGER.info("Waiting for input");
			}

			br.close();

		} else {
			for (int i = 0; i < 20; i++) {
				TimeUnit.SECONDS.sleep(3);
				sender.send(new MyEvent(String.format("Hi there %d %s", i, new Date())));
			}
		}

		sender.send(new MySagaEndEvent(processId));
		LOGGER.info("Send completed");
	}
}
