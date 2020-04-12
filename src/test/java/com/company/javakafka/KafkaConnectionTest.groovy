package com.company.javakafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

class KafkaConnectionTest extends Assert {

    //check if kafka server is running
    @Test
    void testKafkaConnection(){
        Properties props = ProducerCreator.getProperties()
        try (AdminClient client = AdminClient.create(props)) {
            client.listTopics(new ListTopicsOptions().timeoutMs(5000)).listings().get();
        } catch (ExecutionException ex) {
            fail("Kafka is not available, timed out after 5000 ms")
            ex.printStackTrace()
        }
    }
}
