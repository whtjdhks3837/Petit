package open.it.com.petit.Util;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.text.SimpleDateFormat;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by user on 2017-10-30.
 */

public class Util {
    public static String getPhoneNum(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
            return telephonyManager.getLine1Number().replace("+82", "0");
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getDate() {
        java.util.Date date = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        return sdf.format(date).toString();
    }
}
