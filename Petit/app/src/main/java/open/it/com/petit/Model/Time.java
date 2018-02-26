package open.it.com.petit.Model;

/**
 * Created by user on 2017-05-22.
 */

public class Time {
    /*
    * MT_ID = IDX
    * APP_KEY = app key
    * MT_TIME = 시간(1~24)
    * MT_AMOUNT = 급식량(1~5)
    * REG_DT = 등록일자
    */
    private int MT_ID;
    private String APP_KEY;
    private int MT_TIME;
    private int MT_AMOUNT;
    private String REG_DT;

    public Time(int MT_ID, String APP_KEY, int MT_TIME, int MT_AMOUNT, String REG_DT) {
        this.MT_ID = MT_ID;
        this.APP_KEY = APP_KEY;
        this.MT_TIME = MT_TIME;
        this.MT_AMOUNT = MT_AMOUNT;
        this.REG_DT = REG_DT;
    }

    public int getMT_ID() {
        return MT_ID;
    }

    public void setMT_ID(int MT_ID) {
        this.MT_ID = MT_ID;
    }

    public String getAPP_KEY() {
        return APP_KEY;
    }

    public void setAPP_KEY(String APP_KEY) {
        this.APP_KEY = APP_KEY;
    }

    public int getMT_TIME() {
        return MT_TIME;
    }

    public void setMT_TIME(int MT_TIME) {
        this.MT_TIME = MT_TIME;
    }

    public int getMT_AMOUNT() {
        return MT_AMOUNT;
    }

    public void setMT_AMOUNT(int MT_AMOUNT) {
        this.MT_AMOUNT = MT_AMOUNT;
    }

    public String getREG_DT() {
        return REG_DT;
    }

    public void setREG_DT(String REG_DT) {
        this.REG_DT = REG_DT;
    }
}
