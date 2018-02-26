package open.it.com.petit.Activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

import open.it.com.petit.R;

/**
 * Created by user on 2017-05-22.
 */

public class PermissionCheck extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle saveInstanceState) {
            super.onCreate(saveInstanceState);
            setContentView(R.layout.permission_check);
            Log.d("S", "??");
            PermissionListener permissionListener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    startActivity(new Intent(PermissionCheck.this,PetitCoverActivity.class));
                    finish();
                }

                @Override
                public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                    Toast.makeText(PermissionCheck.this, "권한이 필요합니다", Toast.LENGTH_SHORT).show();
                }
            };

            new TedPermission(this)
                    .setPermissionListener(permissionListener)
                    .setRationaleMessage("권한이 필요합니다.")
                    .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
                    .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.READ_PHONE_STATE)
                    .check();

        }
}
