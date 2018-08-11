package com.barocredit.barobaro.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.barocredit.barobaro.Common.Constants;
import com.barocredit.barobaro.Common.BaroChromeClient;
import com.barocredit.barobaro.Common.EnviromentUtil;
import com.barocredit.barobaro.Common.RealPathUtil;
import com.barocredit.barobaro.R;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.appinvite.FirebaseAppInvite;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.gun0912.tedpermission.PermissionListener;

import com.gun0912.tedpermission.TedPermission;
import com.softforum.xecure.XecureSmart;
import com.softforum.xecure.util.XCoreWrapperAsyncCaller;
import com.softforum.xecure.util.XCoreWrapperAsyncParam;
import com.softforum.xecurekeypad.XKEditText;
import com.nprotect.security.inapp.IxSecureManagerHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.softforum.xecure.ui.crypto.SignCertSelectWindow.mSignOption;

public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    //기본 오브젝트
    WebView mWebView;
    private final Handler handler = new Handler();
    Activity activity = this;;

    //파일 업로드
    private String mCameraPhotoPath;
    private ValueCallback<Uri[]> mFilePathCallback;
    private ValueCallback<Uri> mUploadMessage;

    // BACK 2번 클릭 시 종료 핸들러. 플래그
    private Handler mHandler  = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            mFlag = false;
        }
    };
    private boolean mFlag = false;

    // 웹뷰 로딩 관련
    ProgressDialog mProgress;
    private static final int INPUT_FILE_REQUEST_CODE = 1; //파일업로드 관련

    //
    private XecureSmart mXecureSmart;

    //
    //
    private String mPlainMsg;
    private String mSignedMsg;
    private String mVidInfoMsg;

    /*
    private String mEncMsg;
    private String mDecMsg;
    private String mEnc_q_Msg;
    private String mEnc_p_Msg;


    */
    private String mXgateAddr = "1.237.174.154:3028:3029";

    //////////////////////////////////////////////////////
    // [START] Layout 내 반환할 컨트롤 객체 변수 선언
    //////////////////////////////////////////////////////
    // for EditText
    //

    private EditText mEdit_plainMsg;				// edt_plain_msg
//    private EditText mEdit_signedMsg;				// edt_signed_msg
//    private EditText mEdit_vidInfoMsg;				// edt_vidInfo_msg
    private EditText mEdit_full_idn;				// edt_full_idn
    private EditText mEdit_result_verifier_sign;	// edt_verifier_sign
    private EditText mEdit_result_verifier_vid;		// edt_verifier_vid
    //
    private EditText mEdit_plainMsg2;				// edt_plain_msg02
    private EditText mEdit_enc_q_msg;				// edt_block_enc_q_msg
    private EditText mEdit_enc_p_msg;				// edt_block_enc_p_msg
    private EditText mEdit_block_dec_msg;			// edt_block_dec_msg

    // for Button
    //
    private Button mBtn_signData;					// btn_sign_data
    private Button mBtn_getSubmit;					// btn_GenSubmit
    //
    private Button mBtn_cert_change_password;		// btn_cert_change_password
    private Button mBtn_cert_delete;				// btn_cert_delete
    private Button mBtn_mobile_import;				// btn_import_mobile
    private Button mBtn_mobile_export;				// btn_export_mobile
    //
    private Button mBtn_blockEnc;					// btn_block_enc
    private Button mBtn_blockDec;					// btn_block_dec

    // for XKEditText (XecureKeypad)
    //
    private XKEditText mXKEdit_full_idn;			// edt_xk_full_idn (IDN-식별번호(ex. 주민번호) 입력)
    /**/
    // [END]
    //////////////////////////////////////////////////////

    //
    private String mAcceptCert = "yessignCA,yessignCA Class 1,yessignCA Class 2" +		// 금융결제원 인증서
            ",signGATE CA,signGATE CA2,signGATE CA4,signGATE CA5,signGATE FTCA04" +		// 한국정보인증 인증서
            ",CrossCert Certificate Authority,CrossCertCA,CrossCertCA2,CrossCertCA3" +	// 한국전자인증 인증서
            ",SignKorea CA,SignKorea CA2,SignKorea CA3" +								// 증권전산원 인증서
            ",NCASignCA,NCASign CA" + 													// 한국전산원 인증서
            ",TradeSignCA,TradeSignCA2,TradeSignCA3"; 									// 한국무역정보통신 인증서

    String mPEMVal = "";

//    private int mSignOption = 276;   // (4+16+256)
    private int mSignOption = 12;   // (4+16+256)

    String hostUrl = Constants.XAS_BASE_DOMAIN;
    String hostPort = "80";

    Context mContext;
    AppEventsLogger logger ; // 페이스북 관련


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        logger = AppEventsLogger.newLogger(mContext);
//        checkPermission();
        //웹뷰 초기화 및 설정
        initWebView();
//        setNativeSetting(); //for Testing
        //애니싸인 설치 체크
        EnviromentUtil.installAnySign(mContext);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        analyticsFirebase();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
//        FirebaseDynamicLinks.getDynamicLink();
        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData data) {
                        if (data == null) {
                            Log.d("DEBUG", "getInvitation: no data");
                            return;
                        }

                        // Get the deep link
                        Uri deepLink = data.getLink();

                        // Extract invite
                        FirebaseAppInvite invite = FirebaseAppInvite.getInvitation(data);
                        if (invite != null) {
                            String invitationId = invite.getInvitationId();
                        }

                        // Handle the deep link
                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DEBUG", "getDynamicLink:onFailure", e);
                    }
                });
    }


    private void initWebView() {
        mWebView = (WebView)findViewById(R.id.webView);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setAppCacheEnabled(false);
        mWebView.clearCache(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setGeolocationEnabled(true);
        String userAgent = mWebView.getSettings().getUserAgentString();
        mWebView.getSettings().setUserAgentString(userAgent + " " + Constants.USER_AGENT_STRING);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                Log.d("url", "should overload url["+url+"]");
                analyticsFirebase(url);
                analyticsFacebook(url);
                if (url.startsWith("intent://")) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        Intent existPackage = getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                        if (existPackage != null) {
                            startActivity(intent);
                        } else {
                            Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                            marketIntent.setData(Uri.parse("market://details?id=" + intent.getPackage()));
                            startActivity(marketIntent);
                        }
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else if (url.startsWith("tel:")) {
                    //tel:01000000000
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(intent);
                    return true;
                } else if (url.startsWith("mailto:")) {
                    //mailto:ironnip@test.com
                    Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                    startActivity(i);
                    return true;
                }  else {
                    Log.d("DEBUG","TEST:"+url);

                    mProgress = new ProgressDialog(activity);
                    mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgress.setMessage("연결중입니다....");
                    mProgress.setCancelable(false);
                    mProgress.show();
                    view.loadUrl(url);
                    if (mProgress.isShowing()) {
                        mProgress.dismiss();
                    }
                }
                return false;
            }


            // 웹페이지 로딩이 시작할 때 처리
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (mProgress == null) {
                    mProgress = new ProgressDialog(activity);
                    mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    //mProgress.setTitle("Loading...");
                    mProgress.setMessage("연결중입니다....");
                    mProgress.setCancelable(false);
                    mProgress.show();
                }
            }

            //웹페이지 로딩중 에러가 발생했을때 처리
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (mProgress.isShowing()) {
                    mProgress.dismiss();
                }
            }

            //웹페이지 로딩이 끝났을 때 처리
            @Override
            public void onPageFinished(WebView view, String url) {
                if (mProgress.isShowing()) {
                    mProgress.dismiss();
                }
            }

        });
        mWebView.addJavascriptInterface(new AndroidBridge(), "AndroidApi");
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);

        mWebView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                // 현재 시간을 msec으로 구한다.
                long now = System.currentTimeMillis();
                // 현재 시간을 저장 한다.
                Date date = new Date(now);
                SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyyMMddHHmm");

                String filename = CurDateFormat.format(date);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimetype);
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                request.addRequestHeader("User-Agent", userAgent);
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename+".xls");
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);

            }
        });

        mWebView.setWebChromeClient(new BaroChromeClient() {

            @Override
            public void onCloseWindow(WebView w) {
                super.onCloseWindow(w);
                finish();
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
                final WebSettings settings = view.getSettings();
                settings.setDomStorageEnabled(true);
                settings.setJavaScriptEnabled(true);
                settings.setAllowFileAccess(true);
                settings.setAllowContentAccess(true);
                view.setWebChromeClient(this);
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(view);
                resultMsg.sendToTarget();
                return false;
            }



            // For Android Version < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                //System.out.println("WebViewActivity OS Version : " + Build.VERSION.SDK_INT + "\t openFC(VCU), n=1");
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(Constants.TYPE_IMAGE);
                startActivityForResult(intent, Constants.INPUT_FILE_REQUEST_CODE);
            }

            // For 3.0 <= Android Version < 4.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                //System.out.println("WebViewActivity 3<A<4.1, OS Version : " + Build.VERSION.SDK_INT + "\t openFC(VCU,aT), n=2");
                openFileChooser(uploadMsg, acceptType, "");
            }

            // For 4.1 <= Android Version < 5.0
            public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
                Log.d(getClass().getName(), "openFileChooser : " + acceptType + "/" + capture);
                mUploadMessage = uploadFile;
                imageChooser();
            }

            // For Android Version 5.0+
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                System.out.println("WebViewActivity A>5, OS Version : " + Build.VERSION.SDK_INT + "\t onSFC(WV,VCUB,FCP), n=3");
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = filePathCallback;
                imageChooser();
                return true;
            }

            private void imageChooser() {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.e(getClass().getName(), "Unable to create Image File", ex);
                    }

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }

                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType(Constants.TYPE_IMAGE);

                Intent[] intentArray;
                if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                startActivityForResult(chooserIntent, Constants.INPUT_FILE_REQUEST_CODE);
            }
        });






        if(getIntent().getStringExtra("STARTURL") != null){
            mWebView.loadUrl(getIntent().getStringExtra("STARTURL"));
        }else{
            goInitPage();
        }



        // 뷰 가속
        if (Build.VERSION.SDK_INT >= 19) {
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        else {
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        // 캐시 처리 개선
        /*
        try
        {
            Method m = CacheManager.class.getDeclaredMethod("setCacheDisabled", boolean.class);
            m.setAccessible(true);
            m.invoke(null, true);
        }
        catch (Throwable e)
        {
            Log.d("myapp","Reflection failed", e);
        }
        */
        //webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);


        try {
            PackageInfo info = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("keybase:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    public void goInitPage(){
        SharedPreferences pref = getSharedPreferences("CHECKPERMISSIONURL", MODE_PRIVATE);

        //권한 얻었는지 체크
        int readPhoneStateCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);
        int cameraCheck  = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        int writeExternalStorageCheck  = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readExternalStorageCheck  = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);

        Log.d("DEBUG","EXLOG:" + readPhoneStateCheck);
        Log.d("DEBUG","EXLOG:" + cameraCheck );
        Log.d("DEBUG","EXLOG:" + writeExternalStorageCheck);
        Log.d("DEBUG","EXLOG:" + readExternalStorageCheck);

        // 권한 체크 이미 얻은 권한은 0 이외에는 -1
        // 권한이 있거나, 있거나 있거나
        if( readPhoneStateCheck == PackageManager.PERMISSION_GRANTED
                && cameraCheck == PackageManager.PERMISSION_GRANTED
                && writeExternalStorageCheck == PackageManager.PERMISSION_GRANTED
                && readExternalStorageCheck == PackageManager.PERMISSION_GRANTED
                && pref.getBoolean("ISOK", false) ){
            mWebView.loadUrl(Constants.INITURL);    // 메인 페이지
        }else{
            mWebView.loadUrl(Constants.PERMISSIONURL);  // 권한 페이지
        }
    }

    public class AndroidBridge {
        //HTML 문서 내에서 JavaScript로 호출가능한 함수
        //브라우저에서 load가 완료되었을 때 호출하는 함수

        //인증서 실행
        @JavascriptInterface
        public void callAndroidAuth(){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d("DEBUG","EXLOG:1111111111111111" );
                    setNativeSetting();
                }
            });
        }



        //접근권한 확인버튼 누를 시 실행
        @JavascriptInterface
        public void callAndroidPermissionOK(){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= 23) {
                        //마쉬멜로우 이상 버전일 경우
                        checkPermission();
                    }else{
                        //마쉬멜로우 미만일 경우
                        SharedPreferences pref = getSharedPreferences("CHECKPERMISSIONURL", MODE_PRIVATE);
                        SharedPreferences .Editor editor = pref.edit();
                        editor.putBoolean("ISOK", true);
                        editor.commit();
                        goInitPage();
                    }
                }
            });
        }

        @JavascriptInterface
        public void callAndroidDeviceInfo() {
            Log.d("regId", Constants.gcmRegId);

            handler.post(new Runnable() {
                @Override
                public void run() {
//                    mWebView.loadUrl("javascript:callClientInfo('" + Constant.gcmRegId + "')");
                }
            });
        }

        @JavascriptInterface
        public void callAndroidLocationInfo() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @JavascriptInterface
        public void callDownloadFile() {
            //webView.loadUrl("javascript:setLocation('"+ location.getLongitude()+"','"+ location.getLatitude()+"');");
            handler.post(new Runnable() {
                @Override
                public void run() {
                }
            });
        }



        @JavascriptInterface
        public void setAutoLoginYes() {
            Constants.isAutoLoginYn=true;
        }


        @JavascriptInterface
        public void setAutoLoginNo() {
            removeCookies();
        }

        @JavascriptInterface
        public void callAnySign(String urlScheme){
            mWebView.loadUrl("<script type='text/javascript'>'" + urlScheme + "';</script>");

        }

    }

    private void checkPermission() {

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
//                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show(); e.getMessage()
                SharedPreferences pref = getSharedPreferences("CHECKPERMISSIONURL", MODE_PRIVATE);
                SharedPreferences .Editor editor = pref.edit();
                editor.putBoolean("ISOK", true);
                editor.commit();
                goInitPage();
            }


            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
//                Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                finish();
            }


        };

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setRationaleTitle(R.string.rationale_title)
                .setRationaleMessage(R.string.rationale_message)
                .setDeniedTitle(R.string.denied_title)
                .setDeniedMessage(R.string.denied_message)
                .setGotoSettingButtonText("설정보기")
                .setPermissions(Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA,  Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
    }

    /**
     * More info this method can be found at
     * http://developer.android.com/training/camera/photobasics.html
     *
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        removeCookies();
    }

    public void removeCookies(){
        if (mWebView != null) {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(mWebView.getContext());
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.removeSessionCookie();
            cookieManager.removeAllCookie();
            cookieSyncManager.sync();
        }
    }



    @Override
    public void  onBackPressed() {
        //super.onBackPressed();
        if(mWebView.canGoBack()){
            mWebView.goBack();
        }else if (mWebView.getUrl().indexOf(Constants.INITURL) >= 0 ){
            if(!mFlag) {
                Toast.makeText(this, "'뒤로'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();    // 종료안내 toast 를 출력
                mFlag = true;
                mHandler.sendEmptyMessageDelayed(0, 2000);    // 2000ms 만큼 딜레이
                return;
            }else {
                finish();
            }
        }else if(mWebView.getUrl().indexOf(Constants.JOIN_URL) > 0 ) {
            return;
        }else {
            //뒤로 가기 실행
            if ( mWebView.canGoBack() ) {
                mWebView.goBack();
                return;
            }
        }
    }


    private void setNativeSetting() {
//        mEdit_signedMsg = (EditText)findViewById(R.id.result_text);
//        mEdit_vidInfoMsg = (EditText)findViewById(R.id.Edit_vidInfoMsg);
        //////////////////////////////////////////////////////////////
        // 필요 권한 허용 여부 설정
        //////////////////////////////////////////////////////////////
        // Permission Check
        int aWritePermissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int aReadPermissionCheck  = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);

        Log.d("DEBUG","EXLOG:2222222222222" );

        // 권한 없을 경우 사용자에게 권한 요청
        if(aWritePermissionCheck != PackageManager.PERMISSION_GRANTED || aReadPermissionCheck != PackageManager.PERMISSION_GRANTED) {
            // 이 권한이 필요한 이유 설명하는 부분
            // 다이얼로그 등으로 이유 설명한 뒤에 requestPermission 함수 호출하여 권한허가 요청해야 함
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //
            } else {
                // 제일 마지막 인자 값을 콜백함수에서 처리할 requestCode 값임
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
            }
        }

        Log.d("DEBUG","EXLOG:333333333333333" );
        // 서버인증서 PEM 값 호출하여 조회
        getServerCertPEM();


        //
        // XecureSmart
        mXecureSmart = XecureSmart.getInstance();
        initializeAgent();


        // XecureKeypad 설정 (주민번호 입력 용)
        // setE2E_XKeyPad();
//        callSignDataWithVIDAsync();
    }
    // 서버인증서 PEM 값 반환
    private void getServerCertPEM() {
        //
        new MainActivity.getConnectTaskServer().execute();
    }


    //
    public void initializeAgent() {
        //
        mXecureSmart.SetAttribute("id", "XecureWeb" );
        mXecureSmart.SetAttribute("type", "null" );
        //"MIIDqzCCApOgAwIBAgICDU4wDQYJKoZIhvcNAQEFBQAwgZIxCzAJBgNVBAYTAktSMR4wHAYDVQQKExVTb2Z0Zm9ydW0gQ29ycG9yYXRpb24xHjAcBgNVBAsTFVNlY3VyaXR5IFJORCBEaXZpc2lvbjEcMBoGA1UEAxMTU29mdGZvcnVtIFB1YmxpYyBDQTElMCMGCSqGSIb3DQEJARYWY2FtYXN0ZXJAc29mdGZvcnVtLmNvbTAeFw0xNzA1MTcwNjA3MzJaFw0yMjA1MTYwNjA3MzJaMFgxCzAJBgNVBAYTAktSMQ0wCwYDVQQKDAR0ZXN0MQ0wCwYDVQQLDAR0ZXN0MQ0wCwYDVQQDDAR0ZXN0MRwwGgYJKoZIhvcNAQkBFg10ZXN0QHRlc3QuY29tMIGeMA0GCSqGSIb3DQEBAQUAA4GMADCBiAKBgF7b+1hkS/PJzDAsLDBQp4n80xMknloopckKtz+43y4cmLLS9aqFS4Eir3F8hkKvrShCN2LH7eTQCX0r2mid8IaweJ6azOxeZ06KVEKOxquBGYRn4Y10bT2UU0PtxjqtZv0Ay2++uybyI6Ggf2bpIeVtUjiWorZgkcHZfhiOLWUtAgMBAAGjgcgwgcUwHwYDVR0jBBgwFoAULkmrJ4royK+XdTfei3S7JA4NJ18wHQYDVR0OBBYEFOH6c0HaTtXAih8BCl6M0YO3xnPiMAsGA1UdDwQEAwIEsDAMBgNVHRMBAf8EAjAAMGgGA1UdHwRhMF8wXaBboFmGV2xkYXA6Ly9sZGFwLnNvZnRmb3J1bS5jby5rcjozODkvY249c2Usbz1zb2Z0Zm9ydW0sYz1rcj9jZXJ0aWZpY2F0ZVJldm9jYXRpb25saXN0O2JpbmFyeTANBgkqhkiG9w0BAQUFAAOCAQEAEjbWxgm3/1Im34vrMXiZOQd0McKbyPn+0KcRjYz+dEUKLV+sf8xt3e0Enre24HYLXVLATrjlgb7ilhcNimkHAS+ct5gRjnmcCEt3ENA/TIdSexkXU92i9HM78YfAi1vPcwALk5f7LGquU0Q5kufSdtyvEm3ChP6JX8eOzbh9DhNuKOSmotFOGA7GRR7HuyVro19phEEYyvi2zERZy0VFsyJK12zajB7yeoHuwnSfJs9vokydmZNmAWiXy3edMip/H+xI0p3DqcUUqqGq/aW0cw8HDjPTtF+2aBgCviAC/RhSCy37oyBLmWd0F7U53C94D1ejvu/Co+uAhEUjCD44pw=="
        mXecureSmart.SetAttribute("license", "3082072b020101310b300906052b0e03021a05003082010706092a864886f70d010701a081f90481f6313a6d6572637572792e736f6674666f72756d2e636f2e6b723a5665726966795369676e6564446174612c46696c655369676e2c46696c655665726966792c4d756c746946696c655369676e2c46696c655369676e416e645665726966792c46696c65456e76656c6f702c46696c654465456e76656c6f702c46696c65436c6561722c46696c655a69702c46696c65556e5a69702c46696c655369676e344f454d2c4d756c746946696c655369676e344f454d2c5369676e44617461434d535769746848544d4c2c5369676e44617461434d535769746848544d4c45782c56657269667944657461636865645369676e656444617461a0820467308204633082034ba003020102020107300d06092a864886f70d01010505003077310b3009060355040613024b52311e301c060355040a1315536f6674666f72756d20436f72706f726174696f6e3121301f06035504031318536f6674666f72756d20526f6f7420417574686f726974793125302306092a864886f70d010901161663616d617374657240736f6674666f72756d2e636f6d301e170d3034303431393030303030305a170d3333303131333030303030305a308192310b3009060355040613024b52311e301c060355040a1315536f6674666f72756d20436f72706f726174696f6e311e301c060355040b1315536563757269747920524e44204469766973696f6e311c301a06035504031313536f6674666f72756d205075626c69632043413125302306092a864886f70d010901161663616d617374657240736f6674666f72756d2e636f6d30820121300d06092a864886f70d01010105000382010e00308201090282010043340b4e1f2f30d6634c818e9fa4b35c199e0628503dbe0d1f5ad2c05890a918408dc330c991083bc7cdfc50021303c04afab4cb522d22fced11d1be6559835f1f000d466120cff97a2a80e4fdf972ac127f9bb8e8ddb84974323e4cb822c5f15b22f82da3de6ef61a0b6798ca49a85af3d8f8298912b4d26411e2e1635c081a3306931716c5e56b279c4d36068a4b645c10aa582693086e14132ba67fb03526312790261f9c641993e2ffc3fd9e8df3efebfddecd722e874d6366ad1252ac0d8bddb5674533cc2717a7342e5cfb18f8a301e7196ca33d6c3bb7e1f1e4bee34f5358af6ae0fd52a9fc3bdd4925f5eab7db6628e24738f6c882bb0aaa0e10afbf0203010001a381de3081db301f0603551d2304183016801409b5e27e7d2ac24a8f56bb67accebb93f5318fd3301d0603551d0e041604142e49ab278ae8c8af977537de8b74bb240e0d275f300e0603551d0f0101ff04040302010630120603551d130101ff040830060101ff02010030750603551d1f046e306c306aa068a06686646c6461703a2f2f6c6461702e736f6674666f72756d2e636f6d3a3338392f434e3d58656375726543524c505542432c4f553d536563757269747920524e44204469766973696f6e2c4f3d536f6674666f72756d20436f72706f726174696f6e2c433d4b52300d06092a864886f70d010105050003820101003ce700a0492b225b1665d9c73d84c34f7a5faad7b397ed49231f030e4e0e91953a607bd9006425373d490ef3ba1cf47810ca8c22fabe0c609f93823efdede64744458e910267f9f857c907318e286da6c131c9dd5fada43fd8cfdf6bd1b1b239338cea83eb6b6893b88fbcfd8e86a677b7270ad96be5a82b40569efc2dda6df4bcd642d067183186d6cace6c8f73b80f30b57acb3bcd5cbbc51307922d5edb38cb0d90c3917a8e37534183ba10f403c1c034287f39442df795050f39d78ddad97da8a43f02d7641549af9b5d68908e49faa8a1597cfed4a43baadd42c8fe4fd44c96d314df56147b8a7fa6ba65ffdee9ed3a5da52ef9ac7f9ca5afb633e1ccdf318201a13082019d020101307c3077310b3009060355040613024b52311e301c060355040a1315536f6674666f72756d20436f72706f726174696f6e3121301f06035504031318536f6674666f72756d20526f6f7420417574686f726974793125302306092a864886f70d010901161663616d617374657240736f6674666f72756d2e636f6d020107300706052b0e03021a300d06092a864886f70d0101050500048201002a722081e8f73fa00f39b75247111e06e9cd1a68e822b63f0e77bc814b219eb07d63785c56b5f18de4fcaf79011297e01fad3d908841f51e33b47f6ca72e5ca17df41622246a1442380b05ff03d362b78a735509d0b30df63e08075b1cdc71f28891ed3e442eb5794486f267573c03d273e88b9ab1406a62795545f962c67cf2cf4c899f549523df0a52ed1d4649099014c4f8cd45856dab00f91c003c004cdc7ce3c2c1a4bd7f4ed609e76028252c9bd7eacb6553b91bead9a9b6e7c7766cdd45813659f43d1147d0db3837f00280f3644b11d68e870f0c306acff6130e20e644ac01f9ca26c496342080d08e06bdb58af7bba99cbafc11bc5f5dbbbae330e0" );
        mXecureSmart.SetAttribute("storage", "hard,removable,pkcs11" );
        mXecureSmart.SetAttribute("sec_option", "0:hard:iccard" );
        mXecureSmart.SetAttribute("seckey", "XW_SKS_SFVIRTUAL_DRIVER" );
        mXecureSmart.SetAttribute("sec_context", "30820647020101310b300906052b0e03021a0500302306092a864886f70d010701a01604147265617665722e736f6674666f72756d2e636f6da0820467308204633082034ba003020102020107300d06092a864886f70d01010505003077310b3009060355040613024b52311e301c060355040a1315536f6674666f72756d20436f72706f726174696f6e3121301f06035504031318536f6674666f72756d20526f6f7420417574686f726974793125302306092a864886f70d010901161663616d617374657240736f6674666f72756d2e636f6d301e170d3034303431393030303030305a170d3333303131333030303030305a308192310b3009060355040613024b52311e301c060355040a1315536f6674666f72756d20436f72706f726174696f6e311e301c060355040b1315536563757269747920524e44204469766973696f6e311c301a06035504031313536f6674666f72756d205075626c69632043413125302306092a864886f70d010901161663616d617374657240736f6674666f72756d2e636f6d30820121300d06092a864886f70d01010105000382010e00308201090282010043340b4e1f2f30d6634c818e9fa4b35c199e0628503dbe0d1f5ad2c05890a918408dc330c991083bc7cdfc50021303c04afab4cb522d22fced11d1be6559835f1f000d466120cff97a2a80e4fdf972ac127f9bb8e8ddb84974323e4cb822c5f15b22f82da3de6ef61a0b6798ca49a85af3d8f8298912b4d26411e2e1635c081a3306931716c5e56b279c4d36068a4b645c10aa582693086e14132ba67fb03526312790261f9c641993e2ffc3fd9e8df3efebfddecd722e874d6366ad1252ac0d8bddb5674533cc2717a7342e5cfb18f8a301e7196ca33d6c3bb7e1f1e4bee34f5358af6ae0fd52a9fc3bdd4925f5eab7db6628e24738f6c882bb0aaa0e10afbf0203010001a381de3081db301f0603551d2304183016801409b5e27e7d2ac24a8f56bb67accebb93f5318fd3301d0603551d0e041604142e49ab278ae8c8af977537de8b74bb240e0d275f300e0603551d0f0101ff04040302010630120603551d130101ff040830060101ff02010030750603551d1f046e306c306aa068a06686646c6461703a2f2f6c6461702e736f6674666f72756d2e636f6d3a3338392f434e3d58656375726543524c505542432c4f553d536563757269747920524e44204469766973696f6e2c4f3d536f6674666f72756d20436f72706f726174696f6e2c433d4b52300d06092a864886f70d010105050003820101003ce700a0492b225b1665d9c73d84c34f7a5faad7b397ed49231f030e4e0e91953a607bd9006425373d490ef3ba1cf47810ca8c22fabe0c609f93823efdede64744458e910267f9f857c907318e286da6c131c9dd5fada43fd8cfdf6bd1b1b239338cea83eb6b6893b88fbcfd8e86a677b7270ad96be5a82b40569efc2dda6df4bcd642d067183186d6cace6c8f73b80f30b57acb3bcd5cbbc51307922d5edb38cb0d90c3917a8e37534183ba10f403c1c034287f39442df795050f39d78ddad97da8a43f02d7641549af9b5d68908e49faa8a1597cfed4a43baadd42c8fe4fd44c96d314df56147b8a7fa6ba65ffdee9ed3a5da52ef9ac7f9ca5afb633e1ccdf318201a33082019f020101307c3077310b3009060355040613024b52311e301c060355040a1315536f6674666f72756d20436f72706f726174696f6e3121301f06035504031318536f6674666f72756d20526f6f7420417574686f726974793125302306092a864886f70d010901161663616d617374657240736f6674666f72756d2e636f6d020107300906052b0e03021a0500300d06092a864886f70d0101010500048201000e1c302b83a002ac95434a1f33b5907f1641d5bb444ff190608a182c89a1668875236bc90713677754c956041c956b79b4218a6ca3c776c0a152236d6d58a70b7ab220d5c56181165052da2201969e4aea705eea07320135086ad66f3224a972e222c289c197769a283d74b1ab2b5ff4871bff2c9590e7259a1def18c47eba3275a8b974774089b6be4b43702c7ea8bc3c4eba77f9ac81018168ceb0366a00038a83254df56e893dd761abe735c1f3ccc75bfb7efc21a19b0e55c0e590b19ce0d013d3db47ea22280ef13f375cbbd4d673cb2d553bf7d390668685abb889940a9e00d28a0b618df8b53f67e628bb303430c527585507ebd79d6605d4e577450b" );
    }



    private class getConnectTaskServer extends AsyncTask<URL, Void, String> {
        @Override
        protected void onPreExecute() {
            //
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(URL... params) {


            //
            String aResult = "";
            //
            HttpURLConnection aHttp = null;
            try {
                //
                String urlString = "http://" + hostUrl+ "/anysign/jsp/vid2.jsp" + "?" + "charset=utf8";
                urlString = "http://" + Constants.XAS_BASE_DOMAIN + "/anysign/jsp/vid2.jsp";
                //
                URL aURL = new URL(urlString);
                //
                aHttp = (HttpURLConnection) aURL.openConnection();
                //
                aHttp.setDefaultUseCaches(false);
                // 서버에서 읽기모드 설정
                aHttp.setDoInput(true);
                // 서버에서 쓰기모드 설정
                aHttp.setDoOutput(true);
                // 전송방식 설정 (GET, POST)
                aHttp.setRequestMethod("GET");
                // 서버에게 웹에서 <Form> 으로 값이 넘어온 거소가 같은 방식으로 처리하는 것을 알려줌
                aHttp.setRequestProperty("content-type", "text/plain");
                aHttp.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
                aHttp.setRequestProperty("Connection", "keep-alive");
                aHttp.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");

                // 서버로 값 전송
                StringBuffer aBuffer = new StringBuffer();
                //
                OutputStream aOutStream = aHttp.getOutputStream();
                //
                aOutStream.write(aBuffer.toString().getBytes("UTF-8"));
                //
                aOutStream.flush();
                //
                int aResCode = aHttp.getResponseCode();
                //

                Log.d("DEBUG","aResCode:"+aResCode +" urlString:" +urlString);
                if (aResCode == HttpURLConnection.HTTP_OK) {
                    //
                    try {
                        //
                        InputStreamReader tmp = new InputStreamReader(aHttp.getInputStream(), "UTF-8");
                        //
                        BufferedReader aReader = new BufferedReader(tmp);
                        //
                        StringBuilder axBuilder = new StringBuilder();
                        //
                        String aStr = "";
                        //
                        while ((aStr = aReader.readLine()) != null) {
                            //
                            axBuilder.append(aStr + "\n");
                        }
                        //
                        aResult = axBuilder.toString();

                    } catch (IOException e) {
                        //
                        Log.d("emkim", "Exception === " + e.getMessage());
                        //Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            } catch(MalformedURLException e) {
                //
                Log.d("emkim", "MalformedURLException!!!!!!!!!!!!!!!!!!!!!!!!");
//                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch(IOException e) {
                //
                Log.d("XecureSmart", "IOException on http connection");
//                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch(Exception e) {
                //
                Log.d("emkim", "Exception === " + e.getMessage());
//                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {
                //
                //dismissDialog(DIALOG_BLOCK_ENC);
            }
            Log.d("emkim", "aResult === " +aResult );
            return aResult;
        }

        @Override
        protected void onPostExecute(String result) {
            //
            super.onPostExecute(result);
            Log.d("emkim", "onPostExecute!!!!!!!!!!!!!!!!!");
            //
            if(result != null && !result.equals("")) {
                //
                result = result.trim();
                //
//                mPEMVal = rePlaceString(result);
                mPEMVal = result;
                Log.d("emkim", "mPEMVal:" + mPEMVal);

                callSignDataWithVIDAsync();
            }
        }
    }


    // 서버인증서 자바스크립트 변수 부분 제거
    public String rePlaceString(String targetString) {
        String aResult = "";
//        aResult = targetString.replaceAll("var s = '';", "").replaceAll("s [+][=] '", "")
//                .replaceAll("\\\\", "").replaceAll("n';", "").replaceAll("';", "")
//                .replaceAll("\\\n", "");
//        aResult = aResult.substring(0, aResult.indexOf("var session = \""));
//        aResult = aResult.replaceAll("-----BEGIN CERTIFICATE-----", "-----BEGIN CERTIFICATE-----\\\n")
//                .replaceAll("-----END CERTIFICATE-----", "\\\n-----END CERTIFICATE-----");
//        Log.d("CERTIFICATE", "aResult : "+aResult);

        aResult = targetString.replaceAll("var s = '';", "").replaceAll("s [+][=] '", "")
                .replaceAll("\\\\", "").replaceAll("n';", "").replaceAll("';", "");

        return aResult;
    }


    public void callSignDataWithVIDAsync() {
//        Log.d("DEBUG", "+++++++++++++++++++++++++++++++ callSignDataWithVIDAsync S +++++++++++++++++++++++++++++++ ");
        try {
            //
            Method aMethod = MainActivity.class.getDeclaredMethod("signDataWithVID");
            //
            XCoreWrapperAsyncParam aXCoreWrapperParam = new XCoreWrapperAsyncParam(aMethod, this);
            new XCoreWrapperAsyncCaller().execute(aXCoreWrapperParam);
        } catch (NoSuchMethodException e) {
            //Log.d("XecureSmart", "NoSuchMethodException in Asyncronous method");
        }
//        Log.d("DEBUG", "+++++++++++++++++++++++++++++++ callSignDataWithVIDAsync E +++++++++++++++++++++++++++++++ ");
    }

    public void signDataWithVID() {
        //
        Log.d("DEBUG", "+++++++++++++++++++++++++++++++ signDataWithVID S +++++++++++++++++++++++++++++++ ");
        mPlainMsg="";
        //mPlainMsg += mEdit_plainMsg.getText();

        // XecureSmart
        mXecureSmart = XecureSmart.getInstance();

        // 1. 인증서 리스트 오픈
        //     - 인증서 선택 (아이템클릭)

        // 2. 인증서 상세 화면
        //     - EditText 클릭

        // 3. 키패드 표시
        // 4. 비밀번호 입력
        // 5. 비밀번호 검증
        // 6. 검증 완료 (비밀번호 일치) -> 전자서명 데이터 생성
        // 7. 서버PEM 으로 전자봉투 수행 (VidInfo 데이터 생성)
        // 8. SignDataWithVID 는 전자서명 데이터만 리턴
        //    (VidInfo 데이터는 XecsureSmart.getVidInfo() 별도로 호출해서 리턴)
        Log.d("DEBUG", "+++++++++++++++++++++++++++++++ signDataWithVID SSS +++++++++++++++++++++++++++++++ mAcceptCert:[" + mAcceptCert + "]");
        Log.d("DEBUG", "+++++++++++++++++++++++++++++++ signDataWithVID SSS +++++++++++++++++++++++++++++++ mPlainMsg:["+ mPlainMsg + "]");
        Log.d("DEBUG", "+++++++++++++++++++++++++++++++ signDataWithVID SSS +++++++++++++++++++++++++++++++ mPEMVal:["+ mPEMVal+ "]");
        Log.d("DEBUG", "+++++++++++++++++++++++++++++++ signDataWithVID SSS +++++++++++++++++++++++++++++++ mSignOption:["+ mSignOption + "]");
        mSignedMsg = mXecureSmart.SignDataWithVID(
                mXgateAddr,             // 1 : XgateAddr     (Default : "")
                mAcceptCert,    // 2 : AcceptCert
                mPlainMsg,      // 3 : Plain Msg
                mPEMVal,        // 4 : Server PEM
                mSignOption,    // 5 : Sign Option   (Default : 20)
                "",             // 6 : Sign Desc     (Default : "")
                5);             // 7 : Pwd Try Limit
        //
        mVidInfoMsg = mXecureSmart.GetVidInfo();
        //
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
//                mEdit_signedMsg.setText(mSignedMsg);
//                mEdit_vidInfoMsg.setText(mVidInfoMsg);
                // 인증서 결과 출력
                mWebView.loadUrl("javascript:Sign_with_vid_user_Callback('" + mSignedMsg + "', '" + mVidInfoMsg + "')");
                Log.d("DEBUG","mSignedMsg:"+mSignedMsg);
                Log.d("DEBUG","mVidInfoMsg:"+mVidInfoMsg);
                Log.d("DEBUG","mAcceptCert:"+mAcceptCert);
                Log.d("DEBUG","mPlainMsg:"+mPlainMsg);
                Log.d("DEBUG","mSignOption:"+mSignOption);
                //dismissDialog(DIALOG_SignData);
            }
        });
        Log.d("DEBUG", "+++++++++++++++++++++++++++++++ signDataWithVID E +++++++++++++++++++++++++++++++ ");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intentdata) {
        if (requestCode == INPUT_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (mFilePathCallback == null) {
                    super.onActivityResult(requestCode, resultCode, intentdata);
                    return;
                }
                Uri[] results = new Uri[]{getResultUri(intentdata)};

                mFilePathCallback.onReceiveValue(results);
                mFilePathCallback = null;
            } else {
                if (mUploadMessage == null) {
                    super.onActivityResult(requestCode, resultCode, intentdata);
                    return;
                }
                Uri result = getResultUri(intentdata);

                Log.d(getClass().getName(), "openFileChooser : "+result);
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        } else {
            if (mFilePathCallback != null) mFilePathCallback.onReceiveValue(null);
            if (mUploadMessage != null) mUploadMessage.onReceiveValue(null);
            mFilePathCallback = null;
            mUploadMessage = null;
            super.onActivityResult(requestCode, resultCode, intentdata);
        }
    }

    private Uri getResultUri(Intent data) {
        Uri result = null;
        if(data == null || TextUtils.isEmpty(data.getDataString())) {
            // If there is not data, then we may have taken a photo
            if(mCameraPhotoPath != null) {
                result = Uri.parse(mCameraPhotoPath);
            }
        } else {
            String filePath = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                filePath = data.getDataString();
            } else {
                filePath = "file:" + RealPathUtil.getRealPath(this, data.getData());
            }
            result = Uri.parse(filePath);
        }

        return result;
    }

    /**
     * This function assumes logger is an instance of AppEventsLogger and has been
     * created using AppEventsLogger.newLogger() call.
     */
    /*    */
    public void logSentFriendRequestEvent () {
        logger.logEvent("sentFriendRequest");
    }


    private void analyticsFirebase(String url) {
        Bundle bundle = new Bundle();
        if(url.startsWith("tel:")){
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID,"전화상담");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_TO_CART, bundle);
        }else if(url.contains("loaninfo")){
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "대출상품소개");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_TO_WISHLIST, bundle);
        }else if(url.contains("loan_simulation_info")){
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "대출가능한도조회");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM_LIST, bundle);
        }else if(url.contains("loanapplication/baro")){
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "대출신청(신규)");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.BEGIN_CHECKOUT, bundle);
        }else if(url.contains("loanapplication/simple")){
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "대출신청(기존)");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }else if(url.contains("loancomplete/y/101658")){
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "대출성공(바로신규)");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.ECOMMERCE_PURCHASE, bundle);
        }else if(url.contains("loancomplete/y/100909")){
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "대출성공(기존신규)");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.ECOMMERCE_PURCHASE, bundle);
        }else if(url.contains("loancomplete/n")){
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "대출부결");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE_REFUND, bundle);
        }else if(url.contains("loaninfo")){
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "대출성공(재대출)");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle);
        }
    }

    private void analyticsFirebase() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID,"방문");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.JOIN_GROUP, bundle);
    }

    private void analyticsFacebook(String url) {
        Bundle params = new Bundle();
        if(url.startsWith("tel:")){
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, url);
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, url);
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "tel:");
            params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "KRW");
            logger.logEvent(AppEventsConstants.EVENT_NAME_ADDED_TO_CART, 1, params);
        }else if(url.contains("loaninfo")){
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "page");
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, "대출상품소개");
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, "대출상품소개");
            params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "KRW");
            logger.logEvent(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, 1, params);
        }else if(url.contains("loan_simulation_info")){
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "page");
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, "대출가능한도조회");
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, "대출가능한도조회");
            params.putInt(AppEventsConstants.EVENT_PARAM_MAX_RATING_VALUE, 1);
            logger.logEvent(AppEventsConstants.EVENT_NAME_RATED, 1, params);
        }else if(url.contains("loanapplication/baro")){
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, "대출신청(신규)");
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, "대출신청(신규)");
            params.putInt(AppEventsConstants.EVENT_PARAM_SUCCESS, 1);
            logger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_TUTORIAL, params);
        }else if(url.contains("loanapplication/simple")){
            params.putString(AppEventsConstants.EVENT_PARAM_REGISTRATION_METHOD, "대출신청(기존)");
            logger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, params);
        }else if(url.contains("loancomplete/y/101658")){
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, "대출성공(바로신규)");
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, "대출성공(바로신규)");
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "page");
            params.putInt(AppEventsConstants.EVENT_PARAM_NUM_ITEMS, 1);
            params.putInt(AppEventsConstants.EVENT_PARAM_PAYMENT_INFO_AVAILABLE, 1);
            params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "KRW");
            logger.logEvent(AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT, 1, params);
        }else if(url.contains("loancomplete/y/100909")){
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, "대출성공(기존신규)");
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, "대출성공(기존신규)");
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "page");
            params.putInt(AppEventsConstants.EVENT_PARAM_NUM_ITEMS, 1);
            params.putInt(AppEventsConstants.EVENT_PARAM_PAYMENT_INFO_AVAILABLE, 1);
            params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "KRW");
            logger.logEvent(AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT, 1, params);
        }else if(url.contains("loancomplete/n")){
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, "대출부결");
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, "대출부결");
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "page");
            logger.logEvent(AppEventsConstants.EVENT_NAME_SPENT_CREDITS, 1, params);
        }else if(url.contains("loancomplete/y/101523")){
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, "대출성공(재대출)");
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, "대출성공(재대출)");
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "page");
            BigDecimal purchaseAmout = new BigDecimal(1);
            Currency currency = Currency.getInstance("KRW");
            logger.logPurchase(purchaseAmout, currency, params);
        }
    }

    private void analyticsFacebook() {
        Bundle params = new Bundle();
        params.putInt(AppEventsConstants.EVENT_PARAM_SUCCESS, 1);
        logger.logEvent(AppEventsConstants.EVENT_NAME_ADDED_PAYMENT_INFO, params);
    }
}
