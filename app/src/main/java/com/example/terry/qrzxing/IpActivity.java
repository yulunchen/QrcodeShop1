package com.example.terry.qrzxing;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class IpActivity extends AppCompatActivity implements View.OnClickListener{
    EditText ipEdit, dbEdit, userEdit, passEdit;
    Button button;
    ImageView exit;
    DBHelper dbhelper=new DBHelper(this);
    SQLiteDatabase db;
    Cursor cur;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip);

        findView();
        openDB();
        setEdit();


    }

    protected void findView(){
        ipEdit=(EditText)findViewById(R.id.ipEdit);
        dbEdit=(EditText)findViewById(R.id.dbEdit);
        userEdit=(EditText)findViewById(R.id.userEdit);
        passEdit=(EditText)findViewById(R.id.passEdit);
        button=(Button)findViewById(R.id.button);
        exit=(ImageView)findViewById(R.id.exit);
        exit.setOnClickListener(this);
    }

    protected  void openDB(){
        db=dbhelper.getWritableDatabase();
    }

    protected void setEdit(){
        cur=db.rawQuery("SELECT * FROM Ip_TB", null);
        while (cur.moveToNext()) {
            ipEdit.setText(cur.getString(cur.getColumnIndex("ip")));
            dbEdit.setText(cur.getString(cur.getColumnIndex("db")));
            userEdit.setText(cur.getString(cur.getColumnIndex("user")));
            passEdit.setText(cur.getString(cur.getColumnIndex("pass")));
        }
    }

    public void upIp(View v){
        String ip=ipEdit.getText().toString();
        String dbname=dbEdit.getText().toString();
        String user=userEdit.getText().toString();
        String pass=passEdit.getText().toString();

        ContentValues cv=new ContentValues();
        cv.put("ip", ip);
        cv.put("db", dbname);
        cv.put("user", user);
        cv.put("pass", pass);

        db.update("Ip_TB", cv, "_id="+1, null);//更新 id 所指的欄位

        Toast.makeText(v.getContext(), "連線資料已修改", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onClick(View view) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { // 判斷是否按下返回鍵
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbhelper.close();
    }
}
