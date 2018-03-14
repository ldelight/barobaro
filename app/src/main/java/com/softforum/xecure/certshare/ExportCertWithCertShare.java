package com.softforum.xecure.certshare;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.softforum.xecure.core.CoreWrapper;
import com.softforum.xecure.ui.crypto.XecureSmartCertMgr;
import com.softforum.xecure.util.EnvironmentConfig;
import com.softforum.xecure.util.XDetailData;
import com.softforum.xecurecertshare.client.XCSCertificate;
import com.softforum.xecurecertshare.client.XCSMobileUpload;
import com.softforum.xecurecertshare.util.XCSError;

import com.barocredit.barobaro.R;

public class ExportCertWithCertShare extends Activity {

	// input keys
	public static final String mMediaIDKey = "export_cert_media_id_key";
	public static final String mSelectedCertDataKey = "export_cert_selected_cert_data_key";
	
	public static final String mRandomValueKey = "sign_cert_password_random_value_key";
	public static final String mE_ncryptedDataKey = "sign_cert_password_e_ncrypted_data_key";
	
	private XCSCertificate mXCSCertificate = null;
	private XCSMobileUpload mXCSMobileUpload = null;
	
	private String mEncryptedData = null;
	private byte[] mRandomValue = new byte[20];
	
	private EditText mAuthenticationcodeView = null;
	private ProgressDialog mProgressDialog = null;
	private Handler mViewControlHandler = null;
	private String mAuthenticationcode = null;
	private final int mAuthenticationcodeLength = 12;
	private int mConnectServerResultFlag = 0;
	
	/* 선택한 인증서의 byteArray */
	private byte[] mCertificateDER = null;
	private byte[] mKeyDER = null;
	private byte[] mKMCertificateDER = null;
	private byte[] mKMKeyDER = null;
	
	/* setting Flag */
	private int mResultFlag;
	
	private final int mGetEnticateCodeFail = 1;
	private final int mExportCertificateFail = 2;
	private final int mExportCertificateSuccess = 3;
	
	/* message code setting */
	private String mAlertMessage = "";
	private String mErrorMessage = "";
	private String mProgressDialogMessage = "";

	/* Thread */
	private Thread mExportAuthenticationCodeThread = null;
	private Thread mConnectServerThread = null;
	private Thread mExportCertificateThread = null;
	
	/* AuthenticateCode */
	private String mAuthenticationcode1 = null;
	private String mAuthenticationcode2 = null;
	private String mAuthenticationcode3 = null;
	
	private int mMediaID;
	private XDetailData mSelectedData = null;

	/* Error */
	private XCSError mError = new XCSError();
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.export_cert_with_certshare);
		
		mViewControlHandler = new Handler();
		
		/* 사용자가 선택한 인증서 정보 셋팅 */
		mMediaID = getIntent ().getIntExtra (mMediaIDKey, -1);
		
		//if(EnvironmentConfig.mXecureKeypadEncryptionUsage)
		mEncryptedData = getIntent ().getStringExtra(ExportCertWithCertShare.mE_ncryptedDataKey);
		mRandomValue = getIntent ().getByteArrayExtra (ExportCertWithCertShare.mRandomValueKey);
		
		mSelectedData = new XDetailData(getIntent ().getStringArrayExtra (mSelectedCertDataKey), XDetailData.TYPE_CERT_SIMPLE);

		/*-----------------------------------------------------------------------------*
		 * 사용자가 선택한 인증서 정보 화면에 표시.
		 *-----------------------------------------------------------------------------*/
		ImageView aIcon = (ImageView) findViewById (R.id.icon);
		TextView aMainMsg = (TextView) findViewById (R.id.main_msg);
		TextView aSubMsg1 = (TextView) findViewById (R.id.sub_msg1);
		TextView aSubMsg2 = (TextView) findViewById (R.id.sub_msg2);

		if ("0".equals (mSelectedData.getValue (XDetailData.CERT_KEY_SIMPLE.STATE)))
		{
			aIcon.setImageResource (R.drawable.cert_state_normal);
		}
		else if ("1".equals (mSelectedData.getValue (XDetailData.CERT_KEY_SIMPLE.STATE)))
		{
			aIcon.setImageResource (R.drawable.cert_state_update);
		}
		else if ("2".equals (mSelectedData.getValue (XDetailData.CERT_KEY_SIMPLE.STATE)))
		{
			aIcon.setImageResource (R.drawable.cert_state_revoke);
		}

		aMainMsg.setText (mSelectedData.getValue (XDetailData.CERT_KEY_SIMPLE.SUBJECT_RDN));
		aSubMsg1.setText (mSelectedData.getKeyText (XDetailData.CERT_KEY_SIMPLE.ISSUER_RDN)
				+ " : " + mSelectedData.getValue (XDetailData.CERT_KEY_SIMPLE.ISSUER_RDN));
		aSubMsg2.setText (mSelectedData.getKeyText (XDetailData.CERT_KEY_SIMPLE.TO) + " : "
				+ mSelectedData.getValue (XDetailData.CERT_KEY_SIMPLE.TO));

		// 웾쥬 사용
		//printScreenWebView ();

		/*-----------------------------------------------------------------------------*
		 * 사용자가 선택한 인증서의 byteArray를 가져온다.
		 *-----------------------------------------------------------------------------*/
		getCertByteArray ();

		/*-----------------------------------------------------------------------------*
		 * 서버로부터 인증번호를 가져올 쓰레드를 호출한다.
		 *-----------------------------------------------------------------------------*/
		getAuthenticationCode ();

		/*-----------------------------------------------------------------------------*
		 * 내보내기 버튼 클릭시 이벤트 핸들러 설정
		 *-----------------------------------------------------------------------------*/
		Button aOKButton = (Button) findViewById (R.id.top_right_button);
		aOKButton.setOnClickListener (new View.OnClickListener ()
		{
			public void onClick (View v)
			{
				exportCertificate ();
			}
		});
	}

	/*
	private class WebViewClientClass extends WebViewClient
	{
		@Override
		public boolean shouldOverrideUrlLoading (WebView view, String url)
		{
			view.loadUrl (url);
			return true;
		}
	}

	private void printScreenWebView ()
	{
		WebView aWeb;
		
		aWeb = (WebView) findViewById (R.id.ExportCertificate_WebView);

		// 환경설정 클래스에서 페이지주소
		// - http://reaver.softforum.com:8087/xcs/XCSExportCertificate.html 로 설정.
		aWeb.loadUrl (EnvironmentConfig.mExportCertWithCertShareInfoPage);
		aWeb.setWebViewClient (new WebViewClientClass ());
	}
	*/
	
	private void getCertByteArray ()
	{
		String aSubjectRDN = null;
		
		aSubjectRDN = mSelectedData.getValue (XDetailData.CERT_KEY_SIMPLE.SUBJECT_RDN);

		/* 사용자가 선택한 인증서 바이너리의 byteArray 값을 가져온다. */
		if (aSubjectRDN != null && aSubjectRDN.length () > 1)
		{
			mCertificateDER = CoreWrapper.getExportRawSignCert (mMediaID, aSubjectRDN);
			mKeyDER = CoreWrapper.getExportRawSignKey (mMediaID, aSubjectRDN);
			mKMCertificateDER = CoreWrapper.getExportRawKmCert (mMediaID, aSubjectRDN);
			mKMKeyDER = CoreWrapper.getExportRawKmKey (mMediaID, aSubjectRDN);
		}
		else
		{
			mErrorMessage = "인증서 파일을 가져오는데 실패하였습니다.";
			mResultFlag = mExportCertificateFail;
			ShowAlertDialog (mErrorMessage, mResultFlag);
		}
	}
	
	private void ShowAlertDialog (String pAlertMessage, final int pResultFlag)
	{
		if (mProgressDialog != null)
		{
			mProgressDialog.dismiss ();	
		}
		mProgressDialog = null;

		mAlertMessage = pAlertMessage;

		/*-----------------------------------------------------------------------------*
		 * 성공이 아닌경우 에러코드를 셋팅하여 준다.
		 *-----------------------------------------------------------------------------*/
		if (pResultFlag != mExportCertificateSuccess)
		{
			mAlertMessage += '\n';
			mAlertMessage += "Result : " + mError.getLastErrorCode ();
		}

		mViewControlHandler.post (new Runnable()
		{
			public void run ()
			{
				try
				{
					new AlertDialog.Builder (ExportCertWithCertShare.this)
					.setCancelable (false)
					.setMessage (mAlertMessage)
					.setPositiveButton ("확인", new DialogInterface.OnClickListener ()
					{
						public void onClick (DialogInterface dialog, int which)
						{
							/*---------------------------------------------------------*
							 * 사용된 객체의 메모리를 해제하여 주고 Result 셋팅한다.
							 *---------------------------------------------------------*/
							moveBackScreenIntent (pResultFlag);
						}
					})
					.show ();
				}
				catch (NullPointerException e)
				{
					//Log.d ("XecureSmart", "Exception in alert dialog progress.");
					//XSLog.e("XecureCertShare " + "Exception in alert dialog progress.");
				}
			}
		});
	}
	
	private void moveBackScreenIntent (int pResultFlag)
	{
		Intent aIntent = null;
		
		if (mConnectServerResultFlag == 1)
		{
			mXCSMobileUpload.releaseObject ();
		}

		if (mExportAuthenticationCodeThread != null && mExportAuthenticationCodeThread.isAlive ())
		{
			mExportAuthenticationCodeThread.interrupt ();
		}

		if (mConnectServerThread != null && mConnectServerThread.isAlive ())
		{
			mConnectServerThread.interrupt ();
		}

		if (mExportCertificateThread != null && mExportCertificateThread.isAlive ())
		{
			mExportCertificateThread.interrupt ();
		}

		mXCSMobileUpload = null;
		mXCSCertificate = null;
		mCertificateDER = null;
		mKeyDER = null;
		mKMCertificateDER = null;
		mKMKeyDER = null;

		/*-----------------------------------------------------------------------------*
		 * 결과값 리턴.
		 *  - 인증서내 보내기 취소 : 0. 
		 *  - 인증번호 생성 실패 : mGetEnticateCodeFail.
		 *  - 인증서 내보내기 실패 : mExportCertificateFail.
		 *  - 인증서 내보내기 성공 : mExportCertificateSuccess.
		 *-----------------------------------------------------------------------------*/
		aIntent = new Intent();
		if (pResultFlag == 0)
		{
			aIntent.putExtra (XecureSmartCertMgr.mOperationResultKey, XecureSmartCertMgr.mResultForExportCertByXCSCancel);
		}
		else
		{
			if (pResultFlag == mGetEnticateCodeFail)
			{
				aIntent.putExtra (XecureSmartCertMgr.mOperationResultKey, XecureSmartCertMgr.mResultForExportCertByXCSCreateCNumFail);
			}
			else if (pResultFlag == mExportCertificateFail)
			{
				aIntent.putExtra (XecureSmartCertMgr.mOperationResultKey, XecureSmartCertMgr.mResultForExportCertByXCSFail);
			}
			else if (pResultFlag == mExportCertificateSuccess)
			{
				aIntent.putExtra (XecureSmartCertMgr.mOperationResultKey, XecureSmartCertMgr.mResultForExportCertByXCSOK);
			}
		}
		setResult (Activity.RESULT_OK, aIntent);
		finish ();
	}
	
	private void getAuthenticationCode ()
	{
		/*******************************************************************************/

		/*-----------------------------------------------------------------------------*
		 * 서버로 부터 받아온 12자리의 인증번호를 화면에 뿌려 준다.
		 *-----------------------------------------------------------------------------*/
		mProgressDialogMessage = "인증번호를 가져오는 중 입니다.";
		ShowProgressDialog (mProgressDialogMessage);

		mExportAuthenticationCodeThread = new Thread(new Runnable()
		{
			public void run ()
			{
				int mConnectServerCheckTimer = 0;

				try
				{
					/*-----------------------------------------------------------------*
					 * 서버와의 접속을 위해 쓰레드를 구동한다.(접속실패시 무한대기 방지)
					 *-----------------------------------------------------------------*/
					connectServerThread ();

					Thread.sleep (500);
					if (mConnectServerResultFlag == 1)
					{
						mAuthenticationcode = mXCSMobileUpload.getAuthenticateCode ();
					}
					/*-----------------------------------------------------------------*
					 * 서버와의 접속 타이머 동작 (3초 이상 접속못할시 실패로 간주한다.)
					 *-----------------------------------------------------------------*/
					else
					{
						for (int i = 0; i <= 3; i++)
						{
							if (mConnectServerResultFlag == 1)
							{
								mConnectServerCheckTimer = 0;
								mAuthenticationcode = mXCSMobileUpload.getAuthenticateCode ();
								break;
							}

							if (mConnectServerCheckTimer <= 3)
							{
								Thread.sleep (1000);
							}

							if (mConnectServerCheckTimer == 3)
							{
								mConnectServerCheckTimer = 0;
								mErrorMessage = "인증번호 가져오기에 실패하였습니다.";
								mResultFlag = mGetEnticateCodeFail;
								ShowAlertDialog (mErrorMessage, mResultFlag);
							}
							mConnectServerCheckTimer++;
						}
					}
				}
				catch (Throwable e)
				{
					mErrorMessage = "인증번호 가져오기에 실패하였습니다.";
					mResultFlag = mGetEnticateCodeFail;
					ShowAlertDialog (mErrorMessage, mResultFlag);
				}
				/* 작업이 완료되면 프로그레스 바를 없애준다 */
				if (mProgressDialog != null)
				{
					mProgressDialog.dismiss ();
				}

				if (mAuthenticationcode == null)
				{
					mErrorMessage = "인증번호 가져오기에 실패하였습니다.";
					mResultFlag = mGetEnticateCodeFail;
					ShowAlertDialog (mErrorMessage, mResultFlag);
				}
				else if (mAuthenticationcode.length () == mAuthenticationcodeLength)
				{
					settingAuthcode (mAuthenticationcode);
					printScreenAuthCode ();
				}
				else
				{
					mErrorMessage = "인증번호 가져오기에 실패하였습니다.";
					mResultFlag = mGetEnticateCodeFail;
					ShowAlertDialog (mErrorMessage, mResultFlag);
				}

			}
		});
		mExportAuthenticationCodeThread.start ();

		/*******************************************************************************/
	}
	
	private void ShowProgressDialog (String pProgressDialogMessage)
	{
		if (mProgressDialog == null)
		{
			mProgressDialog = ProgressDialog.show (this, "Waiting", pProgressDialogMessage, true, false);
			mProgressDialog.setCanceledOnTouchOutside (false);
		}
	}
	
	private void connectServerThread ()
	{
		mConnectServerThread = new Thread(new Runnable()
		{
			public void run ()
			{
				/*-----------------------------------------------------------------*
				 * 환경설정 클래스에서 XecureCertShare 서버주소
				 *  - http://reaver.softforum.com:8087/xcs 로 설정.
				 *-----------------------------------------------------------------*/
				mXCSMobileUpload = new XCSMobileUpload(EnvironmentConfig.mCertShareAddr);
				mConnectServerResultFlag = 1;
				
				//if(EnvironmentConfig.mXecureKeypadEncryptionUsage)
				mXCSMobileUpload.setSecureKeypadVendor ("xkeypad");
			}
		});
		mConnectServerThread.start ();

		/*******************************************************************************/
	}
	
	private void settingAuthcode (String pAuthenticationcode)
	{
		int i, j = 0;
		String aTempKey = "";
		
		for (i = 1; i <= pAuthenticationcode.length (); i++)
		{
			aTempKey += pAuthenticationcode.charAt (i - 1);
			if ((i % 4 == 0) && (i != 0))
			{
				if (j == 0)
				{
					mAuthenticationcode1 = aTempKey;
				}
				if (j == 1)
				{
					mAuthenticationcode2 = aTempKey;
				}
				if (j == 2)
				{
					mAuthenticationcode3 = aTempKey;
				}
				aTempKey = "";
				j++;
			}
		}
	}
	
	private void printScreenAuthCode ()
	{
		mViewControlHandler.post (new Runnable()
		{
			public void run ()
			{
				try
				{
					/*-----------------------------------------------------------------*
					 * 서버로 부터 받아온 12자리의 인증번호를 화면에 뿌려 준다.
					 *-----------------------------------------------------------------*/
					mAuthenticationcodeView = (EditText) findViewById (R.id.GetAuthenticationcode_Export1);
					mAuthenticationcodeView.setText (mAuthenticationcode1);

					mAuthenticationcodeView = (EditText) findViewById (R.id.GetAuthenticationcode_Export2);
					mAuthenticationcodeView.setText (mAuthenticationcode2);

					mAuthenticationcodeView = (EditText) findViewById (R.id.GetAuthenticationcode_Export3);
					mAuthenticationcodeView.setText (mAuthenticationcode3);
				}
				catch (NullPointerException e)
				{
					//Log.d ("XecureSmart", "Fail to print screen authentication code.");
					//XSLog.e("XecureCertShare " + "Fail to print screen authentication code.");
				}
			}
		});
	}
	
	@Override
	public void onBackPressed ()
	{
		/*-----------------------------------------------------------------------------*
		 * 뒤로 가기 하면 사용된 객체의 메모리를 해제하여 준다.
		 *-----------------------------------------------------------------------------*/
		moveBackScreenIntent (0);
	}
	
	private void exportCertificate ()
	{
		mProgressDialogMessage = "인증서를 내보내는 중입니다.";
		mProgressDialog = ProgressDialog.show (this, "Waiting", mProgressDialogMessage, true, true);

		/*-----------------------------------------------------------------------------*
		 * 인증서를 내보내는 쓰레드.
		 *-----------------------------------------------------------------------------*/
		mExportCertificateThread = new Thread(new Runnable()
		{
			public void run ()
			{
				//int aResult = 0;
				int aResult = -1;
				//String aPasswordString = null;

				try
				{
					/* 인증서 Initialize */
					mXCSCertificate = new XCSCertificate();
					mXCSCertificate.initialize (mCertificateDER, mKeyDER, mKMCertificateDER, mKMKeyDER);
					
					//if(EnvironmentConfig.mXecureKeypadEncryptionUsage)
					if(mEncryptedData != null && mEncryptedData.length() > 0) {
						aResult = mXCSMobileUpload.exportCertificate(mEncryptedData, mRandomValue, mXCSCertificate);
					}
					
					if (aResult != 0)
					{
						mErrorMessage = "인증서 내보내기에 실패하였습니다.";
						mResultFlag = mExportCertificateFail;
						ShowAlertDialog (mErrorMessage, mResultFlag);
					}
					else
					{
						mErrorMessage = mXCSCertificate.getSubjectRDN () + '\n' + "내보내기에 성공하였습니다." + '\n' + 
								"이제 PC에서 인증서 가져오기 동작을 수행해 주세요.";

						mResultFlag = mExportCertificateSuccess;
						ShowAlertDialog (mErrorMessage, mResultFlag);
					}
				}
				catch (Throwable ex)
				{
					mErrorMessage = "인증서 내보내기에 실패하였습니다.";
					mResultFlag = mExportCertificateFail;
					ShowAlertDialog (mErrorMessage, mResultFlag);
				}
			}
		});

		mExportCertificateThread.start ();
	}
}
