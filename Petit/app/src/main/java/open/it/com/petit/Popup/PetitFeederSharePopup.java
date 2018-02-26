package open.it.com.petit.Popup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.LinkedHashMap;
import java.util.List;

import open.it.com.petit.Connection.ConnectionController;
import open.it.com.petit.Connection.HttpHandler;
import open.it.com.petit.Util.Util;
import open.it.com.petit.Model.Feeder;
import open.it.com.petit.Mqtt.BaseMqtt;
import open.it.com.petit.R;

/**
 * Created by user on 2017-07-12.
 */

public class PetitFeederSharePopup extends BaseMqtt implements View.OnClickListener{
    private final static String TAG = PetitFeederSharePopup.class.getSimpleName();

    private EditText phoneNum;
    private Button registBtn;
    private Button masterBtn;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.petit_feeder_share_popup);

        pubTopic = "$open-it/pet-it/update";
        //subTopic = "$open-it/pet-it/update";

        phoneNum = (EditText) findViewById(R.id.share_phone_num);
        registBtn = (Button) findViewById(R.id.share_add);
        masterBtn = (Button) findViewById(R.id.share_master_btn);
        registBtn.setOnClickListener(this);
        masterBtn.setOnClickListener(this);

        if (getIntent().getIntExtra("MS", -1) == 0) {
            phoneNum.setEnabled(false);
            registBtn.setEnabled(false);
        }

        mqttConnect(null, pubTopic, Util.getDate());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share_add:
                if (phoneNum.getText().toString().equals("")) {
                    Toast.makeText(this, "빈칸 없이 입력해주세요.", Toast.LENGTH_SHORT).show();
                    break;
                }

                if (phoneNum.getText().toString().length() != 11) {
                    Toast.makeText(this, "유효한 핸드폰번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    break;
                }
                shareFeeder();
                break;
            case R.id.share_master_btn:
                Intent intent = new Intent(PetitFeederSharePopup.this, PetitFeederMasterChangePopup.class);
                intent.putExtra("GUID", getIntent().getStringExtra("GUID"));
                intent.putExtra("PW", getIntent().getStringExtra("PW"));
                startActivity(intent);
                finish();
                break;
        }
    }

    private void shareFeeder() {
        if (Util.getPhoneNum(this).equals(phoneNum.getText().toString())) {
            Toast.makeText(this, "자신의 핸드폰은 등록 불가합니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        ConnectionController conn = new ConnectionController(getApplicationContext(), handler);
        String url = getString(R.string.db_host) +
                "get_feeder_pnum.php" +
                "?P_NUM=" + phoneNum.getText().toString() +
                "&GUID=" + getIntent().getStringExtra("GUID");
        conn.setMethod("GET").setUrl(url).setClass(Feeder[].class);
        Thread thread1 = new Thread(conn);
        thread1.start();
        try {
            thread1.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Feeder> list = conn.getResult();

        for (Feeder f : list) {
            Log.d(TAG, f.getP_NUM());
        }

        if (list == null)
            return;
        if (list.size() != 0) {
            Toast.makeText(this, "등록된 핸드폰이 존재합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("P_NUM", phoneNum.getText().toString());
        map.put("GUID", getIntent().getStringExtra("GUID"));
        map.put("PW", getIntent().getStringExtra("PW"));
        map.put("MS", 0);
        String php = "feeder_insert.php";
        conn.setMethod("POST").setHash(map).setUrl(php);
        Thread thread2 = new Thread(conn);
        thread2.start();
    }

    /*@Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        final String identifier = message.toString().split(":")[0];
        final String msg = message.toString().split(":")[1];
        if (identifier.equals("MASTER") && getIntent().getStringExtra("GUID").equals(msg)) {
            phoneNum.setEnabled(false);
            registBtn.setEnabled(false);
        }
    }*/

    final HttpHandler handler = new HttpHandler(this) {
        @Override
        public void onHttpOK() {
            super.onHttpOK();
            Toast.makeText(PetitFeederSharePopup.this, "등록 하였습니다.", Toast.LENGTH_SHORT).show();
            publish(pubTopic, "SHARE:" + phoneNum.getText().toString());
            finish();
        }

        @Override
        public void onHttpError() {
            super.onHttpError();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mqttDisConnect();
    }
}
