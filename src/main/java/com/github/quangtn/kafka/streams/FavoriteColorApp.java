package com.github.quangtn.kafka.streams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Stream;

public class FavoriteColorApp {

    public static void main(String [] args) {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "favorite-color-java");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // we disable the cache to demonstrate all the "steps" involved in the transformation- not
        // not recommend in prod.

        config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");

        StreamsBuilder builder = new StreamsBuilder();

        // Step1: We create the topic of users & keys to colors
        KStream<String, String> textLines = builder.stream("favourite-colour-input");

        KStream<String, String> usersAndColours = textLines
                // 1 - we ensure that a comma is here as we will split on it.
                .filter((key, value) -> value.contains(","))
                // 2 - we select a key that will be the user id (lower case for safety)
                .selectKey((key, value) -> value.split(",")[0].toLowerCase())
                // 3 - we get the color from the values (lower case for a safety)
                .mapValues(value -> value.split(",")[1].toLowerCase())
                // 4 - we filter undesired colors (could be a data sanitization step
                .filter((user, colour) -> Arrays.asList("green", "blue", "red").contains(colour));

        usersAndColours.to("user-keys-and-colours");

        // step2 - we read that topic as a KTable so that updates are read correctly
        KTable<String, String> userAndColoursTable = builder.table("user-keys-and-colours");

        // step3 - we count the occurrences of colours
        KTable<String, Long> favoriteColours  = userAndColoursTable
                // 5 - we group by color within the KTable
                .groupBy((user, colour) -> new KeyValue<>(colour, colour))
                .count(Named.as("CountsByColours"));

        // 6 - we output the results to a kafka Topic - don't forget the serializers
        favoriteColours.toStream().to("favourite-colour-output", Produced.with(Serdes.String(), Serdes.Long()));

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        // only do this in dev - no in prod
        streams.cleanUp();
        streams.start();

        // print the topology
        System.out.println(streams);

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
