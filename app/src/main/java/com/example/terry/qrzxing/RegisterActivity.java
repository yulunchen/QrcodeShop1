package com.example.terry.qrzxing;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    TextView emailTxv;
    EditText nameEdit, phoneEdit, addressEdit, psEdit;
    Button registerBt;
    String Woo_gmail, Woo_name, Woo_phone, Woo_add, Woo_ps, ip, dbName, sqldbaccount, sqldbpass;
    Intent it1, it2;
    ImageView exit;

    Connection con;
    PreparedStatement pst;

    static final String TB_NAME = "Vip_TB";
    //設定資料表中除了_id流水號以外的欄位名稱字串陣列
    static final String[] FROM = new String[]{"gmail", "name", "phone", " address", "ps"};
    DBHelper dbhelper = new DBHelper(this);
    SQLiteDatabase db;
    Cursor cur;
    ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher_round);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        View v = findViewById(R.id.LinearLayout1);
        v.getBackground().setAlpha(200);//0~255透明度值

        getInt();//取得上一頁帶來的資料
        myView();//執行畫面設定
        openDB();
    }

    protected void getInt() {//設定接收資料
        //取得上一頁帶來的字串資料
        it1 = getIntent();
        Woo_gmail = it1.getStringExtra("Woo_gmail");
    }

    protected void openDB() {
        db = dbhelper.getWritableDatabase();
        cur = db.rawQuery("SELECT * FROM Ip_TB", null);
        while (cur.moveToNext()) {
            ip = cur.getString(cur.getColumnIndex("ip"));
            dbName = cur.getString(cur.getColumnIndex("db"));
            sqldbaccount = cur.getString(cur.getColumnIndex("user"));
            sqldbpass = cur.getString(cur.getColumnIndex("pass"));
        }
    }

    protected void myView() {//畫面設定
        emailTxv = (TextView) findViewById(R.id.emailEdit);
        emailTxv.setText(Woo_gmail);
        nameEdit = (EditText) findViewById(R.id.nameEdit);
        phoneEdit = (EditText) findViewById(R.id.phoneEdit);
        addressEdit = (EditText) findViewById(R.id.addressEdit);
        psEdit = (EditText) findViewById(R.id.psEdit);
        registerBt = (Button) findViewById(R.id.registerBt);
        exit = (ImageView) findViewById(R.id.exit);
        exit.setOnClickListener(this);

    }

    protected void getEdit() {//設定要取得的輸入資料
        Woo_name = nameEdit.getText().toString();
        Woo_phone = phoneEdit.getText().toString();
        Woo_add = addressEdit.getText().toString();
        Woo_ps = psEdit.getText().toString();
    }

    //註冊按鈕
    public void regist(View v) {
        //判斷有無輸入資料
        if ("".equals(nameEdit.getText().toString().trim())) {
            Toast.makeText(v.getContext(), "請輸入姓名", Toast.LENGTH_LONG).show();
        } else if (3 > addressEdit.getText().toString().trim().length()) {
            Toast.makeText(v.getContext(), "地址格式不正確", Toast.LENGTH_LONG).show();
        } else if (10 != phoneEdit.getText().toString().trim().length()) {
            Toast.makeText(v.getContext(), "手機號碼格式不正確", Toast.LENGTH_LONG).show();
        } else {
            //取得輸入資料
            getEdit();
            //設定目的地與所要帶去的資料
            it2 = new Intent(this, HomeActivity.class);
            it2.putExtra("Woo_gmail", Woo_gmail);
            //將取得的輸入資料放入字串陣列
            String[] empa = {Woo_gmail, Woo_name, Woo_phone, Woo_add, Woo_ps};
            //將會員資料存入內部會員資料表
            addData(Woo_gmail, Woo_name, Woo_phone, Woo_add, Woo_ps);
            //執行非同步任務並帶入字串陣列
            task.execute(empa);
        }
    }

    // 非同步任務設定
    AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
        // 設定非同步任務內容
        protected void onPreExecute() {
            mDialog = new ProgressDialog(RegisterActivity.this);
            mDialog.setTitle("連線伺服器");
            mDialog.setMessage("會員資料建立中...");
            //    設置setCancelable(false); 表示我們不能取消這個彈出框，等下載完成之後再讓彈出框消失
            mDialog.setCancelable(false);
            //    設置ProgressDialog樣式為圓圈的形式
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                // 連結外部資料庫
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection("jdbc:mysql://" + ip + "/" + dbName + "?useUnicode=true&characterEncoding=UTF-8", sqldbaccount, sqldbpass);
                //設定新增資料的sql語法
                String insertdbSQL = "insert into woo_table (id,Woo_gmail,Woo_name,Woo_phone,Woo_add,Woo_ps) "
                        + "select ifNULL(max(id),0)+1,?,?,?,?,? FROM woo_table";
                //將帶入的字串陣列依序將字串放入相應的欄位順序
                pst = con.prepareStatement(insertdbSQL);
                pst.setString(1, params[0]);
                pst.setString(2, params[1]);
                pst.setString(3, params[2]);
                pst.setString(4, params[3]);
                pst.setString(5, params[4]);
                //執行設定好的新增資料語法
                pst.executeUpdate();

                pst.close();
                con.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        // 設定非同步任務執行完後要做的事
        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            mDialog.dismiss();
            //開啟 UserActivity
            startActivity(it2);
            //關閉此頁
            finish();
        }
    };

    private void addData(String gmail, String name, String phone, String address, String ps) {
        ContentValues cv = new ContentValues(5);
        cv.put(FROM[0], gmail);
        cv.put(FROM[1], name);
        cv.put(FROM[2], phone);
        cv.put(FROM[3], address);
        cv.put(FROM[4], ps);

        db.insert(TB_NAME, null, cv);//新增1筆記錄
    }

    //自訂返回鍵方法
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { // 判斷是否按下返回鍵
            //按返回鍵回到登入頁
            Intent it = new Intent(this, MainActivity.class);
            startActivity(it);
            //關閉本頁
            finish();
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        Intent it = new Intent(this, MainActivity.class);
        startActivity(it);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbhelper.close();
    }
}
