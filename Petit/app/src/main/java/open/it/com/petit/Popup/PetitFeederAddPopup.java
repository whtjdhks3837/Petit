package open.it.com.petit.Popup;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import open.it.com.petit.R;

/**
 * Created by user on 2017-07-12.
 */

public class PetitFeederAddPopup extends Activity {
    private Button cancel;
    private Button confirm;
    private String gps;

    @Override
    protected void onCreate(Bundle savaInstanceState) {
        super.onCreate(savaInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.petit_feeder_add_popup);

        cancel = (Button) findViewById(R.id.btn_feeder_add_cancel);
        confirm = (Button) findViewById(R.id.btn_feeder_add_confirm);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gps = android.provider.Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                if (!(gps.matches(".*gps.*") && gps.matches(".*network.*"))) {
                    startActivity(new Intent(PetitFeederAddPopup.this, PetitFeederConnectGPS.class));
                    finish();
                }else {
                    startActivity(new Intent(PetitFeederAddPopup.this, PetitFeederWifiSearch.class));
                    finish();
                }
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
