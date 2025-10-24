package com.rgbsolution.highland_emart.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.rgbsolution.highland_emart.common.Common;


public class DBHelper {

    public static final String TAG = "DBHelper";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "HIGHLAND";

    private static final int DATABASE_VERSION = 27; /*-- 업그레이드를 필요로 할 경우에 버전의 값을 올려주면 된다.  --*/

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            if (Common.D) {
                Log.d(TAG, "Table Upgrade ........ ");
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            Log.d(TAG, "Upgrading from version " + oldVersion
                    + " to " + newVersion);

            //if(oldVersion != newVersion) {
                Log.d(TAG, "업그레이드 하러 들어옴 : " + oldVersion
                        + " to " + newVersion);
                try {
                    db.beginTransaction();
                    //db.execSQL("DROP TABLE TB_GOODS_WET");
                    //db.execSQL("ALTER TABLE TB_SHIPMENT ADD COLUMN WH_AREA  text");
                    //db.execSQL("ALTER TABLE TB_GOODS_WET ADD COLUMN  BOX_ORDER Integer DEFAULT 0");
                    //db.execSQL("ALTER TABLE TB_SHIPMENT ADD COLUMN LAST_BOX_ORDER INTEGER");
                    db.setTransactionSuccessful();
                } catch (IllegalStateException e) {
                    Log.d(TAG, "onUpgrade exception -> " + e.getMessage().toString());
                } finally {
                    db.endTransaction();
                }
            //}

            if (Common.D) {
                Log.d(TAG, "Table Upgrade Drop ");
            }
        }
    }

    public DBHelper(Context ctx) {
        this.mCtx = ctx;
    }

    public DBHelper open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public Cursor selectSql(String sql) {
        return mDb.rawQuery(sql, null);
    }

    public void executeSql(String sql) {
        mDb.execSQL(sql);
    }
}
