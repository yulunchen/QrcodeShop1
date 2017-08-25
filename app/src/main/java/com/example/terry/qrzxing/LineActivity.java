package com.example.terry.qrzxing;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LineActivity extends AppCompatActivity{
    Connection con;
    Statement stmt;
    ResultSet rs;
    Intent it,it1;
    String Woo_gmail, Woo_name, Woo_phone, Woo_address, Woo_ps, ip, dbName, sqldbaccount, sqldbpass;

    static final String TB_NAME = "Vip_TB";//SQL會員資料表
    //設定資料表中除了_id流水號以外的欄位名稱字串陣列
    static final String[] FROM = new String[] {"gmail", "name", "phone"," address", "ps"};
    DBHelper dbhelper=new DBHelper(this);
    SQLiteDatabase db;
    Cursor cur;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher_round);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        openDB();//開起sql資料庫設定
        getInt();//啟動自訂方法

    }

    protected void openDB(){
        db=dbhelper.getWritableDatabase();
        cur=db.rawQuery("SELECT * FROM Ip_TB", null);
        while (cur.moveToNext()) {
            ip = cur.getString(cur.getColumnIndex("ip"));
            dbName = cur.getString(cur.getColumnIndex("db"));
            sqldbaccount = cur.getString(cur.getColumnIndex("user"));
            sqldbpass = cur.getString(cur.getColumnIndex("pass"));
        }
    }

    protected void getInt(){
        //取得上頁帶來的gmail帳號
        it= getIntent();
        Woo_gmail=it.getStringExtra("Woo_gmail");
        //指定到HomeActivity
        it1=new Intent(this, HomeActivity.class);
        //執行非同步任務並帶入gmail資料
        task.execute(Woo_gmail);
    }

    // 非同步任務設定
    AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
        // 設定非同步任務內容
        @Override
        protected Void doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                // 連結外部資料庫
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection(
                        "jdbc:mysql://" + ip + "/" + dbName + "?useUnicode=true&characterEncoding=UTF-8", sqldbaccount,
                        sqldbpass);
                // 尋找外部資料庫會員表單中相同gmail的資料
                String selectSQL = "select * from woo_table where Woo_gmail='" + Woo_gmail + "'";
                stmt = con.createStatement();
                rs = stmt.executeQuery(selectSQL);// 執行sql查詢指令
                while (rs.next()) {//搜尋到資料時
                    //搜尋到的gmail與帶入的gmail相同
                    if (rs.getString("Woo_gmail").equals(params[0])) {
                        //取得會員名稱
                        Woo_name = rs.getString("Woo_name").toString().trim();
                        //取得會員手機號碼
                        Woo_phone = rs.getString("Woo_phone").toString().trim();
                        //取得會員地址
                        Woo_address = rs.getString("Woo_add").toString().trim();
                        //取得會員備註
                        Woo_ps = rs.getString("Woo_ps").toString().trim();
                        //將會員的資料全數存入內部的資料庫會員資料表
                        addData(Woo_gmail, Woo_name, Woo_phone, Woo_address, Woo_ps);
                    }
                }
                rs.close();
                stmt.close();
                con.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            return null;
        }

        // 設定非同步任務執行完後要做的事
        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            //開啟HomeActivity頁
            startActivity(it1);
            //關閉本頁
            finish();
        }
    };

    //在內部資料庫新增資料的方法
    private void addData(String gmail, String name, String phone, String address, String ps) {
        ContentValues cv=new ContentValues(5);//建立含 4個欄位的 ContentValues物件
        cv.put(FROM[0], gmail);//("欄位名", 字串)
        cv.put(FROM[1], name);
        cv.put(FROM[2], phone);
        cv.put(FROM[3], address);
        cv.put(FROM[4], ps);

        db.insert(TB_NAME, null, cv);//新增1筆記錄
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbhelper.close();
    }
}
