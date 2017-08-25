package com.example.terry.qrzxing;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class OrderActivity extends AppCompatActivity implements  View.OnClickListener{
    EditText editText;
    Button button;
    ListView lv;
    TextView txv;
    ImageView exit;
    String number, nq_all, total, ip, dbName, sqldbaccount, sqldbpass;;
    Connection con;
    Statement stmt;
    ResultSet rs;
    //集合物物件，用來放ListView的內容
    ArrayList<String> array=new ArrayList<>();
    int flag;

    DBHelper dbhelper=new DBHelper(this);
    SQLiteDatabase db;
    Cursor cur;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher_round);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        findView();
        openDB();
    }

    protected void findView() {
        editText=(EditText)findViewById(R.id.editText);
        button=(Button)findViewById(R.id.button);
        lv=(ListView)findViewById(R.id.lv);
        txv=(TextView)findViewById(R.id.txv);
        exit=(ImageView)findViewById(R.id.exit);
        exit.setOnClickListener(this);
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

    public void inquire(View v){
        if(flag==1){
            Intent it=new Intent(this, HomeActivity.class);
            startActivity(it);
            finish();
        }else {
            //取得輸入的訂單編號
            number = editText.getText().toString();
            if(number.length()<1){
                Toast.makeText(getApplicationContext(), "請輸入訂單編號查詢", Toast.LENGTH_LONG).show();
            }else {
                //清空集合物件
                array.clear();
                //進入非同步任務先關閉按鈕，避免二次執行非同步任務
                button.setEnabled(false);
                task.execute(number);
            }
        }
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
                String selectSQL = "select * from woo_order ";
                stmt = con.createStatement();
                rs = stmt.executeQuery(selectSQL);// 執行sql查詢指令
                nq_all="";
                while (rs.next()) {//有查詢到資料時
                    if (rs.getString("Woo_num").equals(params[0])) {
                        //取的訂單的購買項目
                        nq_all=rs.getString("Woo_nq").toString().trim();
                        //取得訂單的總金額
                        total=rs.getString("Woo_total").toString().trim();
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
            if (nq_all.length() < 1) {//訂單沒有內容時
                Toast.makeText(getApplicationContext(), "查無訂單", Toast.LENGTH_LONG).show();
            } else {
                //將訂單的購買項目內容用"。"號分割
                String[] nq_spli = nq_all.split("。");
                //將切割好的字串陣列帶入ListView顯示方法
                showList(nq_spli);
                //在畫面上顯示總金額
            }
            txv.setText("總金額："+total+"元");
            flag = 1;
            button.setText("回首頁");
            button.setEnabled(true);//恢復按鈕功能
        }
    };

    public void showList(String[] nq_spli){
        ArrayAdapter<String> nq_adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nq_spli);
        //將Adapter放入ListView
        lv.setAdapter(nq_adapter);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { // 判斷是否按下返回鍵
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbhelper.close();
    }
}
