package com.example.terry.qrzxing;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {
    private final static int DBVersion = 1;
    private final static String DBName = "Vip_DB";
    private final static String IPTable = "Ip_TB";
    static final String Vip_TB = "Vip_TB";//會員資料表
    static final String Shop_TB = "Shop_TB";//購物車資料表
    private final static String _id="_id";
    private final static String IP_ip="ip";
    private final static String IP_db="db";
    private final static String IP_user="user";
    private final static String IP_pass="pass";

    private static final String createIpTable = "CREATE TABLE IF NOT EXISTS"+" "
            + IPTable + " ( "
            + _id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + IP_ip + " VARCHAR(60), "
            + IP_db +" VARCHAR(60), "
            + IP_user + " VARCHAR(60), "
            + IP_pass + " VARCHAR(60)); ";

    private static final String createVipTable = "CREATE TABLE IF NOT EXISTS "+Vip_TB +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + // 索引欄位
            "gmail VARCHAR(60), " +
            "name VARCHAR(32), " +
            "phone VARCHAR(32), " +
            "address VARCHAR(50), "+
            "ps VARCHAR(200))";

    //建立購物車資料表
    private static final String createShopTable ="CREATE TABLE IF NOT EXISTS " + Shop_TB +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +//會自動增加的流水號，一定要有
            "goods VARCHAR(32), " +//商品名稱欄位
            "price  INTEGER(32), " +//商品價格欄位
            "quantity  INTEGER(32))";//商品數量欄位

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DBHelper(Context context) {
        super(context, DBName, null, DBVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createIpTable);
        db.execSQL(createVipTable);
        db.execSQL(createShopTable);

        ContentValues cv=new ContentValues(4);
        cv.put(IP_ip, "sql12.freemysqlhosting.net:3306");
        cv.put(IP_db, "sql12189094");
        cv.put(IP_user, "sql12189094");
        cv.put(IP_pass, "xEFnDBsUZl");

        db.insert(IPTable, null, cv);//新增1筆記錄

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + IPTable);
        db.execSQL("DROP TABLE IF EXISTS " + Vip_TB);
        db.execSQL("DROP TABLE IF EXISTS " + Shop_TB);
        onCreate(db);
    }
}
