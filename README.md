# axon-kafka-example
This repo is forked from marinkobabic/axon-kafka-example. Modified to support multiple instances(different JVMs) consuming messages from Kafka topic, without using the AXON-SERVER.

Here the link to the official documentation for axon-kafka  https://docs.axonframework.org/part-iii-infrastructure-components/event-processing#apache-kafka

## Producer
Produces events which are consumed by the consumer. 

## Producer-events
Every public API represents a contract. When using message the exchanged messages are the contracts. In this example the contract is the MyEvent

## Consumer
Consumer is there to consume the published event by the producer. If the Axon events are published and consumed from Kafka topic, then there is no load balancing between the consumers running on different JVMs. Hence added a custom segment id, sequencing policy and extended the JpaTokenStore to isolate the event processors from contention on the token_entry table.

The segment ids are obtained from the zookeeper in a distributed fashion so that each JVM instance get a different segment id. JpaTokenStore is inherited and modified to return the segment id when queries on segments are executed by the TrackingEventProcessor.

Sequencing policy is implemented to return the same segment id from the zookeeper.

Therefore is the @EventHandler is needed which is automatically scanned by the Axon framework and assigned to the trackingProcessor matching the ProcessingGroup.
 ```
 axon:
   eventhandling:
     processors:
       "[MyProcessor]":
         source: kafkaMessageSource
         mode: TRACKING
```
In our example you can find the name **MyProcessor**. This is the name you can find in the @EventHandler class
```java
@Component
@ProcessingGroup("MyProcessor")
public class MyEventEventHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(MyEventEventHandler.class);

  @EventHandler
  public void handleMyEvent(MyEvent myEvent){
    LOGGER.debug("got the event {}", myEvent);
  }

}
```
For reference see the Spring Boot Autoconfiguraton https://docs.axonframework.org/part-ii-domain-logic/event-handling

###Docker
If you don't have a kafka environment running on your machine, you can use the docker-compose.yaml here. All you need to do is to navigate to this directore and execute the command
```
gradlew build docker
docker-compose up
```
