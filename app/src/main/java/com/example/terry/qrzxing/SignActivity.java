package com.example.terry.qrzxing;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SignActivity extends AppCompatActivity implements View.OnClickListener {
    Button button;
    TextView idTxv, goodsTxv, priceTxv, infoTxv;
    ImageView exit;
    URL url;
    Bitmap bitmap;

    Connection con;
    Statement stmt;
    ResultSet rs;
    String Woo_id, Woo_goods, Woo_info, Woo_img, ip, dbName, sqldbaccount, sqldbpass;
    int Woo_price;

    static final String TB_NAME = "Shop_TB";// SQLite資料表名稱//"Shoplist
    static final String[] FROM = new String[]{"goods", "price", "quantity"};//SQLite資料庫的欄位名稱
    DBHelper dbhelper = new DBHelper(this);
    SQLiteDatabase db;//SQLite資料庫物件
    Cursor cur;//SQLite查詢物件
    ImageView img;
    int flag = 0;
    ProgressDialog mDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher_round);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        openDB();
        findView();

    }

    protected void openDB() {
        // 開啟或建立資料庫
        db = dbhelper.getWritableDatabase();
        cur = db.rawQuery("SELECT * FROM Ip_TB", null);
        while (cur.moveToNext()) {
            ip = cur.getString(cur.getColumnIndex("ip"));
            dbName = cur.getString(cur.getColumnIndex("db"));
            sqldbaccount = cur.getString(cur.getColumnIndex("user"));
            sqldbpass = cur.getString(cur.getColumnIndex("pass"));
        }
    }

    protected void findView() {
        button = (Button) findViewById(R.id.button);
        idTxv = (TextView) findViewById(R.id.idTxv);
        goodsTxv = (TextView) findViewById(R.id.goodsTxv);
        priceTxv = (TextView) findViewById(R.id.priceTxv);
        infoTxv = (TextView) findViewById(R.id.infoTxv);
        img = (ImageView) findViewById(R.id.imageView);
        exit = (ImageView) findViewById(R.id.exit);
        exit.setOnClickListener(this);
    }

    //建立IntentIntegrator物件前，要先到Gradle Scripts/build.gradle(Modile:app)中
    //在dependencies { }內貼上
    //compile 'com.journeyapps:zxing-android-embedded:3.2.0@aar'
    //compile 'com.google.zxing:core:3.2.1'
    //這兩行文字，之後在畫面右上方會出現Sync的選項，按下就會自動下載插件
    public void san(View v) {
        //按下掃描鈕後將按鈕改為加入購物功能
        if (flag == 0) {//掃描
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setPrompt("請掃描"); //底部的提示文字，設為""可以置空
            integrator.setCameraId(0); //前置或者后置摄像头
            integrator.setBeepEnabled(false); //掃描成功的「畢畢」声，默認為開啟
            integrator.initiateScan();
        } else if (flag == 1) {//加入購物
            //查詢SQLite資料庫中有相同商品名稱的資料
            cur = db.rawQuery("SELECT * FROM " + TB_NAME + " WHERE  goods=?", new String[]{Woo_goods});
            int total = cur.getCount();
            if (total > 0) {
                Toast.makeText(getApplicationContext(), "已加入購物車", Toast.LENGTH_LONG).show();
            } else {
                //沒有相同商品名稱的資料，新增一筆資料
                addData(Woo_goods, Woo_price, "1");//輸入商品名稱、商品售價、購買數量(預設為1)
            }
        }
    }

    //將掃描QRCODE後所得到的資料傳回
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null) {
            if (result.getContents() == null) {//沒有掃到QRCODE時
                Toast.makeText(this, "掃描不到資料", Toast.LENGTH_LONG).show();
            } else {
                idTxv.setText(result.getContents());//在畫面上顯示QRCODE解析出來的商品編號
                Woo_id = result.getContents();//將商品編號放入id字串物件中
                flag = 1;//掃描後將旗標設為1
                task.execute();//執行非同步任務
            }
        }
    }

    // 非同步任務設定
    AsyncTask<Void, Integer, Integer> task = new AsyncTask<Void, Integer, Integer>() {
        // 設定非同步任務內容
        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(SignActivity.this);
            mDialog.setTitle("連線伺服器");
            mDialog.setMessage("正在下載資料中...");
            //    設置setCancelable(false); 表示我們不能取消這個彈出框，等下載完成之後再讓彈出框消失
            mDialog.setCancelable(false);
            //    設置ProgressDialog樣式為圓圈的形式
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int i = 0;
            // TODO Auto-generated method stub
            try {
                // 連結外部資料庫
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection(
                        "jdbc:mysql://" + ip + "/" + dbName + "?useUnicode=true&characterEncoding=UTF-8", sqldbaccount,
                        sqldbpass);
                //在外部資料庫中查詢掃描QRCODE的商品資料
                String selectSQL = "select * from woo_data where Woo_id=" + "'" + Woo_id + "'";
                stmt = con.createStatement();
                rs = stmt.executeQuery(selectSQL);// 執行sql查詢指令
                // 讀取外部資料庫中的商品名稱(Woo_goods)與商品售價資料(Woo_price)
                while (rs.next()) {//有查詢到資料時
                    //取的Woo_goods欄位的商品名稱，並放入goods字串物件
                    Woo_goods = rs.getString("Woo_goods").toString();
                    //取的Woo_price欄位的商品售價，並放入price數字物件
                    Woo_price = rs.getInt("Woo_price");
                    Woo_info = rs.getString("Woo_info").toString();
                    Woo_img = rs.getString("Woo_img").toString();
                    url = new URL(Woo_img);
                    bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    i++;
                }
                //關閉所有外部資料庫物件
                rs.close();
                stmt.close();
                con.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return i;//在外部資料庫有找到符合的資料時回傳1
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            //執行中 可以在這邊告知使用者進度
        }

        // 設定非同步任務執行完後要做的事
        @Override
        protected void onPostExecute(Integer result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            mDialog.dismiss();
            if (result < 1) {//沒找到符合得資料時
                Toast.makeText(getApplicationContext(), "查無此QRCODE資料", Toast.LENGTH_LONG).show();
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();
            } else {
                goodsTxv.setText(Woo_goods);//在畫面上顯示商品名稱
                priceTxv.setText(String.valueOf(Woo_price).toString());//在畫面上顯示商品售價
                infoTxv.setText(Woo_info);
                //setImage();//在畫面上顯示圖片
                img.setImageBitmap(bitmap);
                //變更按鈕文字
                button.setText("加入購物車");
            }
        }
    };

    //在SQLite新增資料的方法
    private void addData(String goods, int price, String quantity) {
        ContentValues cv = new ContentValues(3);//建立含 3 個欄位的 ContentValues物件
        cv.put(FROM[0], goods);//在goods填入商品名稱
        cv.put(FROM[1], price);//在price填入商品售價
        cv.put(FROM[2], quantity);//在quantity填入購買數量

        db.insert(TB_NAME, null, cv);//新增1筆記錄
    }

    //顯示圖片方法
    protected void setImage() {
        //將圖檔的路徑與名稱結合成一字串放入uri字串物件
        String uri = "@drawable/" + Woo_img;
        //取得圖片在Resource(res)的路徑
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
        //設定dish_img圖片物件所要顯示的圖片路徑
        img.setImageResource(imageResource);
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
