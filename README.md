# Kafka_Stream
Demo of some mini projects for study.

The project is Bank Balance project. It will generate one producer that will producer messages with JSON format, then one Kafka Stream will process the message.

Another Kafka Consumer is launched to show the message.  

## Configuration for generating new project (Favorite Color) maven:
```
groupId=com.github.quangtn.kafka.streams
artifactId=kafka_bankbalance_project
version=1.0-SNAPSHOT
```

## Steps:
1. Start kafka server.

2. Create input topic.

3. Create log-compacted topic.

4. launch a Kaka consumer

```
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
    --topic bank-balance-exactly-once \
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

5. Run BankTransactionProducer.java && BankBalanceExactlyOnceApp.java
