package com.example.terry.qrzxing;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ShopActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, View.OnClickListener {
    ListView lv;
    TextView total;
    TextView goods_sel;
    EditText quantity_et;
    String goods_up, Woo_gmail, Woo_nq, Woo_num, Woo_total, phone, info, ip, dbName, sqldbaccount, sqldbpass;
    Button button;
    ImageView exit;
    int flag, subtotal;

    static final String Shop_TB = "Shop_TB";// 購物車資料表名稱
    static final String Vip_TB = "Vip_TB";// 會員資料表名稱
    static final String[] FROM = new String[]{"goods", "price", "quantity"};//SQLite資料庫的欄位名稱
    DBHelper dbhelper = new DBHelper(this);
    SQLiteDatabase db;//SQLite資料庫物件
    SimpleCursorAdapter adapter;//自設的ListView顯示方式
    Cursor cur, cur_shop, cur_tl;//SQLite查詢物件

    Connection con;
    PreparedStatement pst;
    ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher_round);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        findView();
        openDB();
        shopList();
        setListener();

        total();//執行合計計算方法

    }

    protected void findView() {
        goods_sel = (TextView) findViewById(R.id.goods_sel);
        quantity_et = (EditText) findViewById(R.id.quantity_et);
        total = (TextView) findViewById(R.id.total);
        lv = (ListView) findViewById(R.id.lv);
        button = (Button) findViewById(R.id.button);
        exit = (ImageView) findViewById(R.id.exit);
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

    protected void shopList() {
        //查詢購物車內所有資料
        cur_shop = db.rawQuery("SELECT *  FROM " + Shop_TB, null);
        adapter = new SimpleCursorAdapter(this,//自設的ListView顯示方式
                R.layout.item,//自設顯示方式的xml設定檔
                cur_shop,//要查詢SQLite中的哪些資料
                FROM,//要顯示的欄位，必須對應cur查詢時所定義的欄位名稱
                new int[]{R.id.goods, R.id.price, R.id.quantity},//上述欄位中的資料要擺放在ListView中的對應位置
                0);
        lv.setAdapter(adapter);// 設定 Adapter
    }

    protected void setListener() {
        lv.setOnItemClickListener(this);//設定短按監視器
        lv.setOnItemLongClickListener(this);//設定長按監視器
        exit.setOnClickListener(this);
    }

    @Override
    //短按時的設定
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //移動到按下的ListView位置
        cur_shop.moveToPosition(i);
        //取得該位置中""goods"欄位的商品名稱資料
        goods_up = cur_shop.getString(cur_shop.getColumnIndex(FROM[0]));
        //將商品名稱顯示在畫面上
        goods_sel.setText("產品名稱：" + goods_up);
        //取得該位置中"quantity"欄位的數量資料，並轉成字串顯示在畫面上
        quantity_et.setText(String.valueOf(cur_shop.getInt(cur_shop.getColumnIndex(FROM[2]))));
    }

    //修改鈕的設定
    public void upData(View v) {

        if ("".equals(quantity_et.getText().toString().trim())) {
            Toast.makeText(getApplicationContext(), "數量不能為空值", Toast.LENGTH_LONG).show();
        } else if (Integer.parseInt(quantity_et.getText().toString()) < 1) {
            Toast.makeText(getApplicationContext(), "數量不能小於1", Toast.LENGTH_LONG).show();
        } else {
            //設定需要更新資料的欄位數量
            ContentValues cv = new ContentValues(1);
            //取得修改後的數量字串資料，並轉成int數字資料
            int quantity_up = Integer.parseInt(quantity_et.getText().toString());
            //將"quantity"欄位的資料更新為quantity_up
            cv.put(FROM[2], quantity_up);
            //更新sql資料庫中指定的商品名稱的資料
            db.update(Shop_TB, cv, "goods=" + "'" + goods_up + "'", null);
            //重新整理畫面
            requery();
        }
    }

    //合計計算方法
    protected void total() {
        subtotal = 0;
        //讀取目前sql資料庫中所有資料
        cur_tl = db.rawQuery("SELECT *  FROM " + Shop_TB, null);
        //在每一筆資料進行計算
        while (cur_tl.moveToNext()) {
            //取得"price"欄位中的售價數字資料
            int price = cur_tl.getInt(cur_tl.getColumnIndex("price"));
            //取得"quantity"欄位中的數量數字資料
            int quantity = cur_tl.getInt(cur_tl.getColumnIndex("quantity"));
            //每筆資料的「售價X數量」加總
            subtotal = subtotal + price * quantity;
        }
        //在畫面上顯示合計的金額
        total.setText("合計：" + String.valueOf(subtotal) + "元");
        Woo_total = String.valueOf(subtotal);
    }

    //常按時的設定
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        cur_shop.moveToPosition(i);
        goods_up = cur_shop.getString(cur_shop.getColumnIndex(FROM[0]));
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        //設定對話視窗的標題
        d.setMessage("確定要刪除這項商品嗎?");
        //按下確定時的動作
        d.setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                //刪除sql資料庫中所選取商品名稱的整筆資料
                db.delete(Shop_TB, "goods=" + "'" + goods_up + "'", null);
                //重新整理畫面
                requery();
            }
        });
        //按下取消時
        d.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.cancel();// 關閉對話框
            }
        });
        d.show();
        return true;
    }

    //在xml設定畫面直接對按鈕物件的onClick填入方法，可不用在java程式中寫findViewById連接
    public void san(View v) {
        Intent it = new Intent(this, SignActivity.class);
        startActivity(it);
        finish();
    }

    // 重新整理畫面的自訂方法
    private void requery() {
        cur_shop = db.rawQuery("SELECT * FROM " + Shop_TB, null);
        //更改 Adapter的Cursor，並重置ListView
        adapter.changeCursor(cur_shop);
        //重算合計金額
        total();
    }

    //送出訂單方法
    public void order(View v) {
        if (flag == 1) {
            Intent it = new Intent(this, HomeActivity.class);
            startActivity(it);
            finish();
        } else if (subtotal < 1) {
            Toast.makeText(getApplicationContext(), "購物車沒有商品", Toast.LENGTH_LONG).show();
        } else {
            Cursor cur1 = db.rawQuery("SELECT * FROM " + Shop_TB, null);
            //給Woo_nq空值
            Woo_nq = "";
            while (cur1.moveToNext()) {//一筆一筆取出購物車表單內的資料
                //取得商品名稱
                String goods = cur1.getString(cur1.getColumnIndex(FROM[0]));
                //取得商品數量
                String quantity = String.valueOf(cur1.getInt(cur1.getColumnIndex(FROM[2])));
                //如果已有資料時在後面加上句號
                if (Woo_nq.length() > 0) {
                    Woo_nq += "。";
                }
                //將商品名稱與數量組合成一個字串
                Woo_nq = Woo_nq + goods + " 數量" + quantity + "組";
            }

            num();//產生訂單序號

            Cursor cur2 = db.rawQuery("SELECT *  FROM " + Vip_TB, null);
            //t從內部會員資料表中取出gmail與手機號碼
            while (cur2.moveToNext()) {
                Woo_gmail = cur2.getString(cur2.getColumnIndex("gmail"));
                phone = cur2.getString(cur2.getColumnIndex("phone"));
            }

            String[] empa = {Woo_gmail, Woo_nq, Woo_num, Woo_total};
            task.execute(empa);
        }

    }

    //產生訂單序號的方法
    protected void num() {
        //設計樣板
        SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddhhmmss");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日HH:mm");
        //取得系統時間
        Date dt = new Date(System.currentTimeMillis());
        //將系統時間轉成字串後在前面加上"Woo"
        Woo_num = "Woo" + sdf.format(dt);
        info = formatter.format(dt);
    }

    // 非同步任務設定
    AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
        // 設定非同步任務內容
        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(ShopActivity.this);
            mDialog.setTitle("連線伺服器");
            mDialog.setMessage("正在送出訂單中...");
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
                        sqldbpass);
                ///設定新增資料的sql語法
                String insertdbSQL = "insert into woo_order (Woo_gmail,Woo_nq,Woo_num,Woo_total) "
                        + "VALUES(?, ? , ?, ?)";
                //將帶入的字串陣列依序將字串放入相應的欄位順序
                pst = con.prepareStatement(insertdbSQL);
                pst.setString(1, params[0]);
                pst.setString(2, params[1]);
                pst.setString(3, params[2]);
                pst.setString(4, params[3]);
                //執行設定好的新增資料語法
                pst.executeUpdate();

                pst.close();
                con.close();
                //寄出訂單mail
                sendMail();
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
            Toast.makeText(getApplicationContext(), "訂單已送出，訂單編號已寄至您的信箱", Toast.LENGTH_LONG).show();
            //清掉這筆購物車表單資料
            String del = "DELETE FROM " + Shop_TB;
            db.execSQL(del);
            //重整畫面
            requery();
            button.setText("回首頁");
            flag = 1;
        }
    };

    //寄出email方法
    protected void sendMail() {
        try {
            //建立GMailSender物件，參數是寄件信箱與密碼
            GMailSender sender = new GMailSender("kitagawasan.test@gmail.com", "testmail");
            sender.sendMail("QRCODE購物訂單",//信件標題
                    "感謝您在" + info + "的購物" + '\n' + "您的訂單編號：" + Woo_num,//信件內容
                    "kitagawasan.test@gmail.com",//寄件者
                    Woo_gmail);//收件者
        } catch (Exception e) {
            e.printStackTrace();
        }
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
