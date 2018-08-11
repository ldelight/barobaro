package com.barocredit.barobaro.Common;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import com.barocredit.barobaro.R;

import java.util.List;

/**
 * Created by ctest on 2018-03-11.
 */

public class EnviromentUtil {
    static boolean isCheckAppInstalled = false;
    public static int getVersion(Context mContext) {
        int version = 0;
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            version = pInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    public static String getPackageName(Context mContext){
        String packageName = "";
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            packageName = pInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packageName;
    }

    public static void removeApp(final Context mContext, Activity mActivity) {
        final String packageName = "kr.note.app.dbnote";
        if(getApplicationInstalled(mContext, packageName)) {
            MessageUtil.confirmDialog(mActivity, mActivity.getString(R.string.msg_required_remove_old_app), mActivity.getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_DELETE).setData(Uri.parse("package:" + packageName));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    dialog.dismiss();
                }
            }, mActivity.getString(R.string.finish), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_DELETE).setData(Uri.parse("package:" + packageName));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    dialog.dismiss();
                }
            });
        }
    }

    public static boolean installApp(final Context mContext, Activity mActivity){
        final String packageName = "com.ketchapp.rider";
        if(getApplicationInstalled(mContext, packageName)) {//앱이 있으면 실행
            if(!isCheckAppInstalled ){
                //패키지 실행할 때 변수 줘야 함.
                PackageManager packageManager = mContext.getPackageManager();
                Intent intent = packageManager.getLaunchIntentForPackage( packageName );
                if (null != intent){
                    mContext.startActivity(intent);
                    isCheckAppInstalled = true;
                }
            }
        }else { // 설치가 안되었을 경우 설치 페이지 이동
            Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
            marketLaunch.setData(Uri.parse("market://search?q=" + packageName ));
            mContext.startActivity(marketLaunch);

        }
        return isCheckAppInstalled;
    }

    public static boolean getApplicationInstalled(Context mContext, String pkgName) {
        ApplicationInfo appInfo = null;

        final PackageManager pm = mContext.getPackageManager();
        try {
            appInfo = pm.getApplicationInfo(pkgName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if(appInfo != null) return true;
        else return false;
    }


    public boolean existPakage(Context context, String pakagename) {
        boolean isExist = false;

        PackageManager pkgMgr = context.getPackageManager();
        List<ResolveInfo> mApps;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = pkgMgr.queryIntentActivities(mainIntent, 0);

        try {
            for (int i = 0; i < mApps.size(); i++) {
                if(mApps.get(i).activityInfo.packageName.startsWith( pakagename )){
                    isExist = true;
                    break;
                }
            }
        }
        catch (Exception e) {
            isExist = false;
        }
        return isExist;
    }

    public static void installAnySign(final Context context){
        final String packageName = "com.softforum.xecureanysign";
        Activity activity = (Activity)context;
        if(!getApplicationInstalled(context, packageName)) {//앱이 없으면 실행
            MessageUtil.confirmDialogOk(activity, activity.getString(R.string.msg_install_anysign), activity.getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                    marketLaunch.setData(Uri.parse("market://search?q=" + packageName ));
                    context.startActivity(marketLaunch);
                    dialog.dismiss();
                }
            }, activity.getString(R.string.finish), false);
        }
    }
}