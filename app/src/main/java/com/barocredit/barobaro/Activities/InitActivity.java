package com.barocredit.barobaro.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NativeActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.barocredit.barobaro.Common.Constants;
import com.barocredit.barobaro.Common.EnviromentUtil;
import com.barocredit.barobaro.Common.MessageUtil;
import com.barocredit.barobaro.R;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nprotect.IxSecureManager;
import com.nprotect.security.inapp.IxSecureManagerHelper;
import com.softforum.xas.XecureAppShield;
import com.softforum.xecure.XecureSmart;
import com.softforum.xecure.util.EnvironmentConfig;
import com.softforum.xecure.util.XCoreWrapperAsyncCaller;
import com.softforum.xecure.util.XCoreWrapperAsyncParam;
import com.softforum.xecurekeypad.XKConstants;
import com.softforum.xecurekeypad.XKEditText;
import com.softforum.xecurekeypad.XKKeypadCustomInterface;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
//import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static com.softforum.xecure.ui.crypto.SignCertSelectWindow.mSignOption;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
/**
 * Created by ctest on 2018-02-22.
 */

public class InitActivity extends Activity {



    String mErrorMessage = new String();

    private ProgressDialog dialog;
    private DoProgress doProgress;

    //2 차검증 관련 변수
    String mURL = XecureAppShield.getInstance().getXASDomain() + "jsp/SecondaryAuthenticationSample.jsp";
    String mResponseCode = new String ();
    String mResponseMessage = new String ();
    private Context mContext = this;
    private Activity mActivity;

    //nProtect 변수
    public static final int REQUEST_CODE_SPLASH = 1;
    private IxSecureManagerHelper mSecureMngHelper = IxSecureManagerHelper.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if( Constants.isTest){ // 테스트
            FirebaseMessaging.getInstance().subscribeToTopic("barobaroloanTest");
        }else{                  // 운영
            FirebaseMessaging.getInstance().subscribeToTopic("barobaroloan");
        }
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        setContentView(R.layout.activity_init);
        mContext = this.getApplicationContext();
        mActivity = this;
        initSecure();// 백신 초기화
        startSecure();// 백신 시작
//        goMainActivity(); //for testing


    }


    private void startSecure() {

        dialog = new ProgressDialog(InitActivity.this);
        dialog.setMessage("악성코드 검사 중입니다.");

        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            public void onCancel(DialogInterface dialog) {
                doProgress.cancel(true);
            }
        });
//        dialog.show();

        mSecureMngHelper = IxSecureManagerHelper.getInstance();
        mSecureMngHelper.setShowSplash(false);
        mSecureMngHelper.setUseSplashActivity(false, REQUEST_CODE_SPLASH);
        IxSecureManagerHelper.IxSecureEventListener secureEvtListener = new IxSecureManagerHelper.IxSecureEventListener() {
            @Override
            public void onStatusChanged(int status) {
                // 상태 변경
                switch(status){
                    case IxSecureManagerHelper.STATUS_SERVICE_END:  // 서비스 완료
                        setStatusText("서비스 완료");
//                        dialog.dismiss();
                        break;
                    case IxSecureManagerHelper.STATUS_SERVICE_START:    // 서비스 시작
                        setStatusText("서비스 시작");
                        break;
                    case IxSecureManagerHelper.STATUS_UPDATE_START: // 업데이트 시작
                        setStatusText("업데이트 시작");
                        break;
                    case IxSecureManagerHelper.STATUS_UPDATE_FINISH:    // 업데이트 완료
                        setStatusText("업데이트 완료");
                        break;
                    case IxSecureManagerHelper.STATUS_SCAN_START:   // 악성코드 검사 시작
                        setStatusText("악성코드 검사 시작");
                        break;
                    case IxSecureManagerHelper.STATUS_SCAN_FINISH:  // 악성코드 검사 완료
                        setStatusText("악성코드 검사 완료");
                        // XecureAppShield 체크
                        appIntegrityVerify();
                        break;
                    case IxSecureManagerHelper.STATUS_UPDATE_TIMEOUT:   // 업데이트 타임아웃
                        setStatusText("업데이트 타임아웃");
                        break;
                }
            }

            @Override
            public void onMalwareFound(int malwareCount) {
                // 악성 코드 발견 횟수
                if(malwareCount > 0){
                    setStatusText("악성 코드가 " + malwareCount + "건 발견되었습니다.");
                    secureStop();
                    finishApp("악성 코드가 " + malwareCount + "건 발견되어 앱을 종료합니다.");
                }
            }

            @Override
            public void onRealtimeMalwareFound(String pkgName) {
                // 악성 코드 명
                setStatusText("악성 코드 " + pkgName + "이(가) 발견되었습니다.");
                secureStop();
                finishApp("악성 코드가 " + pkgName + "이(가) 발견되어 앱을 종료합니다.");
            }
        };

        mSecureMngHelper.setEventListener(secureEvtListener); // 이벤트 리스너 설정
        mSecureMngHelper.start(mActivity);     // 백신 서비스 시작
    }
    public void secureStop(){
//        mSecureMngHelper = IxSecureManagerHelper.getInstance();
        mSecureMngHelper.stop(mActivity);
    }

    public void finishApp(String message){
        MessageUtil.alertDialog(mActivity, message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }, false);
    }

    private void setStatusText(String text){
        TextView tv_status = (TextView)findViewById(R.id.tv_status);
        tv_status.setText("" + text);
    }

    private void initSecure() {
        IxSecureManager.initLicense(getApplicationContext(), Constants.NPROTECT_LICENSE_KEY, Constants.NPROTECT_USER_ID);
//        mSecureMngHelper= IxSecureManagerHelper.getInstance();
    }

    public void onResume() {
        super.onResume();
        //백신 체크
//        EnviromentUtil.installApp(mContext, mActivity);
    }

    private void appIntegrityVerify()
    {
        //InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        //키보드를 없앤다.
        //imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);

        /** XAS의 context를 설정한다.
         *
         *  @param string 		도메인
         *  @param string 		App 아이디
         *  @param String 		App 버전
         *  @param boolean 	라이브업데이트 사용여부
         */
        XecureAppShield.getInstance().setXASContext(
                Constants.XAS_DOMAIN ,
                Constants.XAS_APPID,
                Constants.XAS_APPVER,
                Constants.XAS_LIVE_UPDATE
        );
        doProgress = new DoProgress();
        doProgress.execute();
    }

    private class DoProgress extends AsyncTask<Integer, Void, Integer> {


        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(InitActivity.this);
            dialog.setMessage("무결성 검증을 진행중입니다.");

            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    doProgress.cancel(true);
                }
            });
//            dialog.show();

            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Integer... params) {

			/*
			 *  App무결성 검증을 수행한다.
			 */
            boolean isDebuggable = ( 0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
            Log.d("XecureAppShield", "is Debuggable? " + isDebuggable);
            Log.d("XecureAppShield", "------------  checkApp execute  ------------");

			/*
			 *  사용자 식별정보 :
			 *  - 무결성 검증 결과에 대한 사용자 식별정보가 있다면 전달한다.
			 *  - null일 경우 default 식별정보가 전달되며
			 *  - 1차 사용자 폰번호 수집
			 *  - 실패시 2차 IMEI수집
			 *  - 모두 실패시 식별정보는 수집되지 않는다.
			 */

            String aIdentifier = null;

            Log.d("XecureAppShield", "lib path = " + getApplicationContext().getApplicationInfo().nativeLibraryDir);
            int result = XecureAppShield.getInstance().checkApp(getApplicationContext(), aIdentifier);
            if( result == XecureAppShield.getInstance().UPDATE_CODE){
                Log.d("XecureAppShield", "------------  XAS_SO_UPDATE  ------------");
                this.publishProgress();

				 /*
				  *  so 모듈 다운로드 url 로그 및 라이브업데이트 수행
				  */
                Log.d("XecureAppShield", "URL : " + XecureAppShield.getInstance().getUpdateURL());
                result  = XecureAppShield.getInstance().SOUpdate(XecureAppShield.getInstance().getUpdateURL());
                mErrorMessage = XecureAppShield.getInstance().GetErrorMsg(result);

				 /*
				  * 라이브업데이트 결과 로그 및 UPDATE_RESULT Instance 변수에 결과를 알려준다.
				  */
                Log.d("XecureAppShield", "XAS_SO_UPDATE_RESULT : " + result);
                Log.d("XecureAppShield", "XAS_SO_UPDATE_MESSAGE : " + mErrorMessage);

                if(result== XecureAppShield.getInstance().SUCCESS_CODE){
                    XecureAppShield.getInstance().UPDATE_RESULT = true;
                }else{
                    Log.d("XecureAppShield", "------------  XecureAppShield XAS_SO_UPDATE  ------------" );
                }
            }

            return	result;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            dialog.setMessage("보안모듈 업데이트를 진행중입니다");
        }

        @Override
        protected void onPostExecute(Integer result) {

			/*
			 * UPDATE_RESULT Instance 변수값이 성공이면
			 * 반드시 앱을 완전히 종료후 리스타트 해줘야한다.
			 */

            if(XecureAppShield.getInstance().UPDATE_RESULT){
                Intent mainIntent = new Intent(InitActivity.this, InitActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP  );
                startActivity(mainIntent);
                destroy();
            }

			/*
			 * 앱 무결성 검증 결과 출력 및 성공일 경우
			 * 토큰 검증을 위한 인터페이스를 노출한다.
			 */
            mErrorMessage = XecureAppShield.getInstance().GetErrorMsg(result);

            Log.d("XecureAppShield", "XAS_RESULT : " + result);
            Log.d("XecureAppShield", "XAS_MESSAGE : " + mErrorMessage);

//            Toast.makeText(getApplicationContext(), "<" + result + "> :" + mErrorMessage, Toast.LENGTH_LONG).show();

            TextView tv_status = (TextView)findViewById(R.id.tv_status);


            dialog.dismiss();
            doProgress.cancel(true);

//            TextView Text = (TextView)findViewById(R.id.integrity_check_textview);
//            Text.setText(result+ "\n" + mErrorMessage);

    		/*
    		 * 무결성 검증이 성공이면 token 검증수행을 위한 버튼출력
    		 */
            if(result == XecureAppShield.getInstance().SUCCESS_CODE)
            {
//                Button btn = (Button)findViewById(R.id.token_button);
//                btn.setVisibility(View.VISIBLE);
//                무결성 검증 성공이면 토큰을 받아서 다음동작
//                XecureAppShield.getInstance().getToken();
//                Toast.makeText(getApplicationContext(), "<" + result + "> :" + XecureAppShield.getInstance().getToken() + " checkToken :" +checkToken (), Toast.LENGTH_LONG).show();
//                checkToken (); // 2차 검증은 비즈서버쪽에서 구현해 줘야 함 추후(;;)
                tv_status.setText("무결성 검증이 완료 되었습니다." );
                goMainActivity();
            }else {
//                goMainActivity(); // for Testing

                tv_status.setText("'"+mErrorMessage + "' 으로 확인 되었습니다.");
                MessageUtil.alertDialog(mActivity, "'"+mErrorMessage + "' 으로 확인되어 앱을 종료합니다.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }, false);

            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        private void destroy() {
            android.util.Log.d("XecureAppShield", " Destroy ___________________________");
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }


    public boolean checkToken ()
    {
        boolean aResult = false;
        String aResultString = null;
        ArrayList<NameValuePair> aNameValuePairs = null;
        HttpClient aHttpClient = null;
        HttpParams aParams = null;
        HttpPost aHttpPost = null;
        UrlEncodedFormEntity aRequest = null;
        HttpResponse aResponse = null;
        Header aHeader[] = null;

        aHttpClient = new DefaultHttpClient();


        try {
            // 파라미터 셋팅
            aNameValuePairs = new ArrayList<NameValuePair>();

            Log.d ("XecureAppShield", this.mURL);
            Log.d ("XecureAppShield", "SessionID = " + XecureAppShield.getInstance().getSID());
            Log.d ("XecureAppShield", "Token = " + XecureAppShield.getInstance().getToken());
//            Log.d ("XecureAppShield", "USER_ID = " + "");
//            Log.d ("XecureAppShield", "USER_PW = " + "");

            aNameValuePairs.add(new BasicNameValuePair("session_id", XecureAppShield.getInstance().getSID()));
            aNameValuePairs.add(new BasicNameValuePair("token", XecureAppShield.getInstance().getToken()));

            // 타임아웃 처리 : 응답시간 5초
            aParams = aHttpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(aParams, 5000);
            HttpConnectionParams.setSoTimeout(aParams, 5000);

            // 서버에 OTP요청 수행 : 요청에 대한 결과는 handleResponse()가 처리함
            aHttpPost = new HttpPost(this.mURL);
            aRequest = new UrlEncodedFormEntity(aNameValuePairs, HTTP.UTF_8);
            aHttpPost.setEntity(aRequest);
            aResponse = aHttpClient.execute(aHttpPost);

            aHeader = aResponse.getAllHeaders();
            aResultString = null;

            for(int i=0; i<aHeader.length;i++)
            {
                if(aHeader[i].getName().equals("checkResult")){
                    aResultString = aHeader[i].getValue();
                }else if(aHeader[i].getName().equals("code")){
                    mResponseCode = aHeader[i].getValue();
                }else if(aHeader[i].getName().equals("msge")){
                    mResponseMessage = aHeader[i].getValue();
                }
            }
            Log.d ("XecureAppShield", "aResultString = " + aResultString);
            if(aResultString.equals("OK")){
                aResult = true;
            }else{
                aResult = false;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return aResult;
    }

    public void goMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("state", "launch");
        startActivity(intent);
        finish();
    }
}
