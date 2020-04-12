package com.company.javakafka

import org.apache.kafka.clients.producer.Producer
import org.junit.Test

class ProducerCreatorTest extends KafkaConnectionTest {

    @Test
    void testCreateProducer() {
        testKafkaConnection()
        Producer<Long, String> producer = ProducerCreator.createProducer()
        assertNotNull(producer)
    }
}
