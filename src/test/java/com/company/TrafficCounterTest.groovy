package com.company

import com.company.javakafka.ConsumerCreator
import com.company.javakafka.KafkaConnectionTest
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecords

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.pcap4j.core.PcapHandle
import org.pcap4j.core.PcapNetworkInterface
import org.pcap4j.core.PcapStat
import org.pcap4j.core.Pcaps

class TrafficCounterTest extends KafkaConnectionTest {

    private TrafficCounter trafficCounter
    private PcapHandle handle

    @Before
    void setUpTrafficCounter(){
        List<PcapNetworkInterface> nifs = Pcaps.findAllDevs()
        trafficCounter = new TrafficCounter(nifs.get(0), 5, 5)
        handle = trafficCounter.getHandle()
    }

    @After
    void releaseTrafficHandle(){
        if (handle != null)
            handle.close()
    }

    @Test
    void testOpenTraffic() {

        if (handle != null) {
            trafficCounter.openTraffic()
            PcapStat ps = handle.getStats()
            assertNotNull(ps)
        }
    }

    @Test
    void testSendAlert(){

        //check if kafka server is running
        testKafkaConnection()

        String expected_message = "test message", actual_message
        Consumer<Long, String> consumer = ConsumerCreator.createConsumer()
        int noMessageFound = 0
        //send message
        TrafficCounter.sendAlert(expected_message)

        //run consumer to receiver message
        while (true) {
            ConsumerRecords<Long, String> consumerRecords = consumer.poll(1000)
            // 1000 is the time in milliseconds consumer will wait if no record is found at broker.
            if (consumerRecords.count() == 0) {
                noMessageFound++
                if (noMessageFound >= 1)
                    break
            }

            consumerRecords.forEach(record -> {
                actual_message = record.value()
            })
            // commits the offset of record to broker.
            consumer.commitAsync()
        }
        consumer.close()

        //check equality
        Assert.assertEquals(expected_message, actual_message)
    }

}
