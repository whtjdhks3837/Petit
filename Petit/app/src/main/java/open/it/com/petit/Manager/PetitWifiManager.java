package open.it.com.petit.Manager;

import android.content.Context;
import android.net.wifi.WifiManager;

/**
 * Created by user on 2017-07-31.
 */

public class PetitWifiManager {
    private static final String TAG = "PetitWifiManager";
    private Context context;
    private WifiManager wifiManager;

    public static PetitWifiManager instance;

    public static PetitWifiManager getInstance(Context context) {
        if (instance == null) {
            instance = new PetitWifiManager(context);
        }
        return instance;
    }

    private PetitWifiManager(Context context) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
    }

    public WifiManager getWifiManager() {
        return wifiManager;
    }
}
