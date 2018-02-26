package open.it.com.petit.Popup;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import open.it.com.petit.Handler.WifiConnectHandler;
import open.it.com.petit.Manager.PetitWifiManager;
import open.it.com.petit.R;

/**
 * Created by user on 2017-07-14.
 */

public class PetitFeederAddWifiPwPopup extends Activity {
    private static final String TAG = PetitFeederAddWifiPwPopup.class.getSimpleName();

    private Button cancel;
    private Button confirm;
    private EditText ed_PW;
    private TextView tv_Wifi;
    private ProgressBar pb;

    private Intent intent;
    private String ssid;
    private android.net.wifi.ScanResult ap;
    private boolean wifiLoop;

    private PetitWifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savaInstanceState) {
        super.onCreate(savaInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.petit_feeder_wifi_pw);

        intent = getIntent();

        if (intent.hasExtra("ap")) {
            ap = (android.net.wifi.ScanResult) intent.getExtras().get("ap");
            ssid = ap.SSID;
        } else {
            Toast.makeText(this, "와이파이 오류", Toast.LENGTH_SHORT).show();
            finish();
        }

        cancel = (Button) findViewById(R.id.btn_wifi_pw_cancel);
        confirm = (Button) findViewById(R.id.btn_wifi_pw_confirm);
        ed_PW = (EditText) findViewById(R.id.ed_wifi_pw);
        tv_Wifi = (TextView) findViewById(R.id.tv_pw_wifi_name);
        pb = (ProgressBar) findViewById(R.id.pb_wifi_wait);

        tv_Wifi.setText(ssid);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PetitFeederAddWifiPwPopup.this, PetitFeederWifiSearch.class));
                finish();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pb.setVisibility(View.VISIBLE);
                connWifi();
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        /*intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);*/
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);

        registerReceiver(receiver, intentFilter);
    }

    private void connWifi() {
        if (ed_PW.getText().toString().equals("")) {
            pb.setVisibility(View.INVISIBLE);
            Toast.makeText(PetitFeederAddWifiPwPopup.this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
            return ;
        }

        //WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);
        /*wifiManager = PetitWifiManager.getInstance(this);
        WifiConnectHandler wHandler = new WifiConnectHandler(ap, wifiManager.getWifiManager(), ed_PW.getText().toString(), this);

        *//* 연결이 제대로 되지 않았을 시 -1을 리턴 *//*
        if (wHandler.wifiConnect() == -1) {
            pb.setVisibility(View.INVISIBLE);
            Toast.makeText(PetitFeederAddWifiPwPopup.this, "비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show();
            return ;
        }*/

        Toast.makeText(this, "완료완료완료완료완료!!!!!!!!!!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(PetitFeederAddWifiPwPopup.this, PetitFeederMasterPopup.class);
        intent.putExtra("Ssid", ap.SSID);
        intent.putExtra("WifiPw", ed_PW.getText().toString());
        startActivity(intent);
        finish();
        /*wifiLoop = true;

        while (wifiLoop) {
            if (isWifiConnected(wifiManager.getWifiManager())) {
                Intent intent = new Intent(PetitFeederAddWifiPwPopup.this, PetitFeederMasterPopup.class);
                intent.putExtra("Ssid", wHandler.getSSID());
                intent.putExtra("WifiPw", ed_PW.getText().toString());
                startActivity(intent);
                finish();
                break;
            }
        }*/
    }

    /* 현재 와이파이가 연결될 때를 확인. */
    private boolean isWifiConnected(WifiManager wifiManager) {
        if (!wifiManager.isWifiEnabled()) {
            wifiLoop = false;
            return false;
        }

        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = conMgr.getActiveNetworkInfo();
        return info != null && info.getType() == ConnectivityManager.TYPE_WIFI && info.getState() == NetworkInfo.State.CONNECTED;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        return ;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");
            String action = intent.getAction();
            Log.d(TAG, "action : " + action);


            if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
                Log.d(TAG, "SUPPLICANT_STATE_CHANGED_ACTION");
                int supl_error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                Log.d(TAG, "supl_error : " + supl_error);
                if (supl_error == WifiManager.ERROR_AUTHENTICATING) {
                    Log.d(TAG, "Authentication Error!!!");
                }
            }
        }
    };
}
