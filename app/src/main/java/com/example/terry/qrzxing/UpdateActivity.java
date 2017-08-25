package com.example.terry.qrzxing;

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
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class UpdateActivity extends AppCompatActivity implements View.OnClickListener{
	EditText name_edit, phone_edit, address_edit,ps_edit;
	Intent it1, it2;
	Button savebt;
	ImageView exit;
	String Woo_gmail,Woo_name,Woo_add,Woo_phone,Woo_ps, ip, dbName, sqldbaccount, sqldbpass;
	Connection con;

	static final String TB_NAME = "Vip_TB";
	//設定資料表中除了_id流水號以外的欄位名稱字串陣列
	static final String[] FROM = new String[] {"name", "phone"," address", "ps"};
	DBHelper dbhelper=new DBHelper(this);
	SQLiteDatabase db;
	Cursor cur;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update);
		getSupportActionBar().setLogo(R.mipmap.ic_launcher_round);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		getInt();
		findView();
		openDB();

		exit.setOnClickListener(this);
	}

	protected void findView(){
		name_edit = (EditText)findViewById(R.id.name_edit);
		phone_edit = (EditText)findViewById(R.id.phone_edit);
		address_edit = (EditText)findViewById(R.id.address_edit);
		ps_edit = (EditText)findViewById(R.id.ps_edit);
		savebt=(Button)findViewById(R.id.savebt);
		name_edit.setText(Woo_name);
		phone_edit.setText(Woo_phone);
		address_edit.setText(Woo_add);
		ps_edit.setText(Woo_ps);
		exit=(ImageView)findViewById(R.id.exit);
	}

	public void save (View v) {
		//判斷有無輸入資料
		if("".equals(name_edit.getText().toString().trim())){
			Toast.makeText(v.getContext(), "請輸入姓名", Toast.LENGTH_LONG).show();
		}else if(10!=phone_edit.getText().toString().trim().length()){
			Toast.makeText(v.getContext(), "手機號碼格式不正確", Toast.LENGTH_LONG).show();
		}else if(3>address_edit.getText().toString().trim().length()){
			Toast.makeText(v.getContext(), "地址格式不正確", Toast.LENGTH_LONG).show();
		}else {
			Woo_name = name_edit.getText().toString().trim();
			Woo_phone = phone_edit.getText().toString().trim();
			Woo_add = address_edit.getText().toString().trim();
			Woo_ps = ps_edit.getText().toString().trim();

			updata(Woo_name, Woo_phone, Woo_add, Woo_ps);
			savebt.setEnabled(false);//進入非同步任務前先關閉按鈕，避免二次執行非同步任務
			task.execute();
			it2 = new Intent(this, UserActivity.class);
			it2.putExtra("Woo_gmail", Woo_gmail);
			startActivity(it2);
			finish();
		}
	}

	protected void getInt() {
		it1 = getIntent();
		Woo_gmail = it1.getStringExtra("Woo_gmail");
		Woo_name = it1.getStringExtra("Woo_name");
		Woo_add = it1.getStringExtra("Woo_add");
		Woo_phone = it1.getStringExtra("Woo_phone");
		Woo_ps=it1.getStringExtra("Woo_ps");
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

	AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

		@Override
		protected Void doInBackground(Void... params) {

			try{
				Class.forName("com.mysql.jdbc.Driver");
				con = DriverManager.getConnection(
						"jdbc:mysql://"+ ip + "/" + dbName + "?useUnicode=true&characterEncoding=UTF-8", sqldbaccount,
						sqldbpass);
				String selectSQL = "update woo_table  set Woo_name = '" + Woo_name + "',Woo_phone = '" + Woo_phone +  "', Woo_add= '" + Woo_add +"', Woo_ps= '" + Woo_ps +"' where Woo_gmail = '" + Woo_gmail + "'";
				PreparedStatement preparedStmt = con.prepareStatement(selectSQL);
				preparedStmt.executeUpdate();
				preparedStmt.close();
				con.close();
			}
			catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return null;

		}
		//-----------------------------------------------------------------
	};


	private void updata(String name, String phone, String address, String ps) {
		ContentValues cv=new ContentValues(4);
		cv.put(FROM[0], name);
		cv.put(FROM[1], phone);
		cv.put(FROM[2], address);
		cv.put(FROM[3], ps);

		db.update(TB_NAME, cv, "gmail="+"'"+Woo_gmail+"'", null);//更新 id 所指的欄位
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) { // 判斷是否按下返回鍵
			Intent it=new Intent(this, UserActivity.class);
			startActivity(it);
			finish();
		}
		return false;
	}

	@Override
	public void onClick(View view) {
		Intent it=new Intent(this, UserActivity.class);
		startActivity(it);
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dbhelper.close();
	}
}
