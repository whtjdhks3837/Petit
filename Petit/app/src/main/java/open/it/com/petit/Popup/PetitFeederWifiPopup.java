package open.it.com.petit.Popup;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;

import open.it.com.petit.Adapter.FeederWifiListAdapter;
import open.it.com.petit.R;

/**
 * Created by user on 2017-07-13.
 */

public class PetitFeederWifiPopup extends Activity{
    public static final String TAG = "PetitFeederWifiPopup";

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<ScanResult> scanList;

    private Button cancel;
    private Button reSearch;
    private ScrollView wifiExist;
    private LinearLayout wifiNotExist;

    @Override
    protected void onCreate(Bundle savaInstanceState) {
        super.onCreate(savaInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.petit_feeder_wifi_list_popup);

        cancel = (Button) findViewById(R.id.btn_wifi_list_cancel);
        reSearch = (Button) findViewById(R.id.btn_wifi_list_research);
        wifiExist = (ScrollView) findViewById(R.id.wifi_list_exist);
        wifiNotExist = (LinearLayout) findViewById(R.id.wifi_list_not_exist);

        scanList = getIntent().getParcelableArrayListExtra("scanList");

        if (scanList == null) {
            wifiExist.setVisibility(View.INVISIBLE);
            wifiNotExist.setVisibility(View.VISIBLE);
        } else {
            wifiExist.setVisibility(View.VISIBLE);
            wifiNotExist.setVisibility(View.INVISIBLE);
            recyclerView = (RecyclerView) findViewById(R.id.rv_wifi);
            recyclerView.setHasFixedSize(true);

            layoutManager = new LinearLayoutManager(PetitFeederWifiPopup.this);
            recyclerView.setLayoutManager(layoutManager);

            adapter = new FeederWifiListAdapter(PetitFeederWifiPopup.this, scanList);
            recyclerView.setAdapter(adapter);
        }
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        reSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PetitFeederWifiPopup.this, PetitFeederWifiSearch.class));
                finish();
            }
        });
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
