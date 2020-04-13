package com.company;

import com.company.database.IDataBaseConstants;
import com.company.javakafka.ConsumerCreator;
import com.company.javakafka.constants.IKafkaConstants;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.util.NifSelector;

import java.io.IOException;
import java.sql.*;

public class Main {

    private static PcapNetworkInterface getNetworkDevice() {
        PcapNetworkInterface device = null;
        try {
            device = new NifSelector().selectNetworkInterface();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return device;
    }

    private static void runConsumer() {
        Consumer<Long, String> consumer = ConsumerCreator.createConsumer();
        int noMessageFound = 0;

        while (true) {
            ConsumerRecords<Long, String> consumerRecords = consumer.poll(1000);
            // 1000 is the time in milliseconds consumer will wait if no record is found at broker.
            if (consumerRecords.count() == 0) {
                noMessageFound++;
                if (noMessageFound > IKafkaConstants.MAX_NO_MESSAGE_FOUND_COUNT)
                    // If no message found count is reached to threshold exit loop.
                    break;
                else
                    continue;
            }

            //print each record.
            consumerRecords.forEach(record -> {
                System.out.println("Record recieved: key(" + record.key()
                        + ") value(" + record.value()
                        + ") partition(" + record.partition()
                        + ") offset(" + record.offset() + ')');
            });

            // commits the offset of record to broker.
            consumer.commitAsync();
        }
        consumer.close();
    }

    public static void main(String[] args) throws SQLException {
        String filter = args.length != 0 ? args[0] : "";

        System.out.println("Filter: " + filter);
        final TrafficCounter trafficCounter = new TrafficCounter(getNetworkDevice(), filter, 5*60, 20*60);

        Thread trafficCounterThread = new Thread(() -> trafficCounter.openTraffic());
        Thread trafficPeriodThread = new Thread(() -> trafficCounter.getCurrentTrafficCount());
        Thread updateLimitsThread = new Thread(() -> trafficCounter.getLimitsEveryPeriod());

        trafficCounterThread.start();
        trafficPeriodThread.start();
        updateLimitsThread.start();

        runConsumer();
    }
}
