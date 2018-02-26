package open.it.com.petit.Activity;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import open.it.com.petit.Controller.PetitWZAudio;
import open.it.com.petit.R;

/**
 * Created by user on 2017-09-25.
 */

public class PetitMediaActivity extends AppCompatActivity
        implements IVLCVout.Callback, MqttCallbackExtended, IMqttActionListener, View.OnClickListener {
    public final static String TAG = "PetitMediaActivity";

    private String url;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;

    private LibVLC libVLC;
    private MediaPlayer mediaPlayer;

    private int videoWidth;
    private int videoHeight;
    private int videoVisibleHeight = 0;
    private int videoVisibleWidth = 0;
    private int videoSarNum = 0;
    private int videoSarDen = 0;

    private PetitWZAudio petitWZAudio;
    private boolean mute = false;

    private MediaPlayer.EventListener playerListener = new MyPlayerListener(this);
    private ImageButton audioStart;
    private ImageButton mediaClose;
    private ImageButton feedOnce;
    private ImageButton flash;
    private ImageButton capture;
    private ImageButton record;
    private ImageButton playback;

    private MqttAndroidClient mqttAndroidClient;
    private String mqttUrl;
    private String clientId = "petit";
    private String GUID;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.petit_media_activity_tmp);

        Log.d(TAG, "onCreate");
        url = "rtsp://211.38.86.93:1935/live/ywb";
        surfaceView = (SurfaceView) findViewById(R.id.sv_petit_media);
        holder = surfaceView.getHolder();

        mqttUrl = getResources().getString(R.string.mqtt_host);
        GUID = getIntent().getStringExtra("GUID");

        audioStart = (ImageButton) findViewById(R.id.audio_start);
        mediaClose = (ImageButton) findViewById(R.id.media_close);
        feedOnce = (ImageButton) findViewById(R.id.feed_once);
        flash = (ImageButton) findViewById(R.id.flash);
        capture = (ImageButton) findViewById(R.id.capture);
        record = (ImageButton) findViewById(R.id.record);
        playback = (ImageButton) findViewById(R.id.playback_audio);

        mediaClose.setOnClickListener(this);
        feedOnce.setOnClickListener(this);
        flash.setOnClickListener(this);
        capture.setOnClickListener(this);
        record.setOnClickListener(this);
        playback.setOnClickListener(this);

        audioStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                if(action == MotionEvent.ACTION_DOWN) {
                    //petitWZAudio.setMute(false);
                } else if(action == MotionEvent.ACTION_UP) {
                    //petitWZAudio.setMute(false);
                }
                return true;

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.media_close:
                finish();
                break;
            case R.id.feed_once:
                publish("feeder1Phone Number");
                break;
            case R.id.flash:

                break;
            case R.id.capture:

                break;
            case R.id.record:

                break;
            case R.id.playback_audio:
                publish("voice");
                break;
        }
    }

    /******************************************************Life cycle********************************************************************/
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume excute audio");
        mqttConnect();
        createPlayer(url);
       /* petitWZAudio = new PetitWZAudio(this);
        if (petitWZAudio!= null)
            petitWZAudio.executeAudio();*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause endBroadCast");
        //releasePlayer();
       /* if (petitWZAudio!= null && petitWZAudio.isIdle())
            petitWZAudio.endBroadcast(true);*/

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop endBroadCast");
        releasePlayer();
        publish("stop video");
        mqttDisConnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy endBroadCast");
        mqttDisConnect();
    }

    /******************************************************VLC 영역*******************************************************************/

    /* 화면 전환 callback */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSize(videoWidth,videoHeight);
    }

    /* 새로운 레이아웃을 요청하면 콜백이 호출됨. */
    @Override
    public void onNewLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        Log.d(TAG, "onNewVideoLayout");
        Log.d(TAG, "width ? " + width);
        Log.d(TAG, "height ? " + height);
        Log.d(TAG, "vWidth ? " + visibleWidth);
        Log.d(TAG, "vHeight ? " + visibleHeight);
        if (width * height == 0)
            return;

        this.videoHeight = height;
        this.videoWidth = width;
        setSize(visibleWidth, visibleHeight);
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }

    @Override
    public void onHardwareAccelerationError(IVLCVout vlcVout) {
        Log.d(TAG, "onHardwareAccelerationError");
        releasePlayer();
    }

    private void setSize(int width, int height) {
        Log.d(TAG, "setSize");
        videoWidth = width;
        videoHeight = height;

        Log.d(TAG, "width : " + videoWidth + "/ height : " + videoHeight);
        if (videoWidth * videoHeight <= 1)
            return ;

        if (holder == null || surfaceView == null)
            return ;

        int w = getWindow().getDecorView().getWidth();
        int h = getWindow().getDecorView().getHeight();
        Log.d(TAG, "w : " + w + " // h : " + h);
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        Log.d(TAG, "isPortrait ? " + isPortrait);
        if (w > h && isPortrait || w < h && !isPortrait) {
            Log.d(TAG, "not Portrait");
            int tmp = w;
            w = h;
            h = tmp;
        }

        float videoAR = (float) videoWidth / (float) videoHeight;
        float screenAR = (float) w / (float) h;

        Log.d(TAG, "videoAR ? " + videoAR);
        Log.d(TAG, "screenAR ? " + screenAR);
        if (screenAR < videoAR) {
            h = (int) (w / videoAR);
        } else {
            w = (int) (h * videoAR);
        }

        holder.setFixedSize(videoWidth, videoHeight);
        ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        lp.width = w;
        lp.height = h;
        Log.d(TAG, "lp.width : " + lp.width);
        Log.d(TAG, "lp.height : " + lp.height);
        surfaceView.setLayoutParams(lp);
        surfaceView.invalidate();
    }

    private void createPlayer(String media) {
        releasePlayer();

        try {
            if (media.length() > 0) {
                Toast toast = Toast.makeText(this, media, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
                        0);
                toast.show();
            }

            ArrayList<String> options = new ArrayList<>();
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            libVLC = new LibVLC(this, options);

            //holder.setKeepScreenOn(false); // 에러 발생

            mediaPlayer = new MediaPlayer(libVLC);
            mediaPlayer.setEventListener(playerListener);

            final IVLCVout vout = mediaPlayer.getVLCVout();
            vout.setVideoView(surfaceView);
            vout.addCallback(this);
            vout.attachViews();

            Media m = new Media(libVLC, Uri.parse(media));
            m.setHWDecoderEnabled(true, true);
            m.addOption("network-caching=1000");
            m.addOption(":clock-jitter=0");
            m.addOption(":clock-synchro=0");

            mediaPlayer.setMedia(m);
            mediaPlayer.play();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error in creating player!", Toast
                    .LENGTH_LONG).show();
        }
    }

    private void releasePlayer() {
        if (libVLC == null)
            return ;
        mediaPlayer.stop();
        final IVLCVout vout = mediaPlayer.getVLCVout();
        vout.removeCallback(PetitMediaActivity.this);
        vout.detachViews();
        holder = null;
        libVLC.release();
        libVLC = null;

        videoWidth = 0;
        videoHeight = 0;
    }

    /********************************************MQTT 영역*******************************************************/

    private void mqttConnect() {
        if (mqttAndroidClient == null) {
            mqttAndroidClient = new MqttAndroidClient(this, mqttUrl, clientId);
            mqttAndroidClient.setCallback(this);

            MqttConnectOptions connectOptions = new MqttConnectOptions();
            final int seconds = 10;
            connectOptions.setConnectionTimeout(seconds);
            connectOptions.setAutomaticReconnect(false);
            connectOptions.setCleanSession(false);

            try {
                mqttAndroidClient.connect(connectOptions, null, this);
            } catch (MqttException e) {
                Log.d(TAG, "MqttException");
                e.printStackTrace();
            }
        }
    }

    private void mqttDisConnect() {
        if (mqttAndroidClient != null) {
            if (mqttAndroidClient.isConnected()) {
                try {
                    mqttAndroidClient.unregisterResources();
                    mqttAndroidClient.close();
                    mqttAndroidClient.disconnect();
                    mqttAndroidClient = null;
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void publish(String msg) {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(msg.getBytes());
            message.setQos(0);
            if (mqttAndroidClient != null)
                mqttAndroidClient.publish("$open-it/pet-it/" + GUID + "/order", message);
            Log.d(TAG, "Published to topic : $open-it/pet-it/" + GUID + "/order");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        publish("request video");
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        mqttDisConnect();
        Toast.makeText(this, "서버연결에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "mqttConnect failure with exception : " + exception.getMessage());
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
        if (message != null) {
            //getMqttRequestMessage(message);
        } else {
            //mqttMessageEmpty();
        }
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

    private static class MyPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<PetitMediaActivity> owner;

        //액티비티 변수를 받아오기 위하여 지정
        public MyPlayerListener(PetitMediaActivity owner) {
            this.owner = new WeakReference<PetitMediaActivity>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            PetitMediaActivity player = owner.get();

            switch (event.type) {
                case MediaPlayer.Event.EndReached :
                    Log.d(TAG, "MediaPlayerEndReached");
                    player.releasePlayer();
                    break;
                case MediaPlayer.Event.Playing :
                case MediaPlayer.Event.Paused :
                case MediaPlayer.Event.Stopped :
                default :
                    break;
            }
        }
    }
}
