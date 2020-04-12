package com.company;

import com.company.database.DataBase;
import com.company.database.DBChangeListener;
import com.company.javakafka.ProducerCreator;
import com.company.javakafka.constants.IKafkaConstants;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.pcap4j.core.*;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class TrafficCounter extends Thread{

    private PcapNetworkInterface device;
    private PcapHandle handle;
    private String filter;

    private static int traffic_count_period = 5 * 60; //5 min default
    private static int limits_update_period = 20 * 60; //20 min default
    private int maxLimit, minLimit;
    private int bytes_count = 0;
    private DBChangeListener listener;

    TrafficCounter(PcapNetworkInterface device) throws SQLException {

        this.device = device;
        assignLimits();
        System.out.println("Initial limits: min = " + minLimit + " max = " + maxLimit);
        listener = new DBChangeListener();
        Callable<Void> func = () -> {
            assignLimits();
            return null;
        };
        listener.createListener(func);
    }

    TrafficCounter(PcapNetworkInterface device, int traffic_count_period_sec, int limits_update_period_sec) throws SQLException {

        this.device = device;
        traffic_count_period = traffic_count_period_sec;
        limits_update_period = limits_update_period_sec;

        assignLimits();
        listener = new DBChangeListener();
        Callable<Void> func = () -> {
            assignLimits();
            return null;
        };
        listener.createListener(func);
    }

    TrafficCounter(PcapNetworkInterface device, String filter, int traffic_count_period_sec, int limits_update_period_sec) throws SQLException {

        this.device = device;
        traffic_count_period = traffic_count_period_sec;
        limits_update_period = limits_update_period_sec;
        this.filter = filter;

        assignLimits();
        listener = new DBChangeListener();
        Callable<Void> func = () -> {
            assignLimits();
            return null;
        };
        listener.createListener(func);
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public PcapHandle getHandle() {
        return handle;
    }

    public void setHandle(PcapHandle filter) {
        this.handle = handle;
    }

    private void assignLimits(){
        maxLimit = DataBase.getLimit("max");
        minLimit = DataBase.getLimit("min");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        System.out.println(formatter.format(date) + " Limits have been updated: min = " + minLimit + " max = " + maxLimit);
    }

    public void getLimitsEveryPeriod() {

        TimerTask getLimitsEveryPeriod = new TimerTask() {
            @Override
            public void run() {
                assignLimits();
            }
        };
        Timer limitsTimer = new Timer("limitsTimer");
        limitsTimer.schedule(getLimitsEveryPeriod, limits_update_period * 1000, limits_update_period * 1000);
    }

    public void openTraffic(){

        // Open the device and get a handle
        int snapshotLength = 65536; // in bytes
        int readTimeout = 50; // in milliseconds

        try {
            handle = device.openLive(snapshotLength, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, readTimeout);

            if (filter.length() != 0) {
                handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);
            }

            // Create a listener that defines what to do with the received packets
            PacketListener listener = packet -> bytes_count += packet.length();

            //Tell the handle to loop using the listener we created
            try {
                handle.loop(-1, listener);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            handle.close();
        } catch (PcapNativeException | NotOpenException e) {
            e.printStackTrace();
        }

    }

    public void getCurrentTrafficCount(){

        TimerTask countTraffic = new TimerTask(){
            @Override
            public void run() {
                SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
                Date date = new Date(System.currentTimeMillis());
                System.out.println(formatter.format(date) + ' ' + bytes_count + " bytes received");
                if (bytes_count > maxLimit)
                    sendAlert("Traffic is above maximum limit!");
                if (bytes_count < minLimit)
                    sendAlert("Traffic is lower than minimum limit!");
                bytes_count = 0;
            }
        };
        Timer trafficTimer = new Timer("trafficTimer");
        trafficTimer.schedule(countTraffic, traffic_count_period * 1000, traffic_count_period * 1000);

    }

    public static void sendAlert(String text){

        Producer<Long, String> producer = ProducerCreator.createProducer();
        ProducerRecord<Long, String> record = new ProducerRecord<>(IKafkaConstants.TOPIC_NAME, text);
        try {
            RecordMetadata metadata = producer.send(record).get();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
            Date date = new Date(System.currentTimeMillis());
            System.out.println(formatter.format(date) +" Alert sent to partition " + metadata.partition() + " with offset " + metadata.offset());
        } catch (ExecutionException | InterruptedException e) {
            System.out.println("Error in sending record");
            System.out.println(e);
        }
    }

}
