package com.softforum.xecure.ui.crypto;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.softforum.xecure.XApplication;
import com.softforum.xecure.crypto.CertMgr;
import com.softforum.xecure.keypad.SignCertPasswordWindowWithXK;
import com.softforum.xecure.util.BlockerActivityResult;
import com.softforum.xecure.util.BlockerActivityUtil;
import com.softforum.xecure.util.EnvironmentConfig;
import com.softforum.xecure.util.XDetailData;
import com.softforum.xecure.util.XDetailDataParser;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.barocredit.barobaro.R;
import com.softforum.xecure.util.XDetailDataRowAdapter;

public class SignCertSelectWindow extends ListActivity {

	/*
	 * Key for receiving parameter.
	 */
	public static final String mPluginSessionIDKey = "plugin_session_id_key";
	public static final String mXaddrKey = "xaddr_key";
	public static final String mMediaIDKey = "media_id_key";
	public static final String mCertTypeKey = "cert_type_key";
	public static final String mMediaTypeKey = "media_type_key";
	public static final String mSearchValueKey = "search_value_key";
	public static final String mCertSerialKey = "search_serial_key";
	
	public static final String mCallModeKey = "call_mode_key";
	public static final String mCallModeSign = "call_mode_sign";
	
	public static final String mPainTextKey = "sign_plain_text_data";
	public static final String mSignOption = "sign_option";
	
	/*
	 * Variables
	 */

	private BlockerActivityResult mBlockerParam;
	
	/*
	 * Input Values
	 */
	private int mMediaID;
	private String mSearchValue;
	private String mSearchSerial;
	private String mPainText;
	private int mSignOptionValue;
	
	/*
	 * Return Values
	 */
	private byte[] mInputedPassword;
	private String mSelectedSubjectRDN;
	
	private byte[] mRandomValue = null;
	private String mEncryptedData = null;
	
	private String mCallMode;
	
	public Handler mHandler = new Handler();
	
	public SignCertSelectWindow()
	{
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.crypto_sign_cert_select_window);
		
		/*
		 * Handling received intents
		 */
		Intent receivedIntent = getIntent();

		mMediaID = receivedIntent.getIntExtra(SignCertSelectWindow.mMediaIDKey, -1);
		mSearchValue = receivedIntent.getStringExtra(SignCertSelectWindow.mSearchValueKey);
		mSearchSerial = receivedIntent.getStringExtra(SignCertSelectWindow.mCertSerialKey);
		mPainText = receivedIntent.getStringExtra(SignCertSelectWindow.mPainTextKey);
		mSignOptionValue = receivedIntent.getIntExtra(SignCertSelectWindow.mSignOption, -1);
		mCallMode = receivedIntent.getStringExtra(SignCertSelectWindow.mCallModeKey) ;
		
		//Blocker Parameter
		mBlockerParam = BlockerActivityUtil.getParam(this, receivedIntent);
		
		/*
		 * set description
		 */
		//TextView aTextView01 = (TextView)findViewById(R.id.top_desc_text);
		//aTextView01.setText( this.getString(R.string.sign_cert_select_window_sign_desc) );
		
		View aView = findViewById(R.id.select_media);
		aView.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
		aView.setVisibility(View.INVISIBLE);
		
		setUserCertItems(mMediaID);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		int								aMediaID = 0;
		String aCertList = null;
		String aMediaList = null;
		StringTokenizer aMediaListTokenizer = null;
		XDetailData aSelectedCert = null;
		ArrayList<XDetailData> aXCertDataList = new ArrayList<XDetailData>();
		CertMgr aCertMgr = null;
		int								aSearchCondition = 0;
		Intent aIntent = null;
		
		super.onListItemClick(l, v, position, id);
		
		aMediaID = mMediaID;
		
		if (mSearchValue.equals(""))
		{
			aSearchCondition = XecureSmartCertMgr.mCERT_SEARCH_TYPE_ANY;
		
			if(EnvironmentConfig.mExcludeExpiredCert == true)
			{
				aSearchCondition = XecureSmartCertMgr.mCERT_SEARCH_TYPE_ANY_EXCLUDE_EXPIRE;
			}
		}
		else
		{
			aSearchCondition = XecureSmartCertMgr.mCERT_SEARCH_TYPE_ISSUERRDN_CN;
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
					XecureSmartCertMgr.mCERT_CONTENT_LEVEL_SIMPLE_LIST,
					mSearchValue,
					mSearchSerial);
			
			aXCertDataList.addAll(XDetailDataParser.parse(aCertList, XDetailData.TYPE_CERT_SIMPLE, aMediaID));
		}
		
		aSelectedCert = aXCertDataList.get(position);

		//
		mInputedPassword = null;
		mSelectedSubjectRDN = aSelectedCert.getValue(XDetailData.CERT_KEY_SIMPLE.SUBJECT_RDN);
		
		//if (EnvironmentConfig.mXecureKeypadFullViewUsage|| EnvironmentConfig.mXecureKeypadNormalViewUsage)
		aIntent = new Intent(this, SignCertPasswordWindowWithXK.class);
		
		aIntent.putExtra(SignCertSelectWindow.mCallModeKey, mCallMode);

		aIntent.putExtra(SignCertPasswordWindowWithXK.mMediaIDKey, mMediaID);
		aIntent.putExtra(SignCertPasswordWindowWithXK.mSelectedCertDataKey, aSelectedCert.getValueArray());	
		startActivityForResult(aIntent, SignCertPasswordWindowWithXK.mSignCertPasswordWindowID);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == SignCertPasswordWindowWithXK.mSignCertPasswordWindowID &&
				resultCode == Activity.RESULT_OK)
		{
			//
			mRandomValue = data.getByteArrayExtra (SignCertPasswordWindowWithXK.mRandomValueKey);
			mEncryptedData = data.getStringExtra (SignCertPasswordWindowWithXK.mE_ncryptedDataKey);
		}
		
		if (	Activity.RESULT_OK == resultCode) {
			passValuesToParent();
			finish();
		}
		else if (SignCertPasswordWindowWithXK.RESULT_PASSWD_FAIL == resultCode) {
			//			setResult(SignCertPasswordWindow.RESULT_PASSWD_FAIL);
			mBlockerParam.setBlockerResult(SignCertPasswordWindowWithXK.RESULT_PASSWD_FAIL);
			finish();
		}
	}
	
	private void passValuesToParent ()
	{
		Intent aIntent = new Intent();
		aIntent.putExtra (XApplication.mMediaIDKey, mMediaID);
		aIntent.putExtra (XApplication.mSubjectRDNKey, mSelectedSubjectRDN);
		
		//
		aIntent.putExtra (XApplication.mRandomValueKey, mRandomValue);
		aIntent.putExtra (XApplication.mE_ncryptedDataKey, mEncryptedData);
		
		mBlockerParam.setBlockerResult(Activity.RESULT_OK, aIntent);
	}
	
	private void setUserCertItems(int pMediaID) 
	{
		int							aMediaID = 0;
		String aCertList = null;
		String aMediaList = null;
		ArrayList<XDetailData> aXCertDataList = new ArrayList<XDetailData>();
		CertMgr aCertMgr;
		StringTokenizer aMediaListTokenizer = null;
		int							aSearchCondition = 0;
		
		aMediaID = pMediaID;
		
		aCertMgr = CertMgr.getInstance();
		
		/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		*	검색조건 설정하자.
		*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		if (mSearchValue.equals(""))
		{
			aSearchCondition = XecureSmartCertMgr.mCERT_SEARCH_TYPE_ANY;
			
			if(EnvironmentConfig.mExcludeExpiredCert == true)
			{
				aSearchCondition = XecureSmartCertMgr.mCERT_SEARCH_TYPE_ANY_EXCLUDE_EXPIRE;
			}
		}
		else
		{
			aSearchCondition = XecureSmartCertMgr.mCERT_SEARCH_TYPE_ISSUERRDN_CN;
		}
		
		/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		*	MediaList 가져오자.
		*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		aMediaList = aCertMgr.getMediaList(aMediaID - 1, 1, 1);
		
		aMediaListTokenizer = new StringTokenizer(aMediaList, "\t\n");
		
		while (aMediaListTokenizer.hasMoreTokens())
		{
			aMediaID = Integer.parseInt(aMediaListTokenizer.nextToken());
			aCertList = aCertMgr.getCertTree(aMediaID,
					XecureSmartCertMgr.mCERT_TYPE_USER,
					aSearchCondition,
					XecureSmartCertMgr.mCERT_CONTENT_LEVEL_SIMPLE_LIST,
					mSearchValue,
					mSearchSerial);
			
			aXCertDataList.addAll(XDetailDataParser.parse(aCertList, XDetailData.TYPE_CERT_SIMPLE, aMediaID));
		}
		
		setListAdapter(new XDetailDataRowAdapter(this, aXCertDataList));
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
				Toast.makeText(SignCertSelectWindow.this, message, Toast.LENGTH_LONG).show();
			}
		});
	}
	
	@Override
	public void onBackPressed()
	{
		finish();
	}
	
	@Override
	public void finish()
	{
		BlockerActivityUtil.finishBlockerActivity(mBlockerParam);
		super.finish();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
	}
}
