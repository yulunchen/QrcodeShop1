package com.example.terry.qrzxing;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.TextView;

public class UserActivity extends AppCompatActivity implements View.OnClickListener{
	TextView userTxv;
	ImageView exit;
	String Woo_name, Woo_gmail, Woo_phone, Woo_add, Woo_ps;
	Intent it2;
	Button btn_update;

	static final String TB_NAME = "Vip_TB";
	DBHelper dbhelper=new DBHelper(this);
	SQLiteDatabase db;//SQLite資料庫物件
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

		findView();//執行畫面設定
		getVip();

	}


	protected void getVip(){
		db=dbhelper.getWritableDatabase();
		cur=db.rawQuery("SELECT * FROM "+ TB_NAME, null);
		while (cur.moveToNext()){
			Woo_gmail=cur.getString(cur.getColumnIndex("gmail"));
			Woo_name=cur.getString(cur.getColumnIndex("name"));
			Woo_phone=cur.getString(cur.getColumnIndex("phone"));
			Woo_add=cur.getString(cur.getColumnIndex("address"));
			Woo_ps=cur.getString(cur.getColumnIndex("ps"));
		}
		userTxv.setText("    E-mail："+Woo_gmail+"\n姓       名："+Woo_name+"\n手機號碼：" + Woo_phone + "\n地       址：" + Woo_add+ "\n備       註：" + Woo_ps);
	}

	protected void findView(){//畫面設定
		userTxv=(TextView)findViewById(R.id.userTxv);
		btn_update=(Button)findViewById(R.id.btn_update);
		exit=(ImageView)findViewById(R.id.exit);
		exit.setOnClickListener(this);
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
		it2.putExtra("Woo_gmail", Woo_gmail);
		it2.putExtra("Woo_name",Woo_name);
		it2.putExtra("Woo_add", Woo_add);
		it2.putExtra("Woo_phone", Woo_phone);
		it2.putExtra("Woo_ps", Woo_ps);

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
