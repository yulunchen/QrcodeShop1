package com.example.terry.qrzxing;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements OnItemSelectedListener, View.OnClickListener {
    Spinner account_sp;
    Button start;
    String vipEmail, selEmail, ip, dbName, sqldbaccount, sqldbpass;
    ImageView exit;
    Connection con;
    Statement stmt;
    ResultSet rs;
    Intent it1, it2;
    int flag_a, flag_b;
    static final String Vip_TB = "Vip_TB";//會員資料表
    static final String Shop_TB = "Shop_TB";//購物車資料表
    DBHelper dbhelper = new DBHelper(this);
    SQLiteDatabase db;
    Cursor cur;
    ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher_round);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        View v = findViewById(R.id.LinearLayout1);
        v.getBackground().setAlpha(200);//0~255透明度值

        findView();// 執行頁面設定
        setSpinner();// 執行Spinner
        setListener();// 執行監聽器
        setSql();//執行內部資料庫設定

    }

    protected void findView() {// 頁面設定方法
        account_sp = (Spinner) findViewById(R.id.account_sp);
        start = (Button) findViewById(R.id.start);
        exit = (ImageView) findViewById(R.id.exit);
    }


    protected void setSpinner() {// Spinner設定
        AccountManager accountManager = AccountManager.get(getApplicationContext());
        // 取得指定 type 的 Account
        Account[] accounts = accountManager.getAccountsByType("com.google");

        ArrayList<String> account_str = new ArrayList<String>();// 建立放入帳號的陣列
        // 取得所有帳號並轉成String型態後放入account_str陣列
        for (Account account : accounts) {
            vipEmail = account.name.toString();
            account_str.add(vipEmail);
        }

        // 將account_str中的帳號放入Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,
                account_str);
        account_sp.setAdapter(adapter);

    }


    protected void setListener() {// 註冊監聽器
        account_sp.setOnItemSelectedListener(this);
        exit.setOnClickListener(this);
    }

    // 取得在Spinner中所選取的帳號並轉成String型態
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        selEmail = (String) account_sp.getSelectedItem();
    }

    protected void setSql() {
        db = dbhelper.getWritableDatabase();
        cur = db.rawQuery("SELECT * FROM Ip_TB", null);
        while (cur.moveToNext()) {
            ip = cur.getString(cur.getColumnIndex("ip"));
            dbName = cur.getString(cur.getColumnIndex("db"));
            sqldbaccount = cur.getString(cur.getColumnIndex("user"));
            sqldbpass = cur.getString(cur.getColumnIndex("pass"));
        }
        //清空會員資料表
        String delVip = "DELETE FROM " + Vip_TB;
        db.execSQL(delVip);
        //清空購物車資料表
        String delShop = "DELETE FROM " + Shop_TB;
        db.execSQL(delShop);

    }


    // 開始鈕
    public void start(View v) {
        // 取的網路連線狀態
        // 必須在AndroidManifest.xml新增NETWORK權限
        ConnectivityManager cManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {// 有連上網路時
            nextActivity();// 執行Intent物件設定
            task.execute(selEmail);// 執行非同步任務並帶入帳號字串
        } else {// 沒連上網路時
            Toast.makeText(v.getContext(), "沒有連上網路", Toast.LENGTH_LONG).show();
        }

    }

    // Intent設定
    protected void nextActivity() {
        // 設定目的地為UserActivity頁面，並放入帳號字串資料
        it1 = new Intent(this, LineActivity.class);
        flag_b = 1;
        it1.putExtra("Woo_gmail", selEmail);
        it1.putExtra("flag", flag_b);
        // 設定目的地為RegistActivity頁面，並方入帳號字串資料
        it2 = new Intent(this, RegisterActivity.class);
        it2.putExtra("Woo_gmail", selEmail);
    }

    // 非同步任務設定
    AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
        // 設定非同步任務內容
        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(MainActivity.this);
            mDialog.setTitle("連線伺服器");
            mDialog.setMessage("資料查詢中...");
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
                con = DriverManager.getConnection(
                        "jdbc:mysql://" + ip + "/" + dbName + "?useUnicode=true&characterEncoding=UTF-8", sqldbaccount,
                        sqldbpass);//vivian  與IPaddress設定名稱相同
                // 查詢帳號是否已使用
                String selectSQL = "select * from woo_table where Woo_gmail='" + selEmail + "'";
                //尋找資料夾名稱 找尋是否有相同帳號 如直接蒐尋欄位name 會找不到資料
                stmt = con.createStatement();
                rs = stmt.executeQuery(selectSQL);// 執行sql查詢指令
                // 讀取所有查詢資料中有無相同帳號名稱
                while (rs.next()) {
                    // 一定要注意就算只有輸入一個值，params[0]也不能寫成params
                    if (rs.getString("Woo_gmail").equals(params[0])) {
                        flag_a = 1;// 帳號已註冊將旗標改為1
                    }

                }
                rs.close();
                stmt.close();
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
            if (flag_a == 1) {
                // 已註冊帳號開啟UserActivity頁面
                startActivity(it1);
                // 關閉本頁
                finish();
            } else {
                // 未註冊帳號RegistActivity頁面
                startActivity(it2);
                // 關閉本頁
                finish();
            }
        }
    };

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(this, IpActivity.class));
        finish();
        return super.onOptionsItemSelected(item);
    }

    Boolean isExit = false;
    Boolean hasTask = false;
    Timer timerExit = new Timer();// 建立計時器
    TimerTask tak = new TimerTask() {// 設定要執行的工作
        public void run() {
            isExit = false;
            hasTask = true;
        }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { // 判斷是否按下返回鍵
            if (isExit == false) {// 按一下不會退出
                isExit = true; // 按兩次才退出
                Toast.makeText(this, "再按一次返回鍵離開", Toast.LENGTH_SHORT).show();
                if (!hasTask) {
                    timerExit.schedule(tak, 2000);// 如果超過兩秒則恢復預設值
                }
            } else {
                finishAffinity();// 結束APP程式
            }
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        if (isExit == false) {// 按一下不會退出
            isExit = true; // 按兩次才退出
            Toast.makeText(this, "再按一次返回鍵離開", Toast.LENGTH_SHORT).show();
            if (!hasTask) {
                timerExit.schedule(tak, 2000);// 如果超過兩秒則恢復預設值
            }
        } else {
            finishAffinity();// 結束APP程式
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbhelper.close();
    }

}
