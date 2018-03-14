package com.softforum.xecure.certshare;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.softforum.xecure.crypto.CertMgr;
import com.softforum.xecure.ui.crypto.XecureSmartCertMgr;
import com.softforum.xecure.util.BlockerActivityResult;
import com.softforum.xecure.util.BlockerActivityUtil;
import com.softforum.xecure.util.EnvironmentConfig;
import com.softforum.xecure.util.XActivityIDs;
import com.softforum.xecurecertshare.client.XCSCertificate;
import com.softforum.xecurecertshare.client.XCSMobileDownload;
import com.softforum.xecurecertshare.util.XCSError;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.barocredit.barobaro.R;


public class ImportCertWithCertShare extends Activity {
	
	public static final int mImportCertificateID = XActivityIDs.mImportCertificateID;

	private EditText mAuthenticationcodeView = null;
	private ProgressDialog mProgressDialog = null;
	private Handler mViewControlHandler = null;
	private String mAuthenticationcode = null;
	private int mConnectServerResultFlag = 0;

	/* setting Flag */
	private int mResultFlag;

	private final int mGetEnticateCodeFail = 1;
	private final int mImportCertificateFail = 2;
	private final int mImportCertificateSuccess = 3;
	private final int mAuthenticationcodeLength = 12;

	/* message code setting */
	private String mAlertMessage = "";
	private String mProgressDialogMessage = "";
	private String mErrorMessage = "";

	/* Thread */
	private Thread mImportAuthenticationCodeThread = null;
	private Thread mImportCertificateThread = null;
	private Thread mConnectServerThread = null;

	/* CertificateDER */
	private byte[] mCertificateDER = null;
	private byte[] mKeyDER = null;
	private byte[] mKMCertificateDER = null;
	private byte[] mKMKeyDER = null;

	/* XecureCertShare */
	private XCSMobileDownload mXCSMobileDownload = null;
	private XCSCertificate mXCSCertificate = null;

	/* AuthenticateCode */
	private String mAuthenticationcode1 = null;
	private String mAuthenticationcode2 = null;
	private String mAuthenticationcode3 = null;

	/* Error */
	private XCSError mError = null;

	private int mMediaID = XecureSmartCertMgr.mCERT_LOCATION_SDCARD;
	
	//
	private BlockerActivityResult mBlockerParam;
	

	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate (savedInstanceState);
		setContentView (R.layout.import_cert_with_certshare);
		
		//
		mError = new XCSError();
		
		// 웾쥬 사용
		//printScreenWebView ();
		
		// 서버로부터 인증번호를 가져올 쓰레드를 호출한다.
		getAuthenticationCode ();
		
		// 내보내기 버튼 클릭시 이벤트 핸들러 설정.
		Button aOKButton = (Button) findViewById (R.id.top_right_button);
		aOKButton.setOnClickListener (new View.OnClickListener ()
		{
			public void onClick (View v)
			{
				importCertificate ();
			}
		});
		
		// AsyncTask 처리를 위하여 블록파람 설정
		Intent receivedIntent = getIntent();
		
		mBlockerParam = BlockerActivityUtil.getParam(this, receivedIntent);
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
		
		aWeb = (WebView) findViewById (R.id.ImportCertificate_WebView);
		
		// 환경설정 클래스에서 페이지주소
		// - http://reaver.softforum.com:8087/xcs/XCSImportCertificate.html 로 설정.
		aWeb.loadUrl (EnvironmentConfig.mImportCertWithCertShareInfoPage);
		aWeb.setWebViewClient (new WebViewClientClass ());
	}
	*/
	
	private void ShowProgressDialog (String pProgressDialogMessage)
	{
		mViewControlHandler = new Handler();

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
				// 환경설정 클래스에서 XecureCertShare 서버주소
				// - http://reaver.softforum.com:8087/xcs 로 설정.
				mXCSMobileDownload = new XCSMobileDownload(EnvironmentConfig.mCertShareAddr);
				mConnectServerResultFlag = 1;
			}
		});
		mConnectServerThread.start ();
	}
	
	private void moveBackScreenIntent(int pResultFlag)
	{
		Intent aIntent = null;
		
		if (mImportAuthenticationCodeThread != null && mImportAuthenticationCodeThread.isAlive ())
		{
			mImportAuthenticationCodeThread.interrupt ();
		}

		if (mImportCertificateThread != null && mImportCertificateThread.isAlive ())
		{
			mImportCertificateThread.interrupt ();
		}

		if (mConnectServerThread != null && mConnectServerThread.isAlive())
		{
			mConnectServerThread.interrupt();
		}

		if (mConnectServerResultFlag == 1)
		{
			mXCSMobileDownload.releaseObject();
		}

		mXCSMobileDownload = null;
		mXCSCertificate = null;
		mCertificateDER = null;
		mKeyDER = null;
		mKMCertificateDER = null;
		mKMKeyDER = null;

		// 결과값 리턴.
		//  - 인증서내 가져오기 취소 : 0. 
		//  - 인증번호 생성 실패 : mGetEnticateCodeFail.
		//  - 인증서 가져오기 실패 : mImportCertificateFail.
		//  - 인증서 가져오기 성공 : mImportCertificateSuccess.
		aIntent = new Intent();
		
		if (pResultFlag == 0)
		{
			setResult (Activity.RESULT_CANCELED);
		}
		else
		{
			if (pResultFlag == mGetEnticateCodeFail)
			{
				aIntent.putExtra (XecureSmartCertMgr.mOperationResultKey, XecureSmartCertMgr.mResultForImportCertByXCSCreateCNumFail);
			}

			else if (pResultFlag == mImportCertificateFail)
			{
				aIntent.putExtra (XecureSmartCertMgr.mOperationResultKey, XecureSmartCertMgr.mResultForImportCertByXCSFail);
			}

			else if (pResultFlag == mImportCertificateSuccess)
			{
				aIntent.putExtra (XecureSmartCertMgr.mOperationResultKey, XecureSmartCertMgr.mResultForImportCertByXCSOK);
			}
			setResult (Activity.RESULT_OK, aIntent);
		}
		
		finish ();
	}
	
	private void ShowAlertDialog (String pAlertMessage, final int pResultFlag)
	{
		if (mProgressDialog != null)
		{
			mProgressDialog.dismiss ();
		}
		mProgressDialog = null;

		mAlertMessage = pAlertMessage;

		// 성공이 아닌경우 에러코드를 셋팅하여 준다.
		if (pResultFlag != mImportCertificateSuccess)
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
					new AlertDialog.Builder (ImportCertWithCertShare.this)
					.setCancelable (false)
					.setMessage (mAlertMessage)
					.setPositiveButton ("확인", new DialogInterface.OnClickListener ()
					{
						public void onClick (DialogInterface dialog, int which)
						{
							// 사용된 객체의 메모리를 해제하여 주고 Result 셋팅한다.
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
					mAuthenticationcodeView = (EditText) findViewById (R.id.GetAuthenticationcode_Import1);
					mAuthenticationcodeView.setText (mAuthenticationcode1);

					mAuthenticationcodeView = (EditText) findViewById (R.id.GetAuthenticationcode_Import2);
					mAuthenticationcodeView.setText (mAuthenticationcode2);

					mAuthenticationcodeView = (EditText) findViewById (R.id.GetAuthenticationcode_Import3);
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
	
	private void getAuthenticationCode ()
	{
		// 인증번호 가져오기 쓰레드 시작.
		mProgressDialogMessage = "인증번호를 가져오는 중 입니다.";
		ShowProgressDialog (mProgressDialogMessage);

		mImportAuthenticationCodeThread = new Thread(new Runnable()
		{
			public void run ()
			{
				int mConnectServerCheckTimer = 0;

				try
				{
					// 서버와의 접속을 위해 쓰레드를 구동. (접속실패시 무한대기 방지)
					connectServerThread ();

					Thread.sleep (500);
					if (mConnectServerResultFlag == 1)
					{
						mAuthenticationcode = mXCSMobileDownload.getAuthenticateCode ();
					}
					// 서버와의 접속 타이머 동작. (3초 이상 접속못할시 실패로 간주한다.)
					else
					{
						// 타이머 동작.
						for (int i = 0; i <= 3; i++)
						{
							if (mConnectServerResultFlag == 1)
							{
								mConnectServerCheckTimer = 0;
								mAuthenticationcode = mXCSMobileDownload.getAuthenticateCode ();
								break;
							}

							if (mConnectServerCheckTimer <= 3)
							{
								Thread.sleep(1000);
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

				// 작업이 완료되면 프로그레스 바를 없애준다
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
		mImportAuthenticationCodeThread.start ();
	}
	
	private void importCertificate ()
	{
	 	// 서버로 부터 인증서 가져오기 쓰레드 시작.
		mProgressDialogMessage = "인증서를 가져오는 중입니다.";
		mProgressDialog = ProgressDialog.show (this, "Waiting", mProgressDialogMessage, true, true);

		mImportCertificateThread = new Thread(new Runnable()
		{
			public void run ()
			{
				try
				{
					int aResult = 0;
					
					File aTempCert = null;
					File aTempKey = null;
					File aTempKmCert = null;
					File aTempKmKey = null;

					FileOutputStream aCert = null;
					FileOutputStream aKey = null;
					FileOutputStream aKMCert = null;
					FileOutputStream aKMKey = null;
					
					mXCSCertificate = new XCSCertificate();
					mXCSCertificate = mXCSMobileDownload.getCertificate ();

					// 인증서 객체로 부터 인증서 파일을 추출하여 디코딩 후 저장.
					mCertificateDER = mXCSCertificate.getCertificateDER ();
					mKeyDER = mXCSCertificate.getKeyDER ();
					mKMCertificateDER = mXCSCertificate.getKMCertificateDER ();
					mKMKeyDER = mXCSCertificate.getKMKeyDER ();

					if ((mCertificateDER.length > 1 && mKeyDER.length > 1) || (mKMCertificateDER.length > 1 && mKMKeyDER.length > 1))
					{
						// 앱데이터의 캐쉬공간에 임시파일을 생성한다.
						aTempCert = File.createTempFile ("TempSignCert", ".der");
						aTempKey = File.createTempFile ("TempSignPri", ".key");
						aTempKmCert = File.createTempFile ("TempKmSignCert", ".der");
						aTempKmKey = File.createTempFile ("TempKmSignPri", ".key");

						try
						{
							if (!aTempCert.exists () || (!aTempKey.exists ()))
							{
								boolean aMkdirResult = false;
								
								//Log.d ("XecureSmart", "Make directory");
								//XSLog.d(this.getClass().getName() + "::" + "Make directory");
								
								aMkdirResult = aTempCert.mkdir ();
								aMkdirResult |= aTempKey.mkdir ();
								aMkdirResult |= aTempKmCert.mkdir ();
								aMkdirResult |= aTempKmKey.mkdir ();
								
								if(aMkdirResult != true)
								{
									//디렉토리 생성에 실패하였으므로 로그를 남기고 에러 리턴처리 한다. 
									printAlertMessage (mImportCertificateFail);
								}
							}

							if (mCertificateDER.length > 1 && mKeyDER.length > 1)
							{
								aCert = new FileOutputStream(aTempCert);
								aKey = new FileOutputStream(aTempKey);
								aCert.write (mCertificateDER);
								aKey.write( mKeyDER);


							}

							if (mKMCertificateDER.length > 1 && mKMKeyDER.length > 1)
							{
								aKMCert = new FileOutputStream(aTempKmCert);
								aKMKey = new FileOutputStream(aTempKmKey);
								aKMCert.write (mKMCertificateDER);
								aKMKey.write (mKMKeyDER);


							}

							// 기본적으로는 APPDATA에 인증서를 복사.
							// 미디어를 SDCARD만 사용으로 설정한 경우
							//  - SDCARD에 인증서를 저장.
							if (EnvironmentConfig.mSDCardOnlyUse == true)
							{
								mMediaID = XecureSmartCertMgr.mCERT_LOCATION_SDCARD;
							}
							else
							{
								mMediaID = XecureSmartCertMgr.mCERT_LOCATION_APPDATA;
							}

							if (mKMCertificateDER.length < 1 || mKMKeyDER.length < 1)
							{
								aResult = CertMgr.getInstance ().importCert (mMediaID, null, null, null,
										aTempCert.getPath (), aTempKey.getPath (), null, null);
							}
							else if (mCertificateDER.length > 1 && mKMCertificateDER.length > 1)
							{
								aResult = CertMgr.getInstance ().importCert (mMediaID, null, null, null,
										aTempCert.getPath (), aTempKey.getPath (), aTempKmCert.getPath (), aTempKmKey.getPath ());
							}

							// 생성되었던 임시파일을 삭제한다.
							aTempCert.delete ();
							aTempKey.delete ();
							aTempKmCert.delete ();
							aTempKmKey.delete ();

							if (aResult != 0)
							{
								printAlertMessage (mImportCertificateFail);
							}
							else
							{
								// PC로 부터 가져온 인증서의 주체정보를 alert 으로 표시.
								printAlertMessage (mImportCertificateSuccess);
							}
						}
						catch (FileNotFoundException e)
						{
							printAlertMessage (mImportCertificateFail);
						}
						finally
						{
							if (aCert != null)
							{
								aCert.close ();
							}
							if (aKey != null)
							{
								aKey.close ();
							}
							if (aKMCert != null)
							{
								aKMCert.close ();
							}
							if (aKMKey != null)
							{
								aKMKey.close ();
							}
						}
					}
					else
					{
						printAlertMessage (mImportCertificateFail);
					}
				}
				catch (Throwable e)
				{
					printAlertMessage (mImportCertificateFail);
				}
			}
		});
		mImportCertificateThread.start ();

		/*******************************************************************************/
	}
	
	private void printAlertMessage (int pResult)
	{
		String aErrorMessage;
		
		if (pResult == mImportCertificateFail)
		{
			aErrorMessage = "인증서 가져오기에 실패하였습니다.";
			ShowAlertDialog (aErrorMessage, pResult);
		}
		else 
		{
			aErrorMessage = "인증서 가져오기에 성공하였습니다.";
			ShowAlertDialog (aErrorMessage, pResult);		
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration config)
	{
		super.onConfigurationChanged(config);
	}
	
	@Override
	public void onBackPressed() 
	{
		mBlockerParam.setBlockerResult(123001);
		
		/*------------------------------------------------------------------------
		 * 뒤로 가기 하면 사용된 객체의 메모리를 해제하여 준다.
		 *------------------------------------------------------------------------*/
		moveBackScreenIntent(0);
	}
	
	@Override
	public void finish() {
		BlockerActivityUtil.finishBlockerActivity(mBlockerParam);
		
		super.finish();
	}
}
