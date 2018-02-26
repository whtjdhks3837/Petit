package open.it.com.petit.Handler;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import open.it.com.petit.Model.Time;
import open.it.com.petit.Model.Week;

/**
 * Created by user on 2017-05-22.
 * 데이터를 추가, 조회 삭제하는 핸들러.
 */

public class ReserveDBHandler extends SQLiteOpenHelper {
    public static final String TAG = "ReserveDBHandler";
    public String DBLOCATION = "/data/user/0/open.it.com.petit/databases/";
    public String DBNAME = null;

    private ReserveDBHandler helper = this;
    private Handler handler;
    private Context context;
    private SQLiteDatabase db;

    public ReserveDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory,
                            int version) {
        super(context, name, null, version);
    }

    public ReserveDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory,
                            int version, Handler handler) {
        super(context, name, null, version);
        this.handler = handler;
        this.context = context;
        this.DBNAME = name;
        setSQLite(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void setSQLite(Context context) {
        File folder = new File(DBLOCATION );

        if(!folder.exists())
            folder.mkdirs();

        AssetManager assetManager = context.getResources().getAssets();
        File outFile = new File(DBLOCATION + DBNAME);
        try {
            InputStream is = assetManager.open(DBNAME, AssetManager.ACCESS_BUFFER);
            FileOutputStream fo = null;

            //처음 DB생성. 파일이 없을 시
            if(outFile.length() <= 0) {
                byte buf[] = new byte[is.available()];

                fo = new FileOutputStream(outFile);
                int length = 0;
                while ((length = is.read(buf)) > 0 ) {
                    fo.write(buf, 0, length);
                }
                is.close();
                fo.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openDatabase() {
        String dbPath = context.getDatabasePath(DBNAME).getPath();

        if (db != null && db.isOpen()) {
            return ;
        }
        db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
    }

    public ArrayList selectTime(String tableName) {
        openDatabase();
        String query = "SELECT * FROM " + tableName + " ORDER BY MT_TIME ASC";

        db = helper.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);

            ArrayList<Time> timeList = new ArrayList<>();

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                timeList.add(new Time(cursor.getInt(0), cursor.getString(1),
                        cursor.getInt(2), cursor.getInt(3), cursor.getString(4)));
                cursor.moveToNext();
            }

            for (Time t : timeList) {
                Log.d(TAG, "시간 "+ t.getMT_TIME());
            }
            return timeList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            db.close();
            cursor.close();
        }
    }

    public ArrayList selectWeek(String tableName) {
        openDatabase();
        String query = "SELECT * FROM " + tableName + " ORDER BY MW_WEEK ASC";

        db = helper.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);

            ArrayList<Week> weekList = new ArrayList<>();

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                weekList.add(new Week(cursor.getString(0), cursor.getInt(1), cursor.getString(2)));
                cursor.moveToNext();
            }

            for (Week w : weekList) {
                Log.d(TAG, "요일" + w.getMW_WEEK() +"");
            }
            return weekList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            db.close();
            cursor.close();
        }
    }

    public void deleteTimeData(int MT_ID, boolean flag) {
        openDatabase();
        Log.d(TAG, "delete");
        String query = "delete from TB_MACHINE_TIME where MT_ID=" + MT_ID;
        try {
            db = helper.getWritableDatabase();

            db.execSQL(query);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        if(flag)
            handler.sendMessage(handler.obtainMessage(1));
    }

    public void insertTimeData(String APP_KEY, String MT_TIME, String MT_AMOUNT, boolean flag) {
        openDatabase();
        String query = "insert into TB_MACHINE_TIME(APP_KEY, MT_TIME, MT_AMOUNT, REG_DT) " +
                "values('" + APP_KEY + "', '" + MT_TIME +"', '" + MT_AMOUNT + "', DATETIME('NOW', 'LOCALTIME'))";
        try {
            db = helper.getWritableDatabase();
            db.execSQL(query);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        if(flag)
            handler.sendMessage(handler.obtainMessage(1));
    }

    public void insertWeekDay(String APP_KEY, int day) {
        openDatabase();
        String query = "insert into TB_MACHINE_WEEK(APP_KEY, MW_WEEK, REG_DT) " +
                "values('" + APP_KEY + "', '" + day + "', DATETIME('NOW', 'LOCALTIME'))";
        try {
            db = helper.getWritableDatabase();
            db.execSQL(query);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public void deleteTableAllRows(String tableName) {
        openDatabase();
        String query = "delete from " + tableName;
        try {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL(query);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }
}
