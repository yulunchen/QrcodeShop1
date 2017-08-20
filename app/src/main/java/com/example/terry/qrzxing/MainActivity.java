package com.example.terry.qrzxing;

import android.accounts.Account;
import android.accounts.AccountManager;
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

public class MainActivity extends AppCompatActivity implements OnItemSelectedListener, IPaddress, View.OnClickListener {
	Spinner account_sp;
	Button start;
	String vipEmail, selEmail;
	ImageView exit;
	Connection con;
	Statement stmt;
	ResultSet rs;

	Intent it1, it2;
	int flag_a, flag_b;
	static final String DB_NAME = "Vip_DB";//資料庫
	static final String Vip_TB = "Vip_TB";//會員資料表
	static final String Shop_TB = "Shop_TB";//購物車資料表
	SQLiteDatabase db;
	Cursor cur;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getSupportActionBar().setLogo(R.mipmap.ic_launcher_round);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		View v = findViewById(R.id.LinearLayout1);
		v.getBackground().setAlpha(200);//0~255透明度值

		myView();// 執行頁面設定
		mySpinner();// 執行Spinner
		myListener();// 執行監聽器
		sqlset();//執行內部資料庫設定

	}

	protected void myView() {// 頁面設定方法
		account_sp = (Spinner) findViewById(R.id.account_sp);
		start = (Button) findViewById(R.id.start);
		exit=(ImageView)findViewById(R.id.exit);
	}


	protected void mySpinner() {// Spinner設定
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


	protected void myListener() {// 註冊監聽器
		account_sp.setOnItemSelectedListener(this);
		exit.setOnClickListener(this);
	}

	// 取得在Spinner中所選取的帳號並轉成String型態
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		selEmail = (String) account_sp.getSelectedItem();
	}

	protected void sqlset(){
		db = openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
		// 建立會員資料表
		String createTable1 = "CREATE TABLE IF NOT EXISTS " + Vip_TB + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + // 索引欄位
				"gmail VARCHAR(60), " + "name VARCHAR(32), " + "phone VARCHAR(32), " + "address VARCHAR(50), "+ "ps VARCHAR(200))";
		db.execSQL(createTable1);

		String del="DELETE FROM "+Vip_TB;
		db.execSQL(del);

		//建立購物車資料表
		String createTable2 ="CREATE TABLE IF NOT EXISTS " + Shop_TB +
				"(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +//會自動增加的流水號，一定要有
				"goods VARCHAR(32), " +//商品名稱欄位
				"price  INTEGER(32), " +//商品價格欄位
				"quantity  INTEGER(32))";//商品數量欄位
		db.execSQL(createTable2);
	}


	// 開始鈕
	public void start(View v) {
		// 取的網路連線狀態
		// 必須在AndroidManifest.xml新增NETWORK權限
		ConnectivityManager cManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cManager.getActiveNetworkInfo();
		if (info != null && info.isAvailable()) {// 有連上網路時
			nextActivity();// 執行Intent物件設定
			start.setEnabled(false);//進入非同步任務前先關閉按鈕，避免二次執行非同步任務
			task.execute(selEmail);// 執行非同步任務並帶入帳號字串
		} else {// 沒連上網路時
			Toast.makeText(v.getContext(), "沒有連上網路", Toast.LENGTH_LONG).show();
		}

	}

	// Intent設定
	protected void nextActivity() {
		// 設定目的地為UserActivity頁面，並放入帳號字串資料
		it1 = new Intent(this, LineActivity.class);
		flag_b=1;
		it1.putExtra("Woo_gmail", selEmail);
		it1.putExtra("flag",flag_b);
		// 設定目的地為RegistActivity頁面，並方入帳號字串資料
		it2 = new Intent(this, RegisterActivity.class);
		it2.putExtra("Woo_gmail", selEmail);
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

			if (flag_a == 1) {
				// 已註冊帳號開啟UserActivity頁面
				startActivity(it1);
				// 關閉本頁
				finish();
			} else{
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
}
