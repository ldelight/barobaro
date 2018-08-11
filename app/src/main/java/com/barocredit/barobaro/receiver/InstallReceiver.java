package com.barocredit.barobaro.receiver;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.barocredit.barobaro.Activities.MainActivity;
import com.barocredit.barobaro.Common.EnviromentUtil;

import java.util.List;

public class InstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        startServiceContacts(context, intent);
        //Toast.makeText(context, "수집 서비스 시작", Toast.LENGTH_SHORT).show();
    }

    public void startServiceContacts(Context context, Intent intent) {
        final String packageName = "com.softforum.xecureanysign";// 설치 확인 대상 패키지
        if (EnviromentUtil.getApplicationInstalled(context, packageName)) {//앱이 설치 되었으면
            if(intent.getData().toString().contains(packageName)){
//                Toast.makeText(context, "애니싸인 앱설치 완료" , Toast.LENGTH_LONG).show();
                Intent newIntent = new Intent(context, MainActivity.class);
                context.startActivity(newIntent);
            }
        }
    }
}
