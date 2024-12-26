package com.psl.pallettracking.helper;

import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;

import java.nio.charset.StandardCharsets;

public class MqttSubscriber {
    public void subscribe(String topic, MqttCallback callbackHandler) {
        String broker = "tcp://192.168.0.172:1883";
        String deviceId = "PDA-1";
        int subQos = 1;

        try {
            MqttClient client = new MqttClient(broker, deviceId, new MemoryPersistence());
            MqttConnectionOptions options = new MqttConnectionOptions();
            options.setUserName("admin");
            options.setPassword("569JdchjPb5uYVEB".getBytes(StandardCharsets.UTF_8));
            options.setCleanStart(false);
            options.setSessionExpiryInterval(0L);

            client.setCallback(callbackHandler);

            client.connect(options);
            client.subscribe(topic, subQos);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}