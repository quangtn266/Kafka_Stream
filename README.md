# Kafka_Stream
Demo of some mini projects for study.

The project counts the occurrences of colors [red, green, blue] with format (key, color).

## Configuration for generating new project (Favorite Color) maven:
```
groupId=com.github.quangtn.kafka.streams
artifactId=kafka_favoritecolor_project
version=1.0-SNAPSHOT
```
## Steps:
1. Start kafka server:
```
kafka-server-start.sh ~/kafka_2.13-3.1.0/config/kraft/server.properties
```
2. Create input topic:
```
kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 2 --topic favourite-colour-input
```
3. Create output topic:
```
kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 2 --topic favourite-colour-output
```
4. Create intermediary log compacted topic
```
kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic user-keys-and-colours --config cleanup.policy=compact
```
5. Launch a kafka consumer:

```
kafka-console-consumer.sh --bootstrap-server localhost:9092
--topic word-count-output
--from-beginning
--formatter kafka.tools.DefaultMessageFormatter
--property print.key=true
--property print.value=true
--property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
--property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
```

6. Run FavoriteColorApp.java for running Kafka Stream app.

7. Produce data
```
kafka-console-producer.sh --broker-list localhost:9092 --topic favourite-colour-input
```
## Running.

In "produce" terminal, you can run like:

Quang,red

Mikey,green

In "consumer" terminal, you will see the result like <key>  <number>
