package com.softforum.xecure.certshare;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.softforum.xecure.XecureSmart;
import com.softforum.xecure.crypto.CertMgr;
import com.softforum.xecure.ui.crypto.XecureSmartCertMgr;
import com.softforum.xecure.ui.webcall.ExportCertPasswordWindowXK;
import com.softforum.xecure.util.BlockerActivityResult;
import com.softforum.xecure.util.BlockerActivityUtil;
import com.softforum.xecure.util.EnvironmentConfig;
import com.softforum.xecure.util.XDetailData;
import com.softforum.xecure.util.XDetailDataParser;
import com.softforum.xecure.util.XDetailDataRowAdapter;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.barocredit.barobaro.R;


public class SelectExportCertListWithCertShare extends ListActivity {
	
	private int mMediaID = XecureSmart.mDefaultMediaID;

	private final int MEDIA_SDCARD = 0;
	private final int MEDIA_APPDATA = 1;
	
	private ArrayList<XDetailData> mXCertDataList = null;

	//
	private BlockerActivityResult mBlockerParam;
	
	private int mResultCode = 0;
	
	
	@Override
	public void onCreate (Bundle savedInstanceState)
	{
		super.onCreate (savedInstanceState);
		setContentView (R.layout.select_export_cert_by_xcs);

		//
		mMediaID = XecureSmartCertMgr.mCERT_LOCATION_SDCARD;
		
		mXCertDataList = new ArrayList<XDetailData>();
		
		// 
		setSpinner ();
		
		//
		setUserCertItems(mMediaID);
		
		// AsyncTask 처리를 위하여 블록파람 설정
		Intent receivedIntent = getIntent();
				
		mBlockerParam = BlockerActivityUtil.getParam(this, receivedIntent);
	}
	
	private void setSpinner ()
	{
		//
		View aView = findViewById(R.id.select_media);
		aView.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
		aView.setVisibility(View.INVISIBLE);
	}
	
	private void setUserCertItems (int pMediaID)
	{
		int aMediaID = 0;
		int aSearchCondition = 0;

		String aCertList = null;
		String aMediaList = null;
		StringTokenizer aMediaListTokenizer = null;

		CertMgr aCertMgr = null;
		
		if (mXCertDataList.size () > 0)
		{
			mXCertDataList.clear ();
		}
		
		//
		aMediaID = pMediaID;

		/*-----------------------------------------------------------------------------*
		 * 검색조건 설정하자.
		 *-----------------------------------------------------------------------------*/
		aSearchCondition = XecureSmartCertMgr.mCERT_SEARCH_TYPE_ANY;

		if (EnvironmentConfig.mExcludeExpiredCert)
		{
			aSearchCondition = XecureSmartCertMgr.mCERT_SEARCH_TYPE_ANY_EXCLUDE_EXPIRE;
		}

		/*-----------------------------------------------------------------------------*
		 * MediaList 가져오자.
		 *-----------------------------------------------------------------------------*/
		aCertMgr = CertMgr.getInstance ();
		aMediaList = aCertMgr.getMediaList (aMediaID - 1, 1, 1);
		aMediaListTokenizer = new StringTokenizer(aMediaList, "\t\n");

		while (aMediaListTokenizer.hasMoreTokens ())
		{
			aMediaID = Integer.parseInt (aMediaListTokenizer.nextToken ());

			/*-------------------------------------------------------------------------*
			 * 해당 MediaID 값에 존재하는 인증서 리스트 가져오자.
			 * 인증서 정보 만들어 줄 때 MediaID 값도 저장해주자.
			 *-------------------------------------------------------------------------*/
			aCertList = aCertMgr.getCertTree (aMediaID,
					XecureSmartCertMgr.mCERT_TYPE_USER,
					aSearchCondition,
					XecureSmartCertMgr.mCERT_CONTENT_LEVEL_SIMPLE_LIST,
					"",
					null);

			mXCertDataList.addAll (XDetailDataParser.parse (aCertList, XDetailData.TYPE_CERT_SIMPLE, aMediaID));
		}

		setListAdapter (new XDetailDataRowAdapter (this, mXCertDataList));
	}
	
	@Override
	protected void onListItemClick (ListView l, View v, int position, long id)
	{
		XDetailData aSelectedCert = null;
		Intent aIntent = null;
		
		super.onListItemClick (l, v, position, id);

		aSelectedCert = mXCertDataList.get (position);
		
		//
		aIntent = new Intent(SelectExportCertListWithCertShare.this, ExportCertPasswordWindowXK.class);
		
		aIntent.putExtra (ExportCertPasswordWindowXK.mMediaIDKey, mMediaID);
		aIntent.putExtra (ExportCertPasswordWindowXK.mCallModeKey, ExportCertPasswordWindowXK.mCallModeExportCertificateByCertShare);
		aIntent.putExtra (ExportCertPasswordWindowXK.mSelectedCertDataKey, aSelectedCert.getValueArray ());
		
		startActivityForResult (aIntent, ExportCertPasswordWindowXK.mSignCertPasswordWindowID);
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data)
	{
		int aResultFromThat = -1;
		Intent aIntent = null;

		/*-----------------------------------------------------------------------------*
		 * 비밀번호 입력 activity 로부터 결과값 받아옴.
		 *-----------------------------------------------------------------------------*/
		if (data != null)
		{
			aResultFromThat = data.getIntExtra (XecureSmartCertMgr.mOperationResultKey, -1);
			
			/*-------------------------------------------------------------------------*
			 * 결과값이 인증서 내보내기 취소인 경우 : activity result cancel 값 설정.
			 * 그 외의 경우 : 호출한 activity (TBrowser) 로 결과값 전달.
			 *-------------------------------------------------------------------------*/
			if (aResultFromThat == XecureSmartCertMgr.mResultForExportCertByXCSCancel)
			{
				setResult (Activity.RESULT_CANCELED);
				finish ();
			}
			else
			{
				aIntent = new Intent();
				aIntent.putExtra (XecureSmartCertMgr.mOperationResultKey, aResultFromThat);
				setResult (Activity.RESULT_OK, aIntent);
				finish ();
			}
		}
	}

	@Override
	public void onBackPressed() 
	{
		mResultCode = 123002;
		
		//mBlockerParam.setBlockerResult(123002);
		
		finish();
	}
	
	
	@Override
	public void finish() {
		// 동작 취소시 123002 리턴코드 설정
		mBlockerParam.setBlockerResult(mResultCode);
		
		BlockerActivityUtil.finishBlockerActivity(mBlockerParam);
		
		super.finish();
	}
}
