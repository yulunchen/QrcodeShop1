package com.example.terry.qrzxing;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class OrderActivity extends AppCompatActivity implements IPaddress{
    EditText editText;
    Button button;
    ListView lv;
    TextView txv;
    String number, nq_all, total;
    Connection con;
    Statement stmt;
    ResultSet rs;
    //集合物件，用來放ListView的內容
    ArrayList<String> array=new ArrayList<>();
    int flag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher_round);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setView();

    }

    protected void setView() {
        editText=(EditText)findViewById(R.id.editText);
        button=(Button)findViewById(R.id.button);
        lv=(ListView)findViewById(R.id.lv);
        txv=(TextView)findViewById(R.id.txv);
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
            txv.setText(total);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, Menu.NONE, "首        頁");
        menu.add(0, 1, Menu.NONE, "產品掃描");
        menu.add(0, 2, Menu.NONE, "購  物  車");
        menu.add(0, 3, Menu.NONE, "會員資料");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                Intent it1 = new Intent(this, HomeActivity.class);//建立 Intent 並設定目標 Activity
                startActivity(it1);// 啟動 Intent 中的目標
                finish();
                break;
            case 1:
                Intent it2 = new Intent(this, SignActivity.class);//建立 Intent 並設定目標 Activity
                startActivity(it2);// 啟動 Intent 中的目標
                finish();
                break;
            case 2:
                Intent it3 = new Intent(this, ShopActivity.class);//建立 Intent 並設定目標 Activity
                startActivity(it3);// 啟動 Intent 中的目標
                finish();
                break;
            case 3:
                Intent it4 = new Intent(this, UserActivity.class);//建立 Intent 並設定目標 Activity
                startActivity(it4);// 啟動 Intent 中的目標
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { // 判斷是否按下返回鍵
            finish();
        }
        return false;
    }
}
