package com.example.terry.qrzxing;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends AppCompatActivity implements IPaddress {
    ImageView img;
    Intent it1, it2 ,it3, it4;
    Button Car,Qrzxing,list,hello;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher_round);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Car= (Button)findViewById(R.id.Car);
        Qrzxing=(Button)findViewById(R.id.Qrzxing);
        list=(Button)findViewById(R.id.list);
        hello=(Button)findViewById(R.id.hello);
        //與View上的ImageView作關聯
        img=(ImageView)findViewById(R.id.img);

        //設定ImageView上所要顯示的圖片群
        //圖片群的設定要在res/drawable-mdpi下新增一個xml來寫設定
        //本範例設定在res/drawable-mdpi/animation_xml
        img.setBackgroundResource(R.drawable.animation_xml);

        //建立AnimationDrawable圖檔動畫物件
        AnimationDrawable animationDrawable=
                (AnimationDrawable) img.getBackground();

        //啟動圖檔動畫效果
        animationDrawable.start();


    }


    public void san(View v) {
        // 設定目的地為UserActivity頁面，並放入帳號字串資料
        it1 = new Intent(this, SignActivity.class);
        startActivity(it1);
        finish();
    }

    public void shop(View v) {
        // 設定目的地為UserActivity頁面，並放入帳號字串資料
        it2 = new Intent(this, ShopActivity.class);
        startActivity(it2);
        finish();
    }

    public void send(View v) {
        // 設定目的地為UserActivity頁面，並放入帳號字串資料
        it3 = new Intent(this, OrderActivity.class);
        startActivity(it3);
        finish();
    }
    public void welcome(View v) {
        // 設定目的地為UserActivity頁面，並放入帳號字串資料
        it4 = new Intent(this, UserActivity.class);
        startActivity(it4);
        finish();
    }

    //自訂返回鍵的方法
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

}



