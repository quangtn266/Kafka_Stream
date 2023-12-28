package com.github.quangtn.kafka.streams;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class EventEnricherProducer {
    public static void main(String [] args) throws ExecutionException, InterruptedException, IOException {
        Properties properties = new Properties();

        // Kafka bootstrap server
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Producer acks
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, "3");
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "1");

        // Leverage idempotent producer from Kafka version 2.x
        // ensure we don't push duplicates
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        Producer<String, String> producer = new KafkaProducer<>(properties);

        // FYI - we do .get() to ensure the writes to the topics are sequential, for the sake of the teaching exercise

        // 1 - Creating a new user, then it will receive some data to Kafka.
        System.out.println("\nExample 1 - new user\n");
        producer.send(userRecord("quang", "First=Quang, Last=TranNgoc, Email=quangtrandn933@gmail.com")).get();
        producer.send(purchaseRecord("quang", "Grape and Apple (1)")).get();

        Thread.sleep(1000);

        // 2 - Receiving user purchase, but it doesn't exist in Kafka.
        System.out.println("\nExample 2 - non existing user\n");
        producer.send(purchaseRecord("win", "Flourist shopping (2)")).get();

        Thread.sleep(1000);

        // 3 - Updating user "quang", and send a new transaction.
        System.out.println("\nExample 3 - update to user\n");
        producer.send(userRecord("quang", "First=Quangsx, Last=TranNgoc, Email=quangtrandn93@gmail.com")).get();
        producer.send(purchaseRecord("quang", "Strawberry (3)")).get();

        Thread.sleep(1000);

        // 4 - Sending a user purchase for quang, but it exists in Kafka later.
        System.out.println("\nExample 4- non existing user then user\n");
        producer.send(purchaseRecord("mikey", "Computer (4)")).get();
        producer.send(userRecord("mikey","First=mikey, Last=TrannNggoc, Github=quangtran")).get();
        producer.send(purchaseRecord("mikey", "Books (4)")).get();
        producer.send(userRecord("mikey", null)).get(); // delete for clean-up

        Thread.sleep(1000);

        // 5- Creating a user, but it gets deleted before any purchase comes through.
        System.out.println("\nExample 5- user then delete then data\n");
        producer.send(userRecord("mka", "First=Mikey")).get();
        producer.send(userRecord("mka", null)).get(); // that's delete record
        producer.send(purchaseRecord("mka", "Apache Kafka Series (5)")).get();

        Thread.sleep(1000);

        // 5- Creating a user, but it gets deleted before any purchase comes through.
        System.out.println("\nExample 6- user then delete then data\n");
        producer.send(userRecord("quang", "First=Quang, Last=TranNgoc, Email=quangtrandn933@gmail.com")).get();
        // producer.send(userRecord("quang", null)).get(); // that's delete record
        producer.send(purchaseRecord("quang", "Grape and Apple (6)")).get();

        Thread.sleep(1000);

        System.out.println("End of demo");
        producer.close();
        }

        private static ProducerRecord<String, String> userRecord(String key, String value) {
            return new ProducerRecord<>("user-table", key, value);
        }

        private static ProducerRecord<String, String> purchaseRecord(String key, String value) {
            return new ProducerRecord<>("user-purchases", key, value);
        }

}
