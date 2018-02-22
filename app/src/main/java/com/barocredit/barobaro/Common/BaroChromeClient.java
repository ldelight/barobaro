package com.barocredit.barobaro.Common;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Message;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * Created by ctest on 2018-02-22.
 */

public class BaroChromeClient extends WebChromeClient {

    private ValueCallback<Uri> filePathCallbackNormal;
    private ValueCallback<Uri[]> filePathCallbackLollipop;

    @Override
    public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
        // TODO Auto-generated method stub
        return super.onCreateWindow(view, dialog, userGesture, resultMsg);
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {

        // TODO Auto-generated method stub
        //return super.onJsAlert(view, url, message, result);
        new AlertDialog.Builder(view.getContext())
                .setTitle("알림")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok,
                        new AlertDialog.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                .setCancelable(false)
                .create()
                .show();
        return true;
    }
    @Override
    public boolean onJsConfirm(WebView view, String url, String message,
                               final JsResult result) {
        // TODO Auto-generated method stub
        //return super.onJsConfirm(view, url, message, result);
        new AlertDialog.Builder(view.getContext())
                .setTitle("알림")
                .setMessage(message)
                .setPositiveButton("네",
                        new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                .setNegativeButton("아니오",
                        new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.cancel();
                            }
                        })
                .setCancelable(false)
                .create()
                .show();
        return true;
    }


    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        callback.invoke(origin, true, false);
    }


}
