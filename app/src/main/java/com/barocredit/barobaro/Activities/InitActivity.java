package com.barocredit.barobaro.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.barocredit.barobaro.R;

/**
 * Created by ctest on 2018-02-22.
 */

public class InitActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        // XecureAppShield 체크
        // XecureSmart 체크
        // MainActivity.class 자리에 다음에 넘어갈 액티비티를 넣어주기
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("state", "launch");
        startActivity(intent);
        finish();

    }

}
