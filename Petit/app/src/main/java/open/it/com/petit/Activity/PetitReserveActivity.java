package open.it.com.petit.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tsengvn.typekit.TypekitContextWrapper;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.Arrays;

import open.it.com.petit.Adapter.FeederReserveTimeAdapter;
import open.it.com.petit.Handler.ReserveDBHandler;
import open.it.com.petit.Model.Time;
import open.it.com.petit.Model.Week;
import open.it.com.petit.Mqtt.BaseMqtt;
import open.it.com.petit.R;

/**
 * Created by user on 2017-05-23.
 */

public class PetitReserveActivity extends BaseMqtt
        implements MqttCallbackExtended, IMqttActionListener {
    String tmp = null;
    public static final String TAG = PetitReserveActivity.class.getSimpleName();
    private static final String dbFileName = "petit_reserve.db";

    private static final String timeTableName = "TB_MACHINE_TIME";
    private static final String weekTableName = "TB_MACHINE_WEEK";
    private static final int db_version = 1;

    private ReserveListHandler renewHandler;
    private ReserveDBHandler openHelper;

    private ArrayList<Time> timeDatas;
    private ArrayList<Week> weekDatas;
    private TextView petName;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Button dayBtn[] = new Button[8];

    private Button amBtn;
    private Button pmBtn;

    private LinearLayout daySave;
    private LinearLayout timeSave;

    private Spinner spn_times;
    private Spinner spn_provisions;

    private ArrayAdapter reserveTimeAdapter;
    private ArrayAdapter reserveProvisionAdapter;

    private boolean FLAG_WEEK[] = new boolean[8];
    private boolean FLAG_INIT_WEEK[] = new boolean[8];
    private boolean FLAG_CURRENT_SELECT_TIME = false;
    private boolean FLAG_CHANGE_WEEK;

    private byte reserveByteArray[] = new byte[48];
    private final String spin_time[]
            = {"시간", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00",
            "07:00", "08:00", "09:00", "10:00", "11:00", "12:00"};
    private final String spin_provisions[]
            = {"급식량", "01", "02", "03", "04", "05"};

    @Override
    public void onCreate(Bundle savaInstanceState) {
        super.onCreate(savaInstanceState);
        setContentView(R.layout.petit_feeder_reserve_activity);

        GUID = getIntent().getStringExtra("GUID");
        subTopic = "$open-it/pet-it/" + GUID + "/status";
        pubTopic = "$open-it/pet-it/" + GUID + "/order";

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_pfm_reserve_list);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new FeederReserveTimeAdapter(timeDatas, this, renewHandler);
        mRecyclerView.setAdapter(mAdapter);

        // List 갱신을 위한 핸들러
        renewHandler = new ReserveListHandler();
        // DB 연결
        openHelper = new ReserveDBHandler(this, dbFileName, null, db_version, renewHandler);

        initUI();

    }

    private void initUI() {
        dayBtn[0] = (Button) findViewById(R.id.everyday_btn);
        dayBtn[1] = (Button) findViewById(R.id.sunday_btn);
        dayBtn[2] = (Button) findViewById(R.id.monday_btn);
        dayBtn[3] = (Button) findViewById(R.id.tuesday_btn);
        dayBtn[4] = (Button) findViewById(R.id.wednesday_btn);
        dayBtn[5] = (Button) findViewById(R.id.thursday_btn);
        dayBtn[6] = (Button) findViewById(R.id.friday_btn);
        dayBtn[7] = (Button) findViewById(R.id.saturday_btn);

        amBtn = (Button) findViewById(R.id.am_btn);
        pmBtn = (Button) findViewById(R.id.pm_btn);
        amBtn.setTextColor(getResources().getColor(R.color.colorWhite));

        daySave = (LinearLayout) findViewById(R.id.reserve_day_save);
        timeSave = (LinearLayout) findViewById(R.id.reserve_time_add);

        spn_times = (Spinner) findViewById(R.id.spn_pfm_reserver_times);
        spn_provisions = (Spinner) findViewById(R.id.spn_pfm_reserver_provisions);

        reserveTimeAdapter = new ArrayAdapter(this, R.layout.petit_reserve_spinner, spin_time);
        reserveTimeAdapter.setDropDownViewResource(R.layout.petit_reserve_spinner_dropdown);
        reserveProvisionAdapter = new ArrayAdapter(this, R.layout.petit_reserve_spinner, spin_provisions);
        reserveProvisionAdapter.setDropDownViewResource(R.layout.petit_reserve_spinner_dropdown);

        spn_times.setAdapter(reserveTimeAdapter);
        spn_provisions.setAdapter(reserveProvisionAdapter);

        petName = (TextView) findViewById(R.id.tv_pfm_reserve_pet_name);
        petName.setText(getIntent().getStringExtra("petname"));

        // Button Listener
        BtnOnClickListener onClickListener = new BtnOnClickListener();

        for (int i = 0 ; i < 8 ; i ++) {
            dayBtn[i].setOnClickListener(onClickListener);
            dayBtn[i].setTextColor(getResources().getColor(R.color.colorDarkGray));
        }

        amBtn.setOnClickListener(onClickListener);
        pmBtn.setOnClickListener(onClickListener);

        daySave.setOnClickListener(onClickListener);
        timeSave.setOnClickListener(onClickListener);
    }

    private void getWeekDatas() {
        FLAG_CHANGE_WEEK = false;
        weekDatas = openHelper.selectWeek(weekTableName);

        for ( int i = 0 ; i < 8 ; i ++)
            FLAG_INIT_WEEK[i] = false;

        if (weekDatas != null) {
            if (weekDatas.size() != 0) {
                for (int i = 0; i < weekDatas.size(); i++) {
                    FLAG_INIT_WEEK[weekDatas.get(i).getMW_WEEK()] = true;
                    FLAG_WEEK[weekDatas.get(i).getMW_WEEK()] = true;
                    dayBtn[weekDatas.get(i).getMW_WEEK()].setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_pink));
                    dayBtn[weekDatas.get(i).getMW_WEEK()].setTextColor(getResources().getColor(R.color.colorWhite));
                }
            }
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d(TAG, "--------------messageArrived---------------");
        Log.d(TAG, "Message arrived : " + message + " from topic : " + topic);
        if (message != null) {
            getMqttRequestMessage(message);
        } else {
            mqttMessageEmpty();
        }
        Log.d(TAG, "--------------messageArrived---------------");
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        Log.d(TAG, "mqtt petit mqttConnect successfull. Now Subscribing to topic...");
        try {
            mqttAndroidClientPetit.subscribe(subTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Subscribed to topic : " + subTopic);
                    String msg = "reservation request";
                    try {
                        MqttMessage message = new MqttMessage();
                        message.setPayload(msg.getBytes());
                        message.setQos(0);
                        message.setRetained(true);
                        mqttAndroidClientPetit.publish(pubTopic, message);
                        Log.d(TAG, "Published to topic : " + pubTopic);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "topic subscription failed for topic : " + subTopic);
                }
            });
        } catch (MqttException e) {
            Log.d(TAG, "topic subscription failed for topic : " + subTopic);
        }
    }

    private void reserveArrayInit() {
        for (int i = 0 ; i < 48 ; i ++) {
            reserveByteArray[i] = (byte) 0;
        }
    }

    private void flagWeekInit(boolean b) {
        for(int i = 0; i < 8; i++)
            FLAG_WEEK[i] = b;
    }

    private void renewTimeList() {
        timeDatas = openHelper.selectTime(timeTableName);
        mAdapter = new FeederReserveTimeAdapter(timeDatas, this, renewHandler);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void dayButtonActionSet(int idx) {
        if(FLAG_WEEK[idx]) {
            dayBtn[idx].setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_gray));
            dayBtn[0].setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_gray));
            dayBtn[idx].setTextColor(getResources().getColor(R.color.colorDarkGray));
            dayBtn[0].setTextColor(getResources().getColor(R.color.colorDarkGray));
            FLAG_WEEK[idx] = false;
            FLAG_WEEK[0] = false;
        }else {
            dayBtn[idx].setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_pink));
            dayBtn[idx].setTextColor(getResources().getColor(R.color.colorWhite));
            FLAG_WEEK[idx] = true;
        }
    }

    private void changeWeekAndDaySendReserveData() {
        Log.d(TAG, "----------------changeWeekAndDaySendReserveData--------------------");
        //weektable 전부 삭제
        openHelper.deleteTableAllRows(weekTableName);

        //눌린 week day를 table에 insert
        for (int i = 0 ; i < 8 ; i ++) {
            if (FLAG_WEEK[i]) {
                openHelper.insertWeekDay("njdsd83dsnjsd8sds", i);
            }
        }
        /*
         * 새로운 week data를 가져오고
         * device에 보낼 byte array를 초기화.
         */
        getWeekDatas();
        reserveArrayInit();

        byte week_byte = 0;
        if (weekDatas != null && timeDatas != null) {
            for (Week day : weekDatas) {
                if ((int)Math.pow(2, day.getMW_WEEK() - 1) == 0)
                    continue;
                week_byte += (byte) Math.pow(2, day.getMW_WEEK() - 1);
            }
        }

        Log.d(TAG, "week : " + week_byte);
        for (int i = 0 ; i < 48 ; i += 2) {
            reserveByteArray[i] = week_byte;
        }

        if (timeDatas != null) {
            for (Time time : timeDatas) {
                reserveByteArray[(Integer.valueOf(time.getMT_TIME()) * 2) - 1] = (byte) time.getMT_AMOUNT();
            }
        } else {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }

        for (int i = 0 ; i < 48 ; i ++)
            Log.d(TAG, (i + 1) + " : " +reserveByteArray[i++] + " | " + reserveByteArray[i]);
        Log.d(TAG, "--------------changeWeekAndDaySendReserveData----------------");
    }

    private void getMqttRequestMessage(MqttMessage message) {
        Log.d(TAG, "----------------getMqttRequestMessage--------------------");
        tmp = Arrays.toString(message.getPayload());
        Log.d(TAG, "aa" + tmp);

        timeDatas = openHelper.selectTime(timeTableName);
        weekDatas = openHelper.selectWeek(weekTableName);

        byte bytes[] = message.getPayload();
        String week_binary = String.format("%8s", Integer.toBinaryString(bytes[0] & 0xFF)).replace(' ', '0');

        //Petit에 data가 있으며 내부DB에 data가 없을 때.
        if (timeDatas.isEmpty() && weekDatas.isEmpty()) {
            syncToDevice(week_binary, bytes);
        } else {
            compareDatas(bytes, week_binary);
        }
        Log.d(TAG, "----------------getMqttRequestMessage--------------------");
    }

    private void compareDatas(byte bytes[], String week_binary) {
        Log.d(TAG, "----------------compareDatas--------------------");
        //둘 다 data가 있으며 두개의 데이터가 동일한지 확인.
        boolean weekFlag = true;
        boolean timeFlag = true;
        char byteInverseArr[] = new char[7];

        // week data 저장
        for (int i = 0 ; i < 7; i ++) {
            byteInverseArr[i] = week_binary.charAt(7 - i);
            Log.d(TAG, "convert week data : " + byteInverseArr[i]+"");
        }

        char wTmp[] = new char[7];
        for (int i = 0 ; i < wTmp.length ; i ++) {
            wTmp[i] = '0';
        }

        for (int i = 0 ; i < weekDatas.size() ; i ++) {
            wTmp[weekDatas.get(i).getMW_WEEK() - 1] = '1';
        }

        for (int i = 0 ; i < wTmp.length ; i ++) {
            Log.d(TAG, "wTmp : " + wTmp[i]);
        }
        // week data 비교
        for (int i = 0 ; i < byteInverseArr.length ; i ++) {
            if (byteInverseArr[i] != wTmp[i]) {
                weekFlag = false;
                break;
            }
        }

         /*
         * byte를 시간, 량으로 분류
         * 급식량이 0일 때는 건너 뛴다.
         * byteTimeIdx ==  byte time index (1~24)
         * timeDBidx == DB row index
         * byteTimeCnt == byte time 개수
         */
        int byteTimeIdx = 0, timeDBidx = 0, byteTimeCnt = 0 ;

        for (int byteIdx = 0 ; byteIdx < 48 ; byteIdx ++) {
            Log.d(TAG, "byteCnt == " + byteIdx);
            if ((byteIdx + 1) % 2 == 0) {
                byteTimeIdx ++; // 시간index
                //0일때 건너 뜀
                if (bytes[byteIdx] != 0) {
                    byteTimeCnt ++;
                    //장치 시간과 DB시간이 안맞을 때
                    if (timeDatas.get(timeDBidx++).getMT_TIME() != byteTimeIdx) {
                        timeFlag = false;
                        break;
                    }
                }
            }
        }

        if (timeDatas.size() != byteTimeCnt)
            timeFlag = false;

        if (timeFlag && weekFlag) {
            getWeekDatas();
            renewTimeList();
            Log.d(TAG, "잘맞아여 ㅠㅠ");
        } else {
            Log.d(TAG, "안맞아여 ㅠㅠ");
            reserveDataSyncAlertdialog(week_binary,bytes);
        }
        Log.d(TAG, "----------------compareDatas--------------------");
    }

    private void syncToDevice(String week_binary, byte bytes[]) {
        Log.d(TAG, "----------------syncToDevice--------------------");
        openHelper.deleteTableAllRows(weekTableName);
        openHelper.deleteTableAllRows(timeTableName);

        Log.d(TAG, "syncToDevice");
        Log.d(TAG, tmp);
        Log.d(TAG, "--------------------------");
        for (Week w : weekDatas) {
            Log.d(TAG, w.getMW_WEEK() +"");
        }
        Log.d(TAG, "--------------------------");
        for (Time t : timeDatas) {
            Log.d(TAG, t.getMT_TIME() + " : " + t.getMT_AMOUNT());
        }
        Log.d(TAG, "--------------------------");
        int j = 0;
        for (int i = 0 ; i < 48 ; i ++) {
            if ((i + 1) % 2 == 0) {
                if(bytes[i] != 0)
                   openHelper.insertTimeData("njdsd83dsnjsd8sds", String.valueOf(j + 1), String.valueOf(bytes[i]), false);
                j++;
            }
        }

        j = 0;
        for (int i = 7 ; i >= 0 ; i --) {
            j ++;
            if (week_binary.charAt(i) == '1') {
                openHelper.insertWeekDay("njdsd83dsnjsd8sds", j);
            }
        }

        getWeekDatas();
        renewTimeList();
        Log.d(TAG, "----------------syncToDevice--------------------");
    }

    /* 펫잇에 데이터가 없을 때 */
    private void mqttMessageEmpty() {
        Log.d(TAG, "----------------petitEmpty--------------------");
        timeDatas = openHelper.selectTime(timeTableName);
        weekDatas = openHelper.selectWeek(weekTableName);

        if (!timeDatas.isEmpty()  && !weekDatas.isEmpty()) {
                /*
                DB에 있는 data Petit에 전송
                 */
        }
        Log.d(TAG, "----------------petitEmpty--------------------");
    }

    private void reserveDataSyncAlertdialog(final String week_binary, final byte bytes[]) {
        Log.d(TAG, "----------------alert--------------------");
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("데이터 동기화")
                .setMessage("어느 데이터에 동기화 하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("앱", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "앱");
                        syncToApp();
                    }
                })
                .setNegativeButton("장치", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "장치");
                        syncToDevice(week_binary, bytes);
                    }
                });
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
        Log.d(TAG, "----------------alert--------------------");
    }

    private void syncToApp() {
        Log.d(TAG, "--------------syncToApp---------------");
        byte week_byte = 0;
        if (weekDatas != null && timeDatas != null) {
            for (Week day : weekDatas) {
                if ((int)Math.pow(2, day.getMW_WEEK() - 1) == 0)
                    continue;
                week_byte += (byte) Math.pow(2, day.getMW_WEEK() - 1);
            }
        }

        Log.d(TAG, "week : " + week_byte);
        for (int i = 0 ; i < 48 ; i += 2) {
            reserveByteArray[i] = week_byte;
        }

        for (Time time : timeDatas) {
            reserveByteArray[(Integer.valueOf(time.getMT_TIME()) * 2) - 1] = (byte) time.getMT_AMOUNT();
        }

        for (int i = 0 ; i < 48 ; i ++)
            Log.d(TAG, (i + 1) + " : " +reserveByteArray[i++] + " | " + reserveByteArray[i]);
        Log.d(TAG, "------------------------------");
        publish(pubTopic, combinedByte("reservation set", reserveByteArray));
        getWeekDatas();
        renewTimeList();
        Log.d(TAG, "--------------syncToApp---------------");
    }

    private byte[] combinedByte(String msg, byte[] b) {
        byte[] bytes = msg.getBytes();
        byte[] combined = new byte[bytes.length + b.length];
        System.arraycopy(bytes, 0, combined, 0, bytes.length);
        System.arraycopy(b, 0, combined, bytes.length, b.length);
        return combined;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mqttDisConnect();
        if (openHelper != null)
            openHelper.close();
    }


    class BtnOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.everyday_btn:
                    if(FLAG_WEEK[0]) {
                        for (int i = 0; i < 8; i++) {
                            dayBtn[i].setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_gray));
                            dayBtn[i].setTextColor(getResources().getColor(R.color.colorDarkGray));
                        }
                        flagWeekInit(false);
                    }else {
                        for (int i = 0 ; i < 8 ; i ++) {
                            dayBtn[i].setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_pink));
                            dayBtn[i].setTextColor(getResources().getColor(R.color.colorWhite));
                        }
                        flagWeekInit(true);
                    }
                    break;

                case R.id.sunday_btn:
                    dayButtonActionSet(1);
                    break;

                case R.id.monday_btn:
                    dayButtonActionSet(2);
                    break;

                case R.id.tuesday_btn:
                    dayButtonActionSet(3);
                    break;

                case R.id.wednesday_btn:
                    dayButtonActionSet(4);
                    break;

                case R.id.thursday_btn:
                    dayButtonActionSet(5);
                    break;

                case R.id.friday_btn:
                    dayButtonActionSet(6);
                    break;

                case R.id.saturday_btn:
                    dayButtonActionSet(7);
                    break;

                case R.id.am_btn:
                    FLAG_CURRENT_SELECT_TIME = false;
                    amBtn.setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_yellow));
                    amBtn.setTextColor(getResources().getColor(R.color.colorWhite));
                    pmBtn.setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_gray));
                    pmBtn.setTextColor(getResources().getColor(R.color.colorDarkGray));
                    break;

                case R.id.pm_btn:
                    FLAG_CURRENT_SELECT_TIME = true;
                    amBtn.setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_gray));
                    amBtn.setTextColor(getResources().getColor(R.color.colorDarkGray));
                    pmBtn.setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_yellow));
                    pmBtn.setTextColor(getResources().getColor(R.color.colorWhite));
                    break;

                case R.id.reserve_day_save:
                    Log.d(TAG, "_____________________________________________");
                    // 변동이 있으면 falg_change_week 를 true 로 바꿔줌.
                    for (int i = 0 ; i < 8 ; i ++) {
                        Log.d(TAG, FLAG_INIT_WEEK[i] + "::" + FLAG_WEEK[i]);
                        if (FLAG_INIT_WEEK[i] != FLAG_WEEK[i]) {
                            FLAG_CHANGE_WEEK = true;
                            break;
                        }
                    }

                    if (FLAG_CHANGE_WEEK) {
                        changeWeekAndDaySendReserveData();
                        publish(pubTopic, combinedByte("reservation set", reserveByteArray));
                        Toast.makeText(PetitReserveActivity.this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PetitReserveActivity.this, "변경사항이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case R.id.reserve_time_add:
                    if (timeDatas != null) {
                        if (timeDatas.size() <= 20) {
                            String spinnerSelectTime = spn_times.getSelectedItem().toString();
                            String spinnerSelectProvision = spn_provisions.getSelectedItem().toString();

                            if (spinnerSelectTime.equals("시간")) {
                                Toast.makeText(PetitReserveActivity.this, "시간을 선택해주세요", Toast.LENGTH_SHORT).show();
                                return;
                            } else if (spinnerSelectProvision.equals("급식량")){
                                Toast.makeText(PetitReserveActivity.this, "급식량을 선택해주세요", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (FLAG_CURRENT_SELECT_TIME) {
                                //오후
                                if (spinnerSelectTime.substring(0, 1).equals("0")) {
                                    spinnerSelectTime = spinnerSelectTime.substring(1, 2);
                                } else if (spinnerSelectTime.substring(0, 1).equals("1")) {
                                    spinnerSelectTime = spinnerSelectTime.substring(0, 2);
                                }

                                spinnerSelectProvision = spinnerSelectProvision.substring(1, 2);
                                int timeTmp = Integer.valueOf(spinnerSelectTime) + 12;
                                spinnerSelectTime = String.valueOf(timeTmp);
                            } else {
                                //오전
                                if (spinnerSelectTime.substring(0, 1).equals("0")) {
                                    spinnerSelectTime = spinnerSelectTime.substring(1, 2);
                                } else if (spinnerSelectTime.substring(0, 1).equals("1")) {
                                    spinnerSelectTime = spinnerSelectTime.substring(0, 2);
                                }

                                spinnerSelectProvision = spinnerSelectProvision.substring(1, 2);
                            }

                            if (timeDatas != null) {
                                if (timeDatas.size() == 0) {
                                    openHelper.insertTimeData("njdsd83dsnjsd8sds", spinnerSelectTime, spinnerSelectProvision, true);
                                } else {
                                    for (int i = 0; i < timeDatas.size(); i++) {
                                        if (timeDatas.get(i).getMT_TIME() == Integer.valueOf(spinnerSelectTime)) {
                                            Toast.makeText(PetitReserveActivity.this, "예약이 중복됩니다.", Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                        if (i == timeDatas.size() - 1) {
                                            openHelper.insertTimeData("njdsd83dsnjsd8sds", spinnerSelectTime, spinnerSelectProvision, true);
                                            mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(PetitReserveActivity.this, "DB연결 오류입니다1.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(PetitReserveActivity.this, "최대 예약 횟수를 초과하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(PetitReserveActivity.this, "DB연결 오류입니다2.", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }
    class ReserveListHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    renewTimeList();
                    changeWeekAndDaySendReserveData();
                    publish(pubTopic, combinedByte("reservation set", reserveByteArray));
                    Toast.makeText(PetitReserveActivity.this, "예약변경 되었습니다.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }
}
