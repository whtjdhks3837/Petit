package open.it.com.petit.Mqtt;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import open.it.com.petit.Util.Util;
import open.it.com.petit.R;

/**
 * Created by user on 2017-11-03.
 */

public class BaseMqtt extends AppCompatActivity
        implements Mqtt, MqttCallbackExtended, IMqttActionListener {
    private final static String TAG = BaseMqtt.class.getSimpleName();

    protected String subTopic;
    protected String pubTopic;

    protected MqttAndroidClient mqttAndroidClientPetit;
    protected String mqttURL;
    protected String clientId;

    protected String GUID;

    @Override
    public void mqttConnect(String subTopic, String pubTopic, String clientId) {
        mqttURL = getResources().getString(R.string.mqtt_host);

        mqttAndroidClientPetit = new MqttAndroidClient(this, mqttURL, clientId);
        mqttAndroidClientPetit.setCallback(this);

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        final int seconds = 10;
        connectOptions.setConnectionTimeout(seconds);
        connectOptions.setAutomaticReconnect(true); // reconnect true
        connectOptions.setCleanSession(true); // false일 시 예전 메세지까지 다 받음.

        try {
            mqttAndroidClientPetit.connect(connectOptions, null, this);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void mqttConnect(String subTopic, String pubTopic) {
        clientId = Util.getPhoneNum(this);
        mqttConnect(subTopic, pubTopic, clientId);
    }

    @Override
    public void mqttDisConnect() {
        Log.d(TAG, "MqttDisConnect");
        if (mqttAndroidClientPetit != null) {
            try {
                mqttAndroidClientPetit.unregisterResources();
                mqttAndroidClientPetit.close();
                mqttAndroidClientPetit.disconnect(null, this);
                mqttAndroidClientPetit = null;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void publish(String pubTopic, String msg) {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(msg.getBytes());
            message.setQos(0);
            mqttAndroidClientPetit.publish(pubTopic, message);
            Log.d(TAG, "send topic : " + pubTopic + " / msg : " + msg);
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void publish(String pubTopic, byte[] bytes) {

    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {

    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        Log.e(TAG, "mqttConnect to petit failure with exception : " + exception.getMessage());
        Toast.makeText(this, "서버연결에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
        //mqttDisConnect();
        exception.printStackTrace();
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        Log.d(TAG, "--------------connectComplete---------------");
        if (reconnect) {
            Log.d(TAG, "reconnect is true . So subscribing to topic again");
        } else {
            Log.d(TAG, "Connected to MQTT server");
        }
        Log.d(TAG, "--------------connectComplete---------------");
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "--------------connectionLost---------------");
        if (cause != null) {
            Log.e(TAG, "Connection to MQtt is lost due to " + cause.getMessage());
        }
        Log.d(TAG, "--------------connectionLost---------------");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d(TAG, "--------------messageArrived---------------");
        Log.d(TAG, "Message arrived : " + message + " from topic : " + topic);
        Log.d(TAG, "--------------messageArrived---------------");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            if (token != null && token.getMessage() != null) {
                Log.d(TAG, "Message : " + token.getMessage().toString() + " delivered");
            }
        } catch (MqttException ex) {
            Log.d(TAG, "Message : Not exist topic");
            ex.printStackTrace();
        }
    }
}
