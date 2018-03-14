package com.softforum.xecure.keypad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.softforum.xecure.crypto.CertMgr;
import com.softforum.xecure.util.EnvironmentConfig;
import com.softforum.xecure.util.XActivityIDs;
import com.softforum.xecure.util.XCoreUtil;
import com.softforum.xecure.util.XDetailData;
import com.softforum.xecure.util.XUtil;
import com.softforum.xecurekeypad.XKConstants;
import com.softforum.xecurekeypad.XKEditText;

import java.security.SecureRandom;

import com.barocredit.barobaro.R;


public class SignCertPasswordWindowWithXK extends Activity {
	public static final int mSignCertPasswordWindowID = XActivityIDs.mSignCertPasswordWindowID;	
	
	// input keys
	public static final String mMediaIDKey = "sign_cert_password_media_id_key";
	public static final String mSelectedCertDataKey = "sign_cert_password_selected_cert_data_key";
	
	// output keys
	public static final String m_P_assW_ordKey = "sign_cert_password_password_key";
	
	// XecureKeypad 에서 암호화된 결과 처리할 경우 필요한 키 값.
	public static final String mRandomValueKey = "sign_cert_password_random_value_key";
	public static final String mE_ncryptedDataKey = "sign_cert_password_e_ncrypted_data_key";
	
	public static final String mCallModeKey = "call_mode_key";
	
	public static final int RESULT_PASSWD_FAIL = 2;
	
	private int mMediaID;
	private XDetailData mSelectedData;
	private byte[] m_P_assW_ord = null;
	private int m_P_assW_ordTryCount;
	
	private String mCallMode = null;
	
	private XCoreUtil mCoreUtil = new XCoreUtil();

	/* XecureKeyPad */
	private XKEditText m_P_assW_ordTextView = null;
	
	private String mEncryptedData = null;
	private byte[] mRandomValue = new byte[20];
	
	//
	public Handler mHandler = new Handler();
	
	private TextView mTopDescText;

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate (Bundle savedInstanceState)
	{
		super.onCreate (savedInstanceState);
		
		setContentView (R.layout.crypto_sign_cert_password_window_xk);
		
		//
		mTopDescText = (TextView) findViewById(R.id.top_desc_text);
		
		m_P_assW_ordTryCount = 0;
		mMediaID = getIntent ().getIntExtra (mMediaIDKey, -1);
		mCallMode = getIntent ().getStringExtra (SignCertPasswordWindowWithXK.mCallModeKey);
		mSelectedData = new XDetailData(getIntent ().getStringArrayExtra (mSelectedCertDataKey), XDetailData.TYPE_CERT_SIMPLE);
		
		ImageView aIcon = (ImageView) findViewById (R.id.icon);
		TextView aMainMsg = (TextView) findViewById (R.id.main_msg);
		TextView aSubMsg1 = (TextView) findViewById (R.id.sub_msg1);
		TextView aSubMsg2 = (TextView) findViewById (R.id.sub_msg2);

		if ("0".equals (mSelectedData.getValue (XDetailData.CERT_KEY_SIMPLE.STATE)))
		{
			aIcon.setImageResource (R.drawable.cert_state_normal);
		}
		else if("1".equals (mSelectedData.getValue (XDetailData.CERT_KEY_SIMPLE.STATE)))
		{
			aIcon.setImageResource (R.drawable.cert_state_update);
		}
		else if ("2".equals (mSelectedData.getValue (XDetailData.CERT_KEY_SIMPLE.STATE)))
		{
			aIcon.setImageResource (R.drawable.cert_state_revoke);
		}

		aMainMsg.setText (XUtil.getCNFromRDN (mSelectedData.getValue (XDetailData.CERT_KEY_SIMPLE.SUBJECT_RDN)));
		aSubMsg1.setText (mSelectedData.getKeyText (XDetailData.CERT_KEY_SIMPLE.ISSUER_RDN) +" : "
					+ mSelectedData.getValue (XDetailData.CERT_KEY_SIMPLE.ISSUER_RDN));
		aSubMsg2.setText (mSelectedData.getKeyText (XDetailData.CERT_KEY_SIMPLE.TO) +" : "
					+ mSelectedData.getValue (XDetailData.CERT_KEY_SIMPLE.TO));
		
		/*-----------------------------------------------------------------------------*
		 * XecurekeyPad 적용 설정.
		 *-----------------------------------------------------------------------------*/
		setKeyPad ();
		
		//완료 버튼 
		Button aOKButton = (Button) findViewById (R.id.top_right_button);
		aOKButton.setOnClickListener (new View.OnClickListener ()
		{			
			public void onClick (View v)
			{
				onOKButtonClick (v);
			}
		});	
	}
	
	private void setKeyPad ()
	{
		int aKeypadType = 0;
		int aKeypadViewType = 0;
		
		m_P_assW_ordTextView = (XKEditText) findViewById (R.id.password_edittext);

		/*-----------------------------------------------------------------------------*
		 * 환경 설정에 맞춰서 키패드 화면 설정.
		 *-----------------------------------------------------------------------------*/
		if (EnvironmentConfig.mXecureKeypadFullViewUsage)
		{
			/*-------------------------------------------------------------------------*
			 * XecureKeypad Full View 설정.
			 * 	- 전체화면일 경우에는 subtitle을 XML 파일에 설정하거나,
			 *    setSubTitle 함수로 설정.
			 *-------------------------------------------------------------------------*/
			aKeypadViewType = XKConstants.XKViewType.XKViewTypeFullView;
			m_P_assW_ordTextView.setSubTitle ("비밀번호");
		}
		else if (EnvironmentConfig.mXecureKeypadNormalViewUsage)
		{
			/*-------------------------------------------------------------------------*
			 * XecureKeypad Normal View 설정.
			 * 	- 일반 키패드 뷰일 경우에는 XML 파일의 Root Layout id를 추가해주고,
			 * 	 해당 id를 키패드에 넘겨줘야함.
			 * 	- 이 부분은 EditText를 키패드에 가려지지 않게 하기 위해 필요함.
			 *-------------------------------------------------------------------------*/
			aKeypadViewType = XKConstants.XKViewType.XKViewTypeNormalView;
			m_P_assW_ordTextView.setLayoutIdentifier (R.id.xk_keypad_root_layout);
		}
		
		/*-----------------------------------------------------------------------------*
		 * XecureKeypad Type & View 설정. (쿼티 타입 설정)
		 *-----------------------------------------------------------------------------*/
		aKeypadType = XKConstants.XKKeypadType.XKKeypadTypeQwerty;
		
		m_P_assW_ordTextView.setXKViewType (aKeypadViewType);
		m_P_assW_ordTextView.setXKKeypadType (aKeypadType);

		if (aKeypadViewType == XKConstants.XKViewType.XKViewTypeFullView)
		{
			/*----------------------------------------------------------------------------------*
			 * 입력완료 & 취소 버튼 사용.
			 *----------------------------------------------------------------------------------*/
			m_P_assW_ordTextView.setUseInputButton (true);
			m_P_assW_ordTextView.setSubTitle (getString (R.string.password));
		}
	}
	
	private void onOKButtonClick (View v)
	{
		int aErrorCode = 0;
		CertMgr aCertMgr = null;
		
		int aVerifyResult = 0;
		
		/*-----------------------------------------------------------------------------*
		 * reset Error.
		 *-----------------------------------------------------------------------------*/
		mCoreUtil.resetError ();
		
		//암호화 모드 사용. 
		SecureRandom sr = new SecureRandom();
		sr.nextBytes(mRandomValue);
		
		mEncryptedData = m_P_assW_ordTextView.getEncryptedData(mRandomValue);
		
		//
		if(mEncryptedData == null || mEncryptedData.length() == 0)
		{
			//aTopView.setDescription (getString (R.string.plugin_input_nothing)); /*"입력하신 내용이 없습니다."*/
			showToast(getString(R.string.plugin_input_nothing));
			mTopDescText.setText(getString(R.string.plugin_input_nothing));
			return;
		}
		
		aCertMgr = CertMgr.getInstance ();
		
		
		////////////////////////////////////////////
		// 인증서쌍 유효한지 체크 
		//
		int parResult = aCertMgr.checkValidCertPair(mMediaID, mSelectedData.getValue (XDetailData.CERT_KEY_SIMPLE.SUBJECT_RDN));
		
		aErrorCode = mCoreUtil.lastErrCode ();
		
		if (aErrorCode == 22000017) {
			//
			showToast(mCoreUtil.lastErrMsg());
			
			//
			
		}
		////////////////////////////////////////////
		
		
		
		if(!(mEncryptedData == null && mEncryptedData.length() == 0))
		{
			aVerifyResult = aCertMgr.verifyPassword (mMediaID, mSelectedData.getValue (XDetailData.CERT_KEY_SIMPLE.SUBJECT_RDN), mRandomValue, mEncryptedData);
		}
		
		if(aVerifyResult != 0)
		{
			/*-------------------------------------------------------------------------*
		     * 암호용 인증서 비밀번호가 틀렸을 경우 스킵처리 부분.
		     * - lastErrCode 값이 22000015 일 경우.
		     *-------------------------------------------------------------------------*/
			aErrorCode = mCoreUtil.lastErrCode ();
				
			if (aErrorCode != 22000015)
			{
				//aTopView.setDescription (mCoreUtil.lastErrMsg ()); /* "인증서 암호가 올바르지 않습니다." */
				showToast(mCoreUtil.lastErrMsg());
				mTopDescText.setText(mCoreUtil.lastErrMsg());
				
				if (++m_P_assW_ordTryCount >= EnvironmentConfig.m_P_assW_ordTryLimit)
				{
					setResult (RESULT_PASSWD_FAIL);
					finish ();
				}
				return;
			}
		}
		
		//
		Intent aIntent = new Intent();
		
		/*---------------------------------------------------------------------*
	     * 암호화 설정되어 있는 경우
		 * 랜덤 값 & 암호화값 & SecureKey 값 넘겨주자.
	 	 *---------------------------------------------------------------------*/
		
		aIntent.putExtra (SignCertPasswordWindowWithXK.mRandomValueKey, mRandomValue);
		aIntent.putExtra (SignCertPasswordWindowWithXK.mE_ncryptedDataKey, mEncryptedData);
		
		setResult (Activity.RESULT_OK, aIntent);
		finish ();
		
		/*-----------------------------------------------------------------------------*
		 * 사용된 byte[] 초기화.
		 *-----------------------------------------------------------------------------*/
		XUtil.resetByteArray (m_P_assW_ord, mRandomValue);
	}
	
	@Override
	public void onBackPressed ()
	{
		/*-----------------------------------------------------------------------------*
		 * 사용된 byte[] 초기화.
		 *-----------------------------------------------------------------------------*/
		XUtil.resetByteArray (m_P_assW_ord, mRandomValue);
		
		super.onBackPressed ();
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data)
	{
		if (resultCode == XKConstants.XKKeypadError.MakeIndexError)
		{
			/*--------------------------------------------------------------------------------------*
			 * XecureKeypad가 종료되면 Result 값을 받아 처리해야함.
			 * 	- RequestCode는 XKConstants.XKKeypadRequestCode로 설정.
			 *--------------------------------------------------------------------------------------*/
			Toast.makeText (SignCertPasswordWindowWithXK.this, "인덱스 생성에 실패하였습니다.", Toast.LENGTH_SHORT).show ();
		}
		else if (resultCode == Activity.RESULT_CANCELED
				&& requestCode != XKConstants.XKKeypadRequestCode)
		{
			setResult (Activity.RESULT_CANCELED);
			finish ();
		} else {
			// 입력완료 버튼을 클릭했을 경우
			if(resultCode == Activity.RESULT_OK && requestCode == XKConstants.XKKeypadRequestCode) {
				//
				onOKButtonClick(null);
			}
		}
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
				Toast.makeText(SignCertPasswordWindowWithXK.this, message, Toast.LENGTH_LONG).show();
			}
		});
	}
}
