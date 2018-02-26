package open.it.com.petit.Popup;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.UUID;

import open.it.com.petit.Connection.ConnectionController;
import open.it.com.petit.Connection.HttpHandler;
import open.it.com.petit.Util.Util;
import open.it.com.petit.R;

/**
 * Created by user on 2017-07-13.
 */

public class PetitFeederAddQrPopup extends Activity {
    private static final String TAG = "PetitFeederAddQrPopup";
    private ImageView qrCode;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savaInstanceState) {
        super.onCreate(savaInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.petit_feeder_qr_popup);

        qrCode = (ImageView) findViewById(R.id.QRcode);
        intent = getIntent();

        String reg_info = intent.getStringExtra("Ssid")
                + "/" + intent.getStringExtra("WifiPw")
                + "/" + intent.getStringExtra("MasterPw")
                + "/" + Util.getPhoneNum(this)
                + "/" + FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "token : " + FirebaseInstanceId.getInstance().getToken().getBytes().length);

        createQRCode(reg_info);

        findViewById(R.id.regTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                map.put("P_NUM", Util.getPhoneNum(getApplicationContext()));
                map.put("GUID", getDate());
                map.put("PW", intent.getStringExtra("MasterPw"));
                map.put("MS", 1);
                map.put("TOKEN", FirebaseInstanceId.getInstance().getToken());
                String php = "feeder_insert.php";
                ConnectionController conn = new ConnectionController(getApplicationContext(), handler);
                conn.setMethod("POST").setHash(map).setUrl(php);
                Thread thread = new Thread(conn);
                thread.start();
            }
        });
    }

    private String getDate() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        return simpleDateFormat.format(date);
    }

    private void createQRCode(String info) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        try {
            Bitmap bitmap = toBitmap(qrCodeWriter.encode(info, BarcodeFormat.QR_CODE, 250, 250));
            qrCode.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private Bitmap toBitmap(BitMatrix matrix) {
        int height = matrix.getHeight();
        int width = matrix.getWidth();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0 ; x < width ; x ++) {
            for (int y = 0 ; y < height ; y ++) {
                bmp.setPixel(x, y, matrix.get(x,y) ? Color.BLACK : Color.WHITE);
            }
        }

        return bmp;
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
        finish();
    }

    HttpHandler handler = new HttpHandler() {
        @Override
        public void onHttpOK() {
            finish();
        }

        @Override
        public void onHttpError() {
            super.onHttpError();
        }
    };
}
