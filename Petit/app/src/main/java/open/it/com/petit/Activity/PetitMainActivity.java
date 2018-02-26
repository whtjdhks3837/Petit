package open.it.com.petit.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import open.it.com.petit.Adapter.FeederListAdapter;
import open.it.com.petit.Connection.ConnectCode;
import open.it.com.petit.Connection.ConnectionController;
import open.it.com.petit.Connection.HttpHandler;
import open.it.com.petit.Popup.PetitFeederConnectGPS;
import open.it.com.petit.Util.Util;
import open.it.com.petit.Model.Feeder;
import open.it.com.petit.Mqtt.BaseMqtt;
import open.it.com.petit.R;

public class PetitMainActivity extends BaseMqtt
        implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    public final static String TAG = PetitMainActivity.class.getSimpleName();

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    private ArrayList<Feeder> petList;
    private ImageButton add;

    public static ProgressBar pb;
    private ConnectionController conn;

    private boolean isFinish;

    @Override
    protected void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.petit_main);

        petList = new ArrayList<>();
        pb = (ProgressBar) findViewById(R.id.main_pb);

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer);
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.main_navi);
        navigationView.setNavigationItemSelectedListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.feeder_list);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new FeederListAdapter(petList, this);
        recyclerView.setAdapter(adapter);

        add = (ImageButton) findViewById(R.id.btn_feeder_add);
        add.setOnClickListener(this);

        subTopic = "$open-it/pet-it/update";
        mqttConnect(subTopic, null);
        conn = new ConnectionController(getApplicationContext(), handler);

        pb.setVisibility(View.VISIBLE);

    }

    private void getFeedList() {
        if (pb.getVisibility() == View.VISIBLE)
           pb.setVisibility(View.INVISIBLE);

        String url = getString(R.string.db_host) + "get_feeder_info.php"
               +"?P_NUM=" +  Util.getPhoneNum(getApplicationContext());
        conn.setMethod("GET").setClass(Feeder[].class).setUrl(url);

        Thread thread = new Thread(conn);
        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Feeder> list = conn.getResult();

        Message msg = handler.obtainMessage(ConnectCode.GET_FEEDER_INFO);
        msg.obj = list;
        msg.what = ConnectCode.GET_FEEDER_INFO;
        handler.sendMessage(msg);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        getFeedList();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        isFinish = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mqttDisConnect();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_feeder_add:
                startActivity(new Intent(PetitMainActivity.this, PetitFeederConnectGPS.class));
                //startActivity(new Intent(PetitMainActivity.this, PetitFeederMasterPopup.class));
                break;
        }
    }

    final HttpHandler handler = new HttpHandler(this) {
        @Override
        public void onHttpOK() {
            super.onHttpOK();
        }

        @Override
        public void onGetFeederInfo() {
            List list = (List) msg.obj;
            petList.clear();
            petList.addAll(list);
            adapter.notifyDataSetChanged();

            compareToken();
        }
    };

    private void compareToken() {
        if (petList.size() > 0) {
            for (int i = 0 ; i < petList.size() ; i ++) {
                Log.d(TAG, "my Token : " + petList.get(i).getToken());
                if (!petList.get(i).getToken().equals(FirebaseInstanceId.getInstance().getToken())) {
                    Log.d(TAG, "compareToken2");
                    String php = "regist_token.php";
                    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                    map.put("TOKEN", FirebaseInstanceId.getInstance().getToken());
                    map.put("P_NUM", Util.getPhoneNum(this));
                    conn.setMethod("POST").setHash(map).setUrl(php);
                    Thread thread = new Thread(conn);
                    thread.start();

                    try {
                        thread.join();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    petList.get(i).setToken(FirebaseInstanceId.getInstance().getToken());
                    Log.d(TAG, petList.get(i).getToken());
                }
            }
        }
    }

    /* 동시에 여러대가 작동할 때 어떻게 작동할지 테스트 요망 */
    private synchronized void getMessage(String msg) {
        final String identifier = msg.split(":")[0];
        final String message = msg.split(":")[1];
        Log.d(TAG, "identifier : " + identifier);
        Log.d(TAG, "message : " + message);

        switch (identifier) {
            case "DELETE":
                Log.d(TAG, "DELETE");
                /* 전부 삭제를 할지 의논 필요.*/
                /*for (int i = 0 ; i < petList.size() ; i ++) {
                    if (petList.get(i).getGUID().equals(message)) {
                        pb.setVisibility(View.VISIBLE);
                        petList.remove(i);
                        adapter.notifyDataSetChanged();
                        pb.setVisibility(View.INVISIBLE);
                        break;
                    }
                }*/
                break;
            case "SHARE":
                Log.d(TAG, "SHARE");
                if (Util.getPhoneNum(this).equals(message)) {
                    pb.setVisibility(View.VISIBLE);
                    sendRegistrationToServer();
                    getFeedList();
                    return ;
                }
                break;
            case "MASTER":
                Log.d(TAG, "MASTER");
                for (int i = 0 ; i < petList.size() ; i ++) {
                    if (petList.get(i).getGUID().equals(message)) {
                        pb.setVisibility(View.VISIBLE);
                        getFeedList();
                        break;
                    }
                }
                break;
        }
    }

    public void sendRegistrationToServer() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("P_NUM", Util.getPhoneNum(this));
        map.put("Token", FirebaseInstanceId.getInstance().getToken());
        String php = "regist_token.php";
        conn.setMethod("POST").setHash(map).setUrl(php);
        Thread thread = new Thread(conn);
        thread.start();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        getMessage(message.toString());
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        Log.d(TAG, "mqtt petit mqttConnect successfull. Now Subscribing to topic..." + subTopic);
        try {
            mqttAndroidClientPetit.subscribe(subTopic, 0, null);
        } catch (MqttException e) {
            Log.d(TAG, "topic subscription failed for topic : " + subTopic);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawers();
        switch (item.getItemId()) {
            case R.id.naviitem_setting:
                startActivity(new Intent(this, SystemSettingActivity.class));
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (!isFinish) {
            isFinish = true;
            Toast.makeText(this, "뒤로가기 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
            return ;
        }
        mqttDisConnect();
        finish();
    }
}
