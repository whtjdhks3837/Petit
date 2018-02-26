package open.it.com.petit.Popup;

import android.app.Activity;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import open.it.com.petit.R;

/**
 * Created by user on 2017-07-13.
 */

public class PetitFeederConnectGPS extends Activity {
    private Button cancel;
    private Button confirm;
    private String gps;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.petit_feeder_connect_gps);

        cancel = (Button) findViewById(R.id.btn_gps_cancel);
        confirm = (Button) findViewById(R.id.btn_gps_confirm);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGpsEnabled) {
            startActivity(new Intent(this, PetitFeederWifiSearch.class));
            finish();
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(intent, 100);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == 0) {
                gps = android.provider.Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                if (!(gps.matches(".*gps.*") && gps.matches(".*network.*"))) {
                    Toast.makeText(this, "위치서비스를 켜주세요.", Toast.LENGTH_SHORT).show();;
                }else {
                    startActivity(new Intent(this, PetitFeederWifiSearch.class));
                    finish();
                }
            }
        }
    }

}
