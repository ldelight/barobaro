package com.barocredit.barobaro.Activities;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.Toast;

import com.barocredit.barobaro.Common.Constant;
import com.barocredit.barobaro.Common.BaroChromeClient;
import com.barocredit.barobaro.R;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    //기본 오브젝트
    WebView mWebView;
    private final Handler handler = new Handler();
    Activity activity = this;;

    //파일 업로드
    private String mCameraPhotoPath;
    private ValueCallback<Uri[]> mFilePathCallback;
    private ValueCallback<Uri> mUploadMessage;

    // BACK 2번 클릭 시 종료 핸들러. 플래그
    private Handler mHandler  = new Handler();
    private boolean mFlag = false;

    // 웹뷰 로딩 관련
    ProgressDialog mProgress;

    //useragent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWebView();
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

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                Log.d("url", "should overload url["+url+"]");
                if (url.startsWith("tel:")) {
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

                    // 헤더 인증 문자열 삽입
//                    Map<String, String> extraHeaders = new HashMap<String, String>(); extraHeaders.put(Constant.AUTH_HEADER_KEY,Constant.AUTH_HEADER_VAL);
//                    view.loadUrl(url,extraHeaders);

                    //useragent 값 설정
                    String userAgent = mWebView.getSettings().getUserAgentString();
                    mWebView.getSettings().setUserAgentString(userAgent + " " + Constant.USER_AGENT_STRING);
                    view.loadUrl(url);


                    Toast.makeText(MainActivity.this, ".isShowing:" + mProgress.isShowing(), Toast.LENGTH_SHORT).show();

                    if (mProgress.isShowing()) {
                    //    mProgress.dismiss();
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
        mWebView.addJavascriptInterface(new AndroidBridge(), "android");
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
                intent.setType(Constant.TYPE_IMAGE);
                startActivityForResult(intent, Constant.INPUT_FILE_REQUEST_CODE);
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
                contentSelectionIntent.setType(Constant.TYPE_IMAGE);

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

                startActivityForResult(chooserIntent, Constant.INPUT_FILE_REQUEST_CODE);
            }
        });



        if(getIntent().getStringExtra("STARTURL") != null){
            mWebView.loadUrl(getIntent().getStringExtra("STARTURL"));
        }else{
            mWebView.loadUrl(Constant.INITURL);
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

    public class AndroidBridge {

        //HTML 문서 내에서 JavaScript로 호출가능한 함수
        //브라우저에서 load가 완료되었을 때 호출하는 함수
        @JavascriptInterface
        public void callAndroidDeviceInfo() {
            //webView.loadUrl("javascript:setLocation('"+ location.getLongitude()+"','"+ location.getLatitude()+"');");
            Log.d("regId", Constant.gcmRegId);

            handler.post(new Runnable() {
                @Override
                public void run() {
//                    mWebView.loadUrl("javascript:callClientInfo('" + Constant.gcmRegId + "')");
                }
            });
        }

        @JavascriptInterface
        public void callAndroidLocationInfo() {
            //webView.loadUrl("javascript:setLocation('"+ location.getLongitude()+"','"+ location.getLatitude()+"');");
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
            Constant.isAutoLoginYn=true;
        }


        @JavascriptInterface
        public void setAutoLoginNo() {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(mWebView.getContext());
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.removeSessionCookie();
            cookieManager.removeAllCookie();
            cookieSyncManager.sync();
        }

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
    public void  onBackPressed() {
        //Toast.makeText(this, ".getUrl:" + mWebView.getUrl(), Toast.LENGTH_SHORT).show();
        //super.onBackPressed();

        if (mWebView.getUrl().indexOf(Constant.MAIN_URL) > 0  || mWebView.getUrl().indexOf(Constant.LOGIN_URL) > 0   || mWebView.getUrl().equalsIgnoreCase(Constant.INITURL)){
            if(!mFlag) {
                Toast.makeText(this, "'뒤로'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();    // 종료안내 toast 를 출력
                mFlag = true;
                mHandler.sendEmptyMessageDelayed(0, 2000);    // 2000ms 만큼 딜레이
                return;
            }else {
                finish();
            }
        }else if(mWebView.getUrl().indexOf(Constant.JOIN_URL) > 0 ) {
            return;
        }else {
            //뒤로 가기 실행
            if ( mWebView.canGoBack() ) {
                mWebView.goBack();
                return;
            }
        }

    }
}
