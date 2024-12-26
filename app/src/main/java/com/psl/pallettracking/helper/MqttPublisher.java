package com.psl.pallettracking.helper;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttActionListener;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;

public class MqttPublisher {
    private MqttAsyncClient mqttClient;
    private String clientId = "androidapp";
//    private String tcpBrokerUrl = "tcp://broker.emqx.io:1883";
    private String tcpBrokerUrl = "tcp://192.168.0.172:1883";

    public void connectAndPublish(String topic, String message) {
        try {
            MqttConnectionOptions connOpts = new MqttConnectionOptions();
            connOpts.setConnectionTimeout(30);
            connOpts.setKeepAliveInterval(60);
             connOpts.setUserName("admin");
             connOpts.setPassword("569JdchjPb5uYVEB".getBytes());
            // connOpts.setCleanStart(false);  // MQTT 5 clean start equivalent
            // connOpts.setSessionExpiryInterval(0L);  // Retain session indefinitely

            mqttClient = new MqttAsyncClient(tcpBrokerUrl, clientId, new MemoryPersistence());
            mqttClient.connect(connOpts, null, new MqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("Connected to MQTT broker: " + tcpBrokerUrl);
                    publishMessage(topic, message);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("Error connecting to MQTT broker: " + exception.getMessage());
                    exception.printStackTrace();
                    System.out.println("Error details: " + asyncActionToken.getException().getMessage());
                }
            });
        } catch (MqttException e) {
            System.out.println("Error connecting to MQTT broker: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void publishMessage(String topic, String message) {
        try {
//            MqttProperties properties = new MqttProperties();
//            properties.setMessageExpiryInterval(60L);

            byte[] messagePayload = message.getBytes();
            mqttClient.publish(topic, messagePayload, 0, false);
            System.out.println("Message published successfully!");
        } catch (MqttException e) {
            System.out.println("Error publishing message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}