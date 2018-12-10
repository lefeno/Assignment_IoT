package com.example.bill.assignmentiot;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;

public class MqttControl implements MqttCallback {
    private static final String username = "uaijweiy";
    private static final String password = "Fsz3BK1eUXev";
    private static final String serveruri = "tcp://m10.cloudmqtt.com:13170";
    private String clientId = "raspberry";
    private String toPic = "command";
    private static final String TAG = MqttControl.class.getSimpleName();
    private static final int QOs = 1;
    private MqttClient client;
    private boolean MqttRead = true;
//    public static List<ParamPot> listAutoPot;

    public MqttControl(String topic, String id, boolean read) throws MqttException {
        clientId = id;
        toPic = topic;
        MqttRead = read;
        subcribeToTopic();
    }

    public void subcribeToTopic() throws MqttException {
        client = new MqttClient(serveruri, clientId, new MemoryPersistence());
        client.connect(connectOptionchoice());
        if (MqttRead) {
            client.subscribe(toPic);
        }
        client.setCallback(this);
        Log.d(TAG, "nice connected ~");

    }

    private static MqttConnectOptions connectOptionchoice() {
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setUserName(username);
        connectOptions.setPassword(password.toCharArray());
        connectOptions.setCleanSession(false);
        connectOptions.setAutomaticReconnect(true);
        connectOptions.setKeepAliveInterval(30);
        return connectOptions;
    }

    public void close() throws MqttException {
        client.disconnect();
        client.close();
        Log.d(TAG, "client  disconnected ! ");
    }

    public void sendmessage(String data, String topic) throws MqttException {
        if (!client.isConnected()) {
            subcribeToTopic();
        }
        MqttMessage message = new MqttMessage(data.getBytes());
        message.setQos(QOs);
        client.publish(topic, message);
    }

    @Override
    public void connectionLost(Throwable cause) {
        try {
            Log.d(TAG, "Reconnecting ~ ................(-______-)#......");
            client.connect(MqttControl.connectOptionchoice());
            Log.d(TAG, "Connected success ! ");

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    //Receive code to control arduino
    @Override
    public void messageArrived(String topic, MqttMessage message) throws IOException, MqttException {
        String payload = new String(message.getPayload());
        Log.d(TAG, "message received: " + payload);
        // Check payload
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

}



