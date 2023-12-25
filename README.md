# Kafka_Stream
Demo of some mini projects for study.

I use kafka version (kafka_2.13-3.1.0)

## Running:

1. Start kafka server
```
kafka-server-start.sh ~/kafka_2.13-3.1.0/config/kraft/server.properties
```
3. Create input topic:
```
kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 2 --topic word-count-input
```
3. Create output topic:
```
kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 2 --topic word-count-output
```
4. Launch a kafka consumer:
```
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
    --topic word-count-output \
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer 
```
5. Run StreamStarterApp.java for running Kafka Stream app.

6. Produce data to it.
```
kafka-console-producer.sh --broker-list localhost:9092 --topic word-count-input
```
7. package your application as a fat jar: mvn clean package

8. run the fat jar:
```
java -jar <file>.jar
```

## Note:

Check pom.xml for package configuration.

1. kafka-streams
2. slf4j-api
3. slf4j-log4j12

And configure src/main/resources/log4j.properties
```
log4j.rootLogger=INFO, stdout

log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%p %m (%c:%L) %n
```
