package open.it.com.petit.Popup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.LinkedHashMap;

import open.it.com.petit.Connection.ConnectionController;
import open.it.com.petit.Connection.HttpHandler;
import open.it.com.petit.Util.Util;
import open.it.com.petit.Mqtt.BaseMqtt;
import open.it.com.petit.R;

/**
 * Created by user on 2017-10-30.
 */

public class PetitFeederMasterChangePopup extends BaseMqtt implements View.OnClickListener {
    private final static String TAG = PetitFeederMasterChangePopup.class.getSimpleName();

    private EditText pwEdit;
    private Button cancleBtn;
    private Button confirmBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.petit_feeder_master);

        pwEdit = (EditText) findViewById(R.id.petit_feeder_master_pw);
        cancleBtn = (Button) findViewById(R.id.petit_feeder_master_cancle);
        confirmBtn = (Button) findViewById(R.id.petit_feeder_master_confirm);

        cancleBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);

        pubTopic = "$open-it/pet-it/update";

        mqttConnect(null, pubTopic, Util.getDate());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.petit_feeder_master_cancle:
                finish();
                break;
            case R.id.petit_feeder_master_confirm:
                passwordConfirm();
                break;
        }
    }

    private void passwordConfirm() {
        String pw = pwEdit.getText().toString();
        String masterPW = getIntent().getStringExtra("PW");
        String guid = getIntent().getStringExtra("GUID");

        Log.d(TAG, masterPW);
        if (pw.equals("")) {
            Toast.makeText(this, "빈칸 없이 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pw.equals(masterPW)) {
            Toast.makeText(this, "비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        ConnectionController conn = new ConnectionController(getApplicationContext(), handler);
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("GUID", guid);
        map.put("P_NUM", Util.getPhoneNum(this));
        String php = "feeder_master_change.php";
        conn.setMethod("POST").setHash(map).setUrl(php);
        Thread thread = new Thread(conn);
        thread.start();
    }

    final HttpHandler handler = new HttpHandler(this) {
        @Override
        public void onHttpOK() {
            super.onHttpOK();
            Toast.makeText(PetitFeederMasterChangePopup.this, "권한 변경하였습니다.", Toast.LENGTH_SHORT).show();
            publish(pubTopic, "MASTER:" + getIntent().getStringExtra("GUID"));
            finish();
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
