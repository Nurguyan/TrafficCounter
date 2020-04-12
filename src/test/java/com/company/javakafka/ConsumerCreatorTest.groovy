package com.company.javakafka

import org.apache.kafka.clients.consumer.Consumer
import org.junit.Test


class ConsumerCreatorTest extends KafkaConnectionTest {

    @Test
    void testCreateConsumer() {
        testKafkaConnection()
        Consumer<Long, String> consumer =  ConsumerCreator.createConsumer()
        assertNotNull(consumer)
    }
}
