package open.it.com.petit.Popup;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.LinkedHashMap;

import open.it.com.petit.Connection.ConnectionController;
import open.it.com.petit.Connection.HttpHandler;
import open.it.com.petit.Util.Util;
import open.it.com.petit.Mqtt.BaseMqtt;
import open.it.com.petit.R;

/**
 * Created by user on 2017-07-31.
 */

public class PetitFeederDeletePopup extends BaseMqtt {
    private final static String TAG = PetitFeederDeletePopup.class.getSimpleName();

    private Button cancel;
    private Button confirm;
    private String GUID;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.petit_feeder_delete_popup);

        cancel = (Button) findViewById(R.id.btn_feeder_delete_cancel);
        confirm = (Button) findViewById(R.id.btn_feeder_delete_confirm);
        GUID = getIntent().getStringExtra("GUID");

        pubTopic = "$open-it/pet-it/update";

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                map.put("GUID", GUID);
                map.put("P_NUM", Util.getPhoneNum(getApplicationContext()));
                String php = "feeder_delete.php";
                ConnectionController conn = new ConnectionController(getApplicationContext(), handler);
                conn.setMethod("POST").setHash(map).setUrl(php);
                Thread thread = new Thread(conn);
                thread.start();
            }
        });

        mqttConnect(null, pubTopic, Util.getDate());
    }

    final Handler handler = new HttpHandler() {
        @Override
        public void onHttpOK() {
            super.onHttpOK();
            Toast.makeText(PetitFeederDeletePopup.this, "삭제되었습니다", Toast.LENGTH_SHORT).show();
            publish(pubTopic, "DELETE:" + GUID);
            finish();
        }

        @Override
        public void onHttpError() {
            super.onHttpError();
        }
    };

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        Log.d(TAG, "mqtt petit mqttConnect successfull. Now Subscribing to topic..." + subTopic);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mqttDisConnect();
    }
}
