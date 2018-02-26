package open.it.com.petit.Popup;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import java.util.ArrayList;

import open.it.com.petit.Manager.PetitWifiManager;
import open.it.com.petit.R;

/**
 * Created by user on 2017-07-19.
 */

public class PetitFeederWifiSearch extends Activity {
    private ArrayList<ScanResult> scanList;
    private ImageView img;
    private PetitWifiManager wifiManager;

    @Override
    protected void onCreate(Bundle saveIntanceState) {
        super.onCreate(saveIntanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.petit_feeder_wifi_search);

        img = (ImageView) findViewById(R.id.pb_wifi_search);
        Glide.with(this).load(R.drawable.spin_loader).into(img);

        startScanWifi();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                searchWifi();
            }
        }
    };

    private void startScanWifi() {
        wifiManager = PetitWifiManager.getInstance(this);
        wifiManager.getWifiManager().startScan();

        IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiver, intentFilter);
    }

    private void searchWifi() {
        unregisterReceiver(receiver);
        scanList = (ArrayList) wifiManager.getWifiManager().getScanResults();
        if (scanList.isEmpty())
            scanList = null;

        Intent intent = new Intent(this, PetitFeederWifiPopup.class);
        intent.putParcelableArrayListExtra("scanList", scanList);
        startActivity(intent);
        finish();
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
}
