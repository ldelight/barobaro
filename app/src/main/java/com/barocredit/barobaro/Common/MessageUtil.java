package com.barocredit.barobaro.Common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import com.barocredit.barobaro.R;


public class MessageUtil {
	
	public static void alertDialog(Activity mActivity, String message) {
		new AlertDialog.Builder(mActivity).setMessage(message)
		.setPositiveButton(mActivity.getString(R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.show();
	}

	public static void alertDialog(Activity mActivity, String message, DialogInterface.OnClickListener listener) {
		alertDialog(mActivity, message, listener, true);
	}

	public static void alertDialog(Activity mActivity, String message, DialogInterface.OnClickListener listener, boolean cancelable) {
		new AlertDialog.Builder(mActivity).setMessage(message)
		.setPositiveButton(mActivity.getString(R.string.ok), listener)
		.setCancelable(cancelable)
		.show();
	}

	public static void confirmDialog(Activity mActivity, String message, String positiveBtnTxt, DialogInterface.OnClickListener positiveListener, String negativeBtnTxt, DialogInterface.OnClickListener negativeListener) {
		try{
			new AlertDialog.Builder(mActivity).setMessage(message)
					.setPositiveButton(positiveBtnTxt, positiveListener)
					.setNegativeButton(negativeBtnTxt, negativeListener)
					.show();
		}catch (Exception e){
			e.getStackTrace();
			Log.d("DEBUG","EXCEPTION::" + e.getMessage());
		}
	}

	
	
	
}
