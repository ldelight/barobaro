package com.softforum.xecure.ui.webcall;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.softforum.xecure.XecureSmart;
import com.softforum.xecure.crypto.CertMgr;
import com.softforum.xecure.keypad.XecureSmartChangePasswordWithXK;
import com.softforum.xecure.ui.crypto.XecureSmartCertMgr;
import com.softforum.xecure.util.BlockerActivityResult;
import com.softforum.xecure.util.BlockerActivityUtil;
import com.softforum.xecure.util.EnvironmentConfig;
import com.softforum.xecure.util.XDetailData;
import com.softforum.xecure.util.XDetailDataParser;
import com.softforum.xecure.util.XDetailDataRowAdapter;
import com.softforum.xecure.util.XErrorCode;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.barocredit.barobaro.R;


public class XecureSmartChangePasswordCertList extends ListActivity {
	//
	public static final String mOperationResultKey	= "xecure_smart_cert_mgr_result_key";

	//
	private static int mMediaID = XecureSmart.mDefaultMediaID;
	
	//
	private BlockerActivityResult mBlockerParam;
	
	private int mResultCode = 0;
	
	//
	public Handler mHandler = new Handler();
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView( R.layout.activity_change_pw_select_window );
		
		//
		mMediaID = XecureSmartCertMgr.mCERT_LOCATION_SDCARD;
		
		Intent receivedIntent = getIntent();
		
		mBlockerParam = BlockerActivityUtil.getParam(this, receivedIntent);
		
		//
		setSpinner();
		
		//
		setUserCertItems(mMediaID);
	}
	
	//
	private void setSpinner() {
		//
		View aView = findViewById(R.id.select_media);
		aView.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
		aView.setVisibility(View.INVISIBLE);
	}
	
	//
	private void setUserCertItems(int pMediaID) 
	{
		//
		int							aMediaID = 0;
		String aCertList = null;
		String aMediaList = null;
		StringTokenizer aMediaListTokenizer = null;
		ArrayList<XDetailData> aXCertDataList = new ArrayList<XDetailData>();
		CertMgr aCertMgr;
		int								aSearchCondition = 0;
		
		//
		aMediaID = pMediaID;
		
		/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		 *	MediaList 가져오자.
		 *------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		aCertMgr = CertMgr.getInstance();
		aMediaList = aCertMgr.getMediaList(aMediaID - 1, 1, 1);

		aMediaListTokenizer = new StringTokenizer(aMediaList, "\t\n");
		
		/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		 *	검색조건 설정하자.
		 *------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		aSearchCondition = XecureSmartCertMgr.mCERT_SEARCH_TYPE_ANY;

		if (EnvironmentConfig.mExcludeExpiredCert == true) {
			aSearchCondition = XecureSmartCertMgr.mCERT_SEARCH_TYPE_ANY_EXCLUDE_EXPIRE;
		}
		
		while (aMediaListTokenizer.hasMoreTokens()) {
			aMediaID = Integer.parseInt(aMediaListTokenizer.nextToken());
			
			/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
			 *	해당 MediaID 값에 존재하는 인증서 리스트 가져오자.
			 * 인증서 정보 만들어 줄 때 MediaID 값도 저장해주자.
			 *------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			aCertList = aCertMgr.getCertTree(aMediaID,
					XecureSmartCertMgr.mCERT_TYPE_USER, aSearchCondition,
					XecureSmartCertMgr.mCERT_CONTENT_LEVEL_SIMPLE_LIST, "", 
					null);

			aXCertDataList.addAll(XDetailDataParser.parse(aCertList,
					XDetailData.TYPE_CERT_SIMPLE, aMediaID));
		}
		
		setListAdapter(new XDetailDataRowAdapter(this, aXCertDataList));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
		
		int								aMediaID = 0;
		String aCertList = null;
		String aMediaList = null;
		StringTokenizer aMediaListTokenizer = null;
		XDetailData aSelectedCert = null;
		ArrayList<XDetailData> aXCertDataList = new ArrayList<XDetailData>();
		CertMgr aCertMgr;
		int								aSearchCondition = 0;
		Intent aIntent = null;
		
		aMediaID = mMediaID;
		
		/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	     *	검색조건 설정하자.
	     *------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		aSearchCondition = XecureSmartCertMgr.mCERT_SEARCH_TYPE_ANY;
		
		if(EnvironmentConfig.mExcludeExpiredCert == true)
		{
			aSearchCondition = XecureSmartCertMgr.mCERT_SEARCH_TYPE_ANY_EXCLUDE_EXPIRE;
		}
		
		/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 	*	MediaList 가져오자.
	 	*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		aCertMgr = CertMgr.getInstance();
		aMediaList = aCertMgr.getMediaList(aMediaID - 1, 1, 0);
	
		aMediaListTokenizer = new StringTokenizer(aMediaList, "\t\n");
		
		while (aMediaListTokenizer.hasMoreTokens())
		{
			aMediaID = Integer.parseInt(aMediaListTokenizer.nextToken());
	
			/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		 	*	해당 MediaID 값에 존재하는 인증서 리스트 가져오자.
		 	* 인증서 정보 만들어 줄 때 MediaID 값도 저장해주자.
		 	*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			aCertList = aCertMgr.getCertTree(aMediaID,
					XecureSmartCertMgr.mCERT_TYPE_USER,
					aSearchCondition,
					XecureSmartCertMgr.mCERT_CONTENT_LEVEL_FULL,
					"",
					null);
			
			aXCertDataList.addAll(XDetailDataParser.parse(aCertList, XDetailData.TYPE_CERT, aMediaID));
		}
		
		aSelectedCert = aXCertDataList.get(position);
		
		//
		aIntent = new Intent(this, XecureSmartChangePasswordWithXK.class);
		
		aIntent.putExtra(XecureSmartChangePasswordWithXK.mMediaIDKey, mMediaID);
		aIntent.putExtra(XecureSmartChangePasswordWithXK.mSelectedCertDataKey, aSelectedCert.getValueArray());	
		startActivityForResult(aIntent, XecureSmartChangePasswordWithXK.mXecureSmartChangePasswordID);		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		String aMessage = "";
		int aResultFromThat = -1;
		
		if (data != null) {
			aResultFromThat = data.getIntExtra(mOperationResultKey, -1);
		}
		
		if (XErrorCode.XW_ERROR_RESULT_FAIL == resultCode) {
			aMessage = getString(R.string.requested_task_fail);	/* 요청한 작업이 실패하였습니다. */
		}
		else if (Activity.RESULT_CANCELED == resultCode) {
			aMessage = getString(R.string.requested_task_canceled);	/* 요청한 작업이 취소되었습니다. */
			
			//
			mResultCode = 123003;
		}
		else if (aResultFromThat == XecureSmartCertMgr.mResultForChangePassword) {
			aMessage = getString(R.string.xecure_smart_cert_mgr_change_password_success);
			refreshCertList();
		}
		else if (	Activity.RESULT_OK == resultCode)
		{
			aMessage = "요청하신 작업이 정상적으로 처리되었습니다.";
			refreshCertList();
    	}
			
		else
			return;

		if (aMessage.length() > 0) {
			new AlertDialog.Builder(this)
			.setTitle("알림")
			.setPositiveButton("확인", null)
			.setMessage(aMessage)
			.show();
		}
		
		//finish();	
	}
	
	public void refreshCertList() {
		//
		this.setUserCertItems(mMediaID);
	}
	
	@Override
	public void onBackPressed() 
	{
		mResultCode = 123003;
		
		//mBlockerParam.setBlockerResult(123002);
		
		finish();
	}
	
	@Override
	public void finish() {
		
		/*
		Intent receivedIntent = getIntent();
		
		//String tempData = receivedIntent.getStringExtra(mResultCodeKey);
		int tempData = receivedIntent.getIntExtra(mResultCodeKey, 99999);
		
		if(tempData != 0) {
			mResultCode = 123003;
		}
		*/
		
		// 동작 취소시 123003 리턴코드 설정
		mBlockerParam.setBlockerResult(mResultCode);
		
		BlockerActivityUtil.finishBlockerActivity(mBlockerParam);
		
		super.finish();
	}
	
	/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 	*	Alert을 위한 토스트 메소드.
 	*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	public void showToast(final String message)
	{
		mHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				Toast.makeText(XecureSmartChangePasswordCertList.this, message, Toast.LENGTH_LONG).show();
			}
		});
	}
}
