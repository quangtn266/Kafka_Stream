package com.github.quangtn.kafka.streams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Properties;

public class EventEnricherApp {

    public static void main(String[] args) {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "user-event-enricher-app");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();

        // we get a global table out of Kafka. This table will be replicated on each Kafka Streams application
        // the key of our globalKtable is the user ID
        GlobalKTable<String, String> userGlobalTable = builder.globalTable("user-table");

        // we get a stream of user purchases.
        KStream<String, String> userPurchases = builder.stream("user-purchases");

        // we want to enrich that stream
        KStream<String, String> userPurchaseEnrichedJoin =
                userPurchases.join(userGlobalTable,
                (key, value) -> value, // map from the (key, value) of this stream to the key of the GlobalKtable
                (userPurchase, userInfo) -> "Purchase-innerjoin=" + userPurchase + ",UserInfo=[" + userInfo +"]"
        );

        userPurchaseEnrichedJoin.to("user-purchases-enriched-inner-join");

        // we want to enrich that stream using Left Join.
        KStream<String, String> userPurchasesEnrichedLeftJoin =
                userPurchases.leftJoin(userGlobalTable,
                        (key, value) -> key, // map from the (key, value) of this stream to the key of the GlobalKTable.
                        (userPurchase, userInfo) -> {
                            // as this is a left join, userInfo can be null
                            if(userInfo != null) {
                                return "Purchase-leftjoin=" + userPurchase + ",UserInfo=[" + userInfo + "]";
                            } else {
                                return "Purchase=" + userPurchase + ",UserInfo=null";
                                }
                            }
                        );

        userPurchasesEnrichedLeftJoin.to("user-purchases-enriched-left-join", Produced.with(Serdes.String(), Serdes.String()));

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.cleanUp();
        streams.start();

        // print the topology
        System.out.println(streams.toString());

        // shutdown hook to correctly close the streams application.
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
