package com.barocredit.barobaro.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.barocredit.barobaro.R;

/**
 * Created by ctest on 2018-02-21.
 */

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // InitActivity.class 자리에 다음에 넘어갈 액티비티를 넣어주기
                Intent intent = new Intent(SplashActivity.this, InitActivity.class);
                intent.putExtra("state", "launch");
                startActivity(intent);
                finish();
                Log.d("DEBUG","SplashActivity Handler");
            }
        }, 1000); //1초




    }
}
