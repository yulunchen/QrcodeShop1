package com.example.terry.qrzxing;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class UserActivity extends AppCompatActivity implements IPaddress{
	TextView userTxv;
	String Woo_name, Woo_gmail, Woo_phone, Woo_add, Woo_ps;
	Intent it2;
	Button btn_update;

	static final String DB_NAME = "Vip_DB";// SQLitey資料庫名稱//"ShopDB"
	static final String TB_NAME = "Vip_TB";// SQLite資料表名稱//"Shoplist
	static final String[] FROM = new String[] {"goods","price","quantity"};//SQLite資料庫的欄位名稱
	SQLiteDatabase sql_db;//SQLite資料庫物件
	Cursor cur;//SQLite查詢物件

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);
		getSupportActionBar().setLogo(R.mipmap.ic_launcher_round);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		View v = findViewById(R.id.LinearLayout1);
		v.getBackground().setAlpha(150);//0~255透明度值

		myView();//執行畫面設定
		getVip();
		
	}


	protected void getVip(){
		sql_db = openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
		//查詢資料表
		cur=sql_db.rawQuery("SELECT * FROM "+ TB_NAME, null);
		while (cur.moveToNext()){
			Woo_gmail=cur.getString(cur.getColumnIndex("gmail"));
			Woo_name=cur.getString(cur.getColumnIndex("name"));
			Woo_phone=cur.getString(cur.getColumnIndex("phone"));
			Woo_add=cur.getString(cur.getColumnIndex("address"));
			Woo_ps=cur.getString(cur.getColumnIndex("ps"));
		}
		userTxv.setText("會員資料: \n姓名:"+Woo_name+"\n手機號碼:" + Woo_phone + "\n地址是:" + Woo_add+ "\n您的需求是:" + Woo_ps);
	}
	
	protected void myView(){//畫面設定
		userTxv=(TextView)findViewById(R.id.userTxv);
		btn_update=(Button)findViewById(R.id.btn_update);
	}
	
	public void update(View v)
	{
		nextActivity();
		startActivity(it2);
		finish();
	}
	
	protected void nextActivity() {
		// 設定目的地為UserActivity頁面，並放入帳號字串資料
		it2 = new Intent(this, UpdateActivity.class);
		it2.putExtra("Woo_gmail", Woo_gmail);   //vivian 0723
		it2.putExtra("Woo_name",Woo_name);
		it2.putExtra("Woo_add", Woo_add);
		it2.putExtra("Woo_phone", Woo_phone);
		it2.putExtra("Woo_ps", Woo_ps);

	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, Menu.NONE, "首        頁");
		menu.add(0, 1, Menu.NONE, "產品掃描");
		menu.add(0, 2, Menu.NONE, "購  物  車");
		menu.add(0, 3, Menu.NONE, "訂單查詢");
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
				Intent it4 = new Intent(this, OrderActivity.class);//建立 Intent 並設定目標 Activity
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
