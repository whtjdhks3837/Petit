package open.it.com.petit.Model;

/**
 * Created by user on 2017-05-22.
 */

public class Week {
    /*
    * APP_KEY = app key
    * MW_WEEK = 요일 (월:0, 화:1, 수:2, 목:3, 금:4, 토:5, 일:6)
    * REG_DT = 등록일자
    */
    private String APP_KEY;
    private int MW_WEEK;
    private String REG_DT;

    public Week(String APP_KEY, int MW_WEEK, String REG_DT) {
        this.APP_KEY = APP_KEY;
        this.MW_WEEK = MW_WEEK;
        this.REG_DT = REG_DT;
    }

    public String getAPP_KEY() {
        return APP_KEY;
    }

    public void setAPP_KEY(String APP_KEY) {
        this.APP_KEY = APP_KEY;
    }

    public int getMW_WEEK() {
        return MW_WEEK;
    }

    public void setMW_WEEK(char MW_WEEK) {
        this.MW_WEEK = MW_WEEK;
    }

    public String getREG_DT() {
        return REG_DT;
    }

    public void setREG_DT(String REG_DT) {
        this.REG_DT = REG_DT;
    }
}
