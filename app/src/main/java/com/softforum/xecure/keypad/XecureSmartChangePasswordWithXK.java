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
import com.softforum.xecure.ui.crypto.SignCertSelectWindow;
import com.softforum.xecure.ui.crypto.XecureSmartCertMgr;
import com.softforum.xecure.util.EnvironmentConfig;
import com.softforum.xecure.util.XActivityIDs;
import com.softforum.xecure.util.XCoreUtil;
import com.softforum.xecure.util.XDetailData;
import com.softforum.xecure.util.XErrorCode;
import com.softforum.xecure.util.XUtil;
import com.softforum.xecurekeypad.XKConstants;
import com.softforum.xecurekeypad.XKEditText;

import java.security.SecureRandom;

import com.barocredit.barobaro.R;

public class XecureSmartChangePasswordWithXK extends Activity {

	public static final int mXecureSmartChangePasswordID = XActivityIDs.mChangePasswordDialogID; 

	public static final String mMediaIDKey 			= "xecure_smart_changepw_media_id_key";
	public static final String mSelectedCertDataKey	= "xecure_smart_changepw_data_key";
	
	public static final String mNewPasswordKey	= SignCertPasswordWindowWithXK.m_P_assW_ordKey;
	
	public static final int RESULT_PASSWD_FAIL = 2;
	
	private int mMediaID;
	private int m_P_assW_ordTryCount = 0;
	private XDetailData mSelectedData;
	
	private XCoreUtil mCoreUtil = new XCoreUtil();
	
	private byte[] mOldPassword = null;
	private byte[] mNewPassword = null;
	private byte[] mNewPasswordConfirm = null;
	
	private String mEncryptedData = null;
	private String mNewEncryptedData = null;
	private String mConfirmEncryptedData = null;
	private byte[] mRandomValue = new byte[20];
	
	private String mCallMode;
	private int mDetailDataType = XDetailData.TYPE_CERT;
	private int mDetailDataCertSubjectKey = XDetailData.CERT_KEY.SUBJECT_RDN;
	private int mDetailDataCertIssuerKey = XDetailData.CERT_KEY.ISSUER_RDN;
	private int mDetailDataCertToKey = XDetailData.CERT_KEY.TO;
	
	/* XecureKeyPad */
	private XKEditText mOldPasswordTextView = null;
	private XKEditText mNewPasswordTextView = null;
	private XKEditText mNewPasswordConfirmTextView = null;
	
	private final int mResultForNotExistPassword = 10;
	private final int mResultForVerifyPasswordFail = 11;
	private final int mResultForNotCorrectPassword = 12;
	private final int mResultForEqualPassword = 13;
	
	//
	public Handler mHandler = new Handler();
	
	private TextView mTopDescText;
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView (R.layout.crypto_xecure_smart_change_password_xk);
		
		//
		mTopDescText = (TextView) findViewById(R.id.top_desc_text);
		
		mMediaID = getIntent().getIntExtra(mMediaIDKey, -1);
		mCallMode = getIntent().getStringExtra(SignCertSelectWindow.mCallModeKey) ;
		
		mSelectedData = new XDetailData( getIntent().getStringArrayExtra(mSelectedCertDataKey), mDetailDataType );
		
		ImageView aIcon = (ImageView) findViewById(R.id.icon);
		TextView aMainMsg = (TextView) findViewById(R.id.main_msg);
		TextView aSubMsg1 = (TextView) findViewById(R.id.sub_msg1);
		TextView aSubMsg2 = (TextView) findViewById(R.id.sub_msg2);

		if( "0".equals( mSelectedData.getValue(XDetailData.CERT_KEY.STATE) ) ){
			aIcon.setImageResource( R.drawable.cert_state_normal );
		} else if ( "1".equals( mSelectedData.getValue(XDetailData.CERT_KEY.STATE) ) ) {
			aIcon.setImageResource( R.drawable.cert_state_update );
		} else if ( "2".equals( mSelectedData.getValue(XDetailData.CERT_KEY.STATE) ) ) {
			aIcon.setImageResource( R.drawable.cert_state_revoke );
		}

		aMainMsg.setText( mSelectedData.getValue(mDetailDataCertSubjectKey) );
		aSubMsg1.setText( mSelectedData.getKeyText(mDetailDataCertIssuerKey) +" : "
				+ mSelectedData.getValue(mDetailDataCertIssuerKey) );
		aSubMsg2.setText( mSelectedData.getKeyText( mDetailDataCertToKey ) +" : "
				+mSelectedData.getValue(mDetailDataCertToKey) );
		
		// 암호화 모드 사용. 
		//if(EnvironmentConfig.mXecureKeypadEncryptionUsage)
		SecureRandom sr = new SecureRandom();
		sr.nextBytes(mRandomValue);	
		
		/*-----------------------------------------------------------------------------*
		 * XecurekeyPad 적용 설정.
		 *-----------------------------------------------------------------------------*/
		setKeyPad();

		Button aOKButton = (Button) findViewById(R.id.top_right_button);
		aOKButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onOKButtonClick(v);
			}
		});
	}
	
	private void setKeyPad()
	{
		int aKeypadType = 0;
		int aKeypadViewType = 0;
		
		mOldPasswordTextView = (XKEditText) findViewById (R.id.password_edittext);
		mNewPasswordTextView = (XKEditText) findViewById (R.id.new_password_edittext);
		mNewPasswordConfirmTextView = (XKEditText) findViewById (R.id.new_password_confirm_edittext);
		
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
			mOldPasswordTextView.setSubTitle ("비밀번호");
			mNewPasswordTextView.setSubTitle ("새 비밀번호");
			mNewPasswordConfirmTextView.setSubTitle ("새 비밀번호 확인");
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
			mOldPasswordTextView.setLayoutIdentifier (R.id.xk_keypad_root_layout);
			mNewPasswordTextView.setLayoutIdentifier (R.id.xk_keypad_root_layout);
			mNewPasswordConfirmTextView.setLayoutIdentifier (R.id.xk_keypad_root_layout);
		}
		
		if (aKeypadViewType == XKConstants.XKViewType.XKViewTypeFullView)
		{
			/*----------------------------------------------------------------------------------*
			 * 입력완료 & 취소 버튼 사용.
			 *----------------------------------------------------------------------------------*/
			mOldPasswordTextView.setUseInputButton (true);
			mNewPasswordTextView.setUseInputButton (true);
			mNewPasswordConfirmTextView.setUseInputButton (true);
			
			mOldPasswordTextView.setSubTitle (getString (R.string.password));
			mNewPasswordTextView.setSubTitle (getString (R.string.new_password));
			mNewPasswordConfirmTextView.setSubTitle (getString (R.string.new_password_confirm));
		}
		
		/*-----------------------------------------------------------------------------*
		 * XecureKeypad Type & View 설정. (쿼티 타입 설정)
		 *-----------------------------------------------------------------------------*/
		aKeypadType = XKConstants.XKKeypadType.XKKeypadTypeQwerty;
		
		mOldPasswordTextView.setXKViewType (aKeypadViewType);
		mOldPasswordTextView.setXKKeypadType (aKeypadType);
		mNewPasswordTextView.setXKViewType (aKeypadViewType);
		mNewPasswordTextView.setXKKeypadType (aKeypadType);
		mNewPasswordConfirmTextView.setXKViewType (aKeypadViewType);
		mNewPasswordConfirmTextView.setXKKeypadType (aKeypadType);
	}
	
	private int checkInputPasswordWithEncryption ()
	{
		int aResult = -1;
		int aErrorCode = 0;
		int aConfirmResult = 0;
		String aSubjectDN = null;
		CertMgr aCertMgr = CertMgr.getInstance ();
		
		if (mEncryptedData == null || mEncryptedData.length () == 0)
		{
			return mResultForNotExistPassword;
		}
		
		aSubjectDN = mSelectedData.getValue (mDetailDataCertSubjectKey);
		aResult = aCertMgr.verifyPassword (mMediaID, aSubjectDN, mRandomValue, mEncryptedData);
		
		if (aResult != 0)
		{
			/*-------------------------------------------------------------------------*
		     * 암호용 인증서 비밀번호가 틀렸을 경우 스킵처리 부분.
		     * - lastErrCode 값이 22000015 일 경우.
		     *-------------------------------------------------------------------------*/
			aErrorCode = mCoreUtil.lastErrCode ();
			if (aErrorCode != 22000015)
			{
				return mResultForVerifyPasswordFail;
			}
		}
		
		if (mNewEncryptedData == null || mNewEncryptedData.length () == 0)
		{
			return mResultForNotExistPassword;
		}
		
		//
		if (EnvironmentConfig.mUseNewPasswordValidCheck)
		{
			aResult = XUtil.checkPassword (mNewEncryptedData, mRandomValue, XUtil.PASSWORD_CHECK_OPTION_TEN_ALP_NUM_SC);
		} 
		else
		{
			aResult = XUtil.checkPassword (mNewEncryptedData, mRandomValue, XUtil.PASSWORD_CHECK_OPTION_EIGHT_ALP_NUM);
		}
		
		if (aResult != 0)
		{
			return aResult;
		}
		
		if (mConfirmEncryptedData == null || mConfirmEncryptedData.length () == 0)
		{
			aConfirmResult = -1;
		}
		else
		{
			if (!mNewEncryptedData.equals (mConfirmEncryptedData))
			{
				aConfirmResult = -1;
			}	
		}
		
		if (aConfirmResult != 0)
		{
			return mResultForNotCorrectPassword;
		}
		
		if (mEncryptedData.equals (mConfirmEncryptedData))
		{
			return mResultForEqualPassword;
		}
		
		return aResult;
	}
	
	private void onOKButtonClick (View v)
	{
		int aResult = 0;
		String aSubjectDN = null;
	
		Intent aIntent = null;
		CertMgr aCertMgr = CertMgr.getInstance ();
		
		aSubjectDN = mSelectedData.getValue (mDetailDataCertSubjectKey);
		
		mEncryptedData = mOldPasswordTextView.getEncryptedData(mRandomValue);
		mNewEncryptedData = mNewPasswordTextView.getEncryptedData(mRandomValue);
		mConfirmEncryptedData = mNewPasswordConfirmTextView.getEncryptedData(mRandomValue);
		
		mCoreUtil.resetError ();
		
		aResult = checkInputPasswordWithEncryption ();
		
		switch (aResult)
		{
			case mResultForNotExistPassword :
				//aTopView.setDescription (getString (R.string.plugin_input_nothing)); /* "입력하신 내용이 없습니다." */
				showToast(getString(R.string.plugin_input_nothing));
				mTopDescText.setText(getString(R.string.plugin_input_nothing));
				return;
			case mResultForVerifyPasswordFail :
				//aTopView.setDescription (mCoreUtil.lastErrMsg ()); /* "인증서 암호가 올바르지 않습니다." */
				showToast(mCoreUtil.lastErrMsg ());
				mTopDescText.setText(mCoreUtil.lastErrMsg ());
				
				if (++m_P_assW_ordTryCount >= EnvironmentConfig.m_P_assW_ordTryLimit)
				{
					setResult (RESULT_PASSWD_FAIL);
					finish ();
				}
				return;
			case mResultForNotCorrectPassword :
				//aTopView.setDescription (getString (R.string.incorrect_confirm_password)); /* "새 인증서 암호가 일치하지 않습니다." */
				showToast(getString(R.string.incorrect_confirm_password));
				mTopDescText.setText(getString(R.string.incorrect_confirm_password));
				return;
			case mResultForEqualPassword :
				//aTopView.setDescription (getString (R.string.password_renew_syntax_error)); /* "새 비밀번호와 기존 비밀번호가 일치합니다." */
				showToast(getString(R.string.password_renew_syntax_error));
				mTopDescText.setText(getString(R.string.password_renew_syntax_error));
				return;
			case XUtil.PASSWORD_FORMAT_LENGTH_ERROR :
				//aTopView.setDescription (getString (R.string.password_length_error)); /* "인증서 암호는 최소 8자 이상입니다." */
				showToast(getString(R.string.password_length_error));
				mTopDescText.setText(getString(R.string.password_length_error));
				return;
			case XUtil.PASSWORD_FORMAT_SYNTAX_ERROR :
				//aTopView.setDescription (getString (R.string.password_syntax_error)); /* "인증서 암호는 문자와 숫자를 조합하여야 합니다." */
				showToast(getString(R.string.password_syntax_error));
				mTopDescText.setText(getString(R.string.password_syntax_error));
				return;
			case XUtil.NEWPASSWORD_FORMAT_LENGTH_ERROR :
				//aTopView.setDescription (getString (R.string.newpassword_length_error)); /* "인증서 암호는 최소 10자 이상입니다." */
				showToast(getString(R.string.newpassword_length_error));
				mTopDescText.setText(getString(R.string.newpassword_length_error));
				return;
			case XUtil.NEWPASSWORD_FORMAT_SYNTAX_ERROR :
				//aTopView.setDescription (getString (R.string.newpassword_syntax_error)); /* "인증서 암호는 숫자, 문자, 특수문자를 조합하여야 합니다." */
				showToast(getString(R.string.newpassword_syntax_error));
				mTopDescText.setText(getString(R.string.newpassword_syntax_error));
				return;
			case XUtil.NEWPASSWORD_FORMAT_INVALID_ERROR :
				//aTopView.setDescription (getString (R.string.newpassword_invalid_error)); /* ' " \ | 는 인증서 암호로 쓰실수 없습니다. */
				showToast(getString(R.string.newpassword_invalid_error));
				mTopDescText.setText(getString(R.string.newpassword_invalid_error));
				return;
		}
		
		//
		
		aIntent = new Intent();
		
		//if (EnvironmentConfig.mXecureKeypadEncryptionUsage)
		aResult = aCertMgr.changePassword (mMediaID, aSubjectDN, mRandomValue, mEncryptedData, mNewEncryptedData);
		
		if (aResult == 0)
		{
			aIntent.putExtra (XecureSmartCertMgr.mOperationResultKey, 
					XecureSmartCertMgr.mResultForChangePassword);
			aIntent.putExtra (XecureSmartChangePasswordWithXK.mNewPasswordKey, mNewPassword);
		
			setResult (Activity.RESULT_OK, aIntent);
			finish ();
		}
		else
		{
			setResult (XErrorCode.XW_ERROR_RESULT_FAIL, aIntent);
		}
		
		finish ();
	}
	
	@Override
	public void finish ()
	{
		/*---------------------------------------------------------------------------------------------------*
	     * 사용된 byte[]는 초기화 해주자.
	     * finish 함수 처리 될 때 mOldPassword & mNewPassword 변수를 초기화 시켜주면 
	     * intent 에 포함된 값 역시 초기화 되는 문제 발생한다.
	     * mOldPassword & mNewPassword 변수는 트랜스키 처리 마지막에서 초기화 시켜주자.
	     *---------------------------------------------------------------------------------------------------*/
		XUtil.resetByteArray (mNewPasswordConfirm);
		
		super.finish();
	}
	
	@Override
	public void onBackPressed ()
	{
		/*-----------------------------------------------------------------------------*
		 * 사용된 byte[] 초기화.
		 *-----------------------------------------------------------------------------*/
		XUtil.resetByteArray (mOldPassword, mNewPassword, mNewPasswordConfirm, mRandomValue);
		
		super.onBackPressed ();
	}

	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data)
	{
		/*-----------------------------------------------------------------------------*
		 * XecureKeypad가 종료되면 Result 값을 받아 처리해야함.
		 * 	- RequestCode는 XKConstants.XKKeypadRequestCode로 설정.
		 *-----------------------------------------------------------------------------*/
		if (requestCode == XKConstants.XKKeypadRequestCode
			&& resultCode == Activity.RESULT_CANCELED)
		{
		}
		else if (requestCode == XKConstants.XKKeypadRequestCode
			&& resultCode == XKConstants.XKKeypadError.MakeIndexError)
		{
			Toast.makeText (XecureSmartChangePasswordWithXK.this, "인덱스 생성에 실패하였습니다.", Toast.LENGTH_SHORT).show ();
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
				Toast.makeText(XecureSmartChangePasswordWithXK.this, message, Toast.LENGTH_LONG).show();
			}
		});
	}
}
