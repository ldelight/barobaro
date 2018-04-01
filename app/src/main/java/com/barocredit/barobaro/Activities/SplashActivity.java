package com.barocredit.barobaro.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.barocredit.barobaro.R;
import com.softforum.xecure.XApplication;
import com.softforum.xecure.util.EnvironmentConfig;
import com.softforum.xecure.util.XSLog;
import com.softforum.xecure.util.XUtil;
import com.softforum.xecurecertshare.client.XCSInitialize;
import com.softforum.xecurekeypad.XKCoreWrapperToJni;

/**
 * Created by ctest on 2018-02-21.
 */

public class SplashActivity extends Activity {

    final static int RESOURCE_UPDATE_DIALOG = 1;
    protected 	int	 mSplashTime = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (!isTaskRoot()) {
            final Intent intent = getIntent();

            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(intent.getAction())) {
                //Log.w(LOG_TAG, "Main Activity is not the root.  Finishing Main Activity instead of launching.");
                finish();

                return;
            }
        }
        setContentView(R.layout.activity_splash);

        //APP 로그레벨 설정.
        XSLog.setLogLevel(EnvironmentConfig.mLogLevel);


        ////////////////////////////////////////////////////////////
        // XecureSmart / XecureCertShare / XecureKeypad 초기화 API 설정
        ////////////////////////////////////////////////////////////

        // XecureCertShare 라이브러리 로드.
        XCSInitialize aXCSInitialize = null;

        try
        {
            aXCSInitialize = new XCSInitialize ();
            aXCSInitialize.setPackageName (getPackageName ());
            aXCSInitialize.initializeLibrary ();
        }
        catch (UnsatisfiedLinkError aLinkError)
        {
            XSLog.e("[XecureSmart][libXCSAndroid.so load failed.]");
        }

        // XecureKeypad 라이브러리 로드.
        try
        {
            XKCoreWrapperToJni.initializeLibrary (SplashActivity.this);
        }
        catch (UnsatisfiedLinkError aLinkError)
        {
            XSLog.e("[XecureSmart][libXKCore_jni.so load failed.]");
        }

        // XecureSmart 라이브러리 로드.
        final boolean aNeedUpdate = XUtil.checkResourcesUpdate(getApplicationContext());

        if(aNeedUpdate) {
            showDialog(RESOURCE_UPDATE_DIALOG);
        }

        //initializeXecureCoreConfig를 새로운 Thread로 실행
        new Thread() {
            public void run() {

                if(aNeedUpdate) {
                    XUtil.initializeXecureCoreConfig(getApplicationContext(), false);
                    //XUtil.initializeXecureCoreConfig(getApplicationContext());
                    dismissDialog(RESOURCE_UPDATE_DIALOG);

                } else {
                    //Splash Screen 지연
                    try {
                        Thread.sleep( mSplashTime );
                    } catch (InterruptedException e) {
                        XSLog.e("InterruptedException in thread");
                    }
                }

                Context aContext = XApplication.getContext();

                //core 의 로그레벨을 설정한다.
                XUtil.setLogLevel(EnvironmentConfig.mLogLevel);

                //이후 필요한 작업을 계속 수행
                postInitialize();
            }
        }.start();
        /*

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
        }, mSplashTime); //2초
*/
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        Dialog aResult = null;

        switch(id) {
            case RESOURCE_UPDATE_DIALOG :
                ProgressDialog aProgressDialog = new ProgressDialog(this, R.style.ResourceUpdateDialog);
                aProgressDialog.setMessage( getString( R.string.resource_initialize ) );
                aProgressDialog.setIndeterminate(true);
                aProgressDialog.setCancelable(false);

                aResult = aProgressDialog;
        }

        return aResult;
    }

    @Override
    public void onBackPressed() {

		/*
		 * StartScreen에서 Back 버튼을 누르는 경우 아무런 동작도 하지 않도록 막음
		 * (Thread로 인한 의도하지 않은 동작 제한)
		 */
    }

    //
    private void postInitialize () {

        Intent aIntent = new Intent(this, InitActivity.class);

        startActivity(aIntent);

        finish();
    }
}
