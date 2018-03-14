package com.softforum.xecure;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.softforum.xecure.certshare.ImportCertWithCertShare;
import com.softforum.xecure.certshare.SelectExportCertListWithCertShare;
import com.softforum.xecure.crypto.BlockMgr;
import com.softforum.xecure.crypto.CMPMgr;
import com.softforum.xecure.crypto.CertMgr;
import com.softforum.xecure.crypto.SessionMgr;
import com.softforum.xecure.crypto.SignEnvelopMgr;
import com.softforum.xecure.crypto.XSFileEnvelope;
import com.softforum.xecure.keypad.SignCertPasswordWindowWithXK;
import com.softforum.xecure.ui.crypto.SignCertSelectWindow;
import com.softforum.xecure.ui.crypto.XecureSmartCertMgr;
import com.softforum.xecure.ui.webcall.XecureSmartChangePasswordCertList;
import com.softforum.xecure.ui.webcall.XecureSmartDeleteCertList;
import com.softforum.xecure.util.BlockerActivityResult;
import com.softforum.xecure.util.BlockerActivityUtil;
import com.softforum.xecure.util.EnvironmentConfig;
import com.softforum.xecure.util.XCoreUtil;
import com.softforum.xecure.util.XErrorCode;
import com.softforum.xecure.util.XSLog;
import com.softforum.xecure.util.XUtil;

public class XecureSmart {

	//
	//public static int mDefaultMediaID = XecureSmartCertMgr.mCERT_LOCATION_APPDATA;
	public static int mDefaultMediaID = XecureSmartCertMgr.mCERT_LOCATION_SDCARD;
	
	//
	private volatile static XecureSmart mUniqueXecureSmartInstance;
	
	//
	private BlockMgr mBlockMgr;
	private CMPMgr mCMPMgr;
	private CertMgr mCertMgr;
	private XSFileEnvelope mFileEnvelope;
	private SignEnvelopMgr mSignEnvelopMgr;
	private SessionMgr mSessionMgr;
	private XCoreUtil mCoreUtil;
	
	//
	private long mSessionID;
	private Context mGlobalContext = XApplication.getContext();
	
	private BlockerActivityResult mBlockerActivityResult;
	
	// 서명관련 인증서 선택창 옵션
	private static final int XW_FVIEW_CERT_LOGIN = 0x00000002;
	private static final int XW_FVIEW_CREATE_VID_FROM_IDN = 0x00000004;
	private static final int XW_FVIEW_CREATE_VID_FROM_WEB = 0x00000008;
	private static final int XW_FVIEW_CREATE_VID_NO_IDN = 0x00000010;
	
	//
	private XecureSmart() {
		mSessionID = this.hashCode();
		mBlockMgr = BlockMgr.getInstance();
		mCMPMgr = new CMPMgr();
		mCertMgr = CertMgr.getInstance();
		mFileEnvelope = new XSFileEnvelope(mSessionID);
		mSignEnvelopMgr = new SignEnvelopMgr();
		mSessionMgr = SessionMgr.getInstance();
		mCoreUtil = new XCoreUtil();
		
		// XecureKeypad Encryption 설정
		SetAttribute("securekeypad_vendor", "xkeypad");
		
		/*
		 * DefaultMediaID 설정 (환경설정 클래스에서 저장소를 SDCARD 로 선택했을땐 101, APPDATA 로
		 * 선택했을땐 1401)
		 */
		//
		if (EnvironmentConfig.mSDCardOnlyUse == true)
		{
			mDefaultMediaID = XecureSmartCertMgr.mCERT_LOCATION_SDCARD;
		}
		else
		{
			mDefaultMediaID = XecureSmartCertMgr.mCERT_LOCATION_APPDATA;
		}
	}
	
	/*
	 * Method for single instance
	 */

	public synchronized static XecureSmart getInstance()
	{
		if (null == mUniqueXecureSmartInstance)
		{
			mUniqueXecureSmartInstance = new XecureSmart();
		}

		return mUniqueXecureSmartInstance;
	}
	
	/*
	 * BlockMgr related function
	 */
	public void setBlockMgrCallersContext(Context context)
	{
	}
	
	@JavascriptInterface
	public String BlockEnc(String xaddr, String path, String plain, String method)
	{
		mCoreUtil.resetError();
		return BlockEncEx(xaddr, path, plain, method, "");
	}

	@JavascriptInterface
	public String BlockEncEx(String xaddr, String path, String plain, String method, String ca_name)
	{
		//Log.d("XecureSmart", "session id: " + mSessionID);
		mCoreUtil.resetError();

		String result = mBlockMgr.blockEncEx(mSessionID, xaddr, path, plain, method, ca_name);

		return result;
	}
	
	@JavascriptInterface
	public String BlockDec(String xaddr, String cipher_msg)
	{
		mCoreUtil.resetError();
		String aResult = mBlockMgr.blockDecEx(xaddr, cipher_msg, ""); // FIXME:  "euc-kr"
		return aResult;
	}

	@JavascriptInterface
	public String BlockDecEx(String aXgateAddress, String aCipherText, String aCharacterSet)
	{
		String aResult = null;
		
		mCoreUtil.resetError();

		aResult = mBlockMgr.blockDecEx(aXgateAddress, aCipherText, aCharacterSet);
		
		return aResult;
	}
	
	private boolean isShowSelectCertWindow(long aSessionID, String aXgateAddr)
	{
		boolean aResult = true;

		String aSecOptionStr = mCoreUtil.getAttribute(mSessionID, "sec_option");
		XSLog.d(this.getClass().getName() + "::" + "SecOption" + aSecOptionStr);
		
		if (null == aSecOptionStr || "".equals(aSecOptionStr))
			return aResult;
		
		String aSecOptionCode = aSecOptionStr.substring(0, aSecOptionStr.indexOf(':'));

		try
		{
			if (aSecOptionCode.length() != 0 && (Integer.valueOf(aSecOptionCode) & 0x00000200) != 0)
			{ // SECOPT_SIGN_CACHE_CERT
				XSLog.d(this.getClass().getName() + "::" + "SecOption" + aSecOptionStr);
				if (mCertMgr.hasCachedData(mSessionID, aXgateAddr) == 1)
				{
					XSLog.d(this.getClass().getName() + "::" + "SecOption" + aSecOptionStr);
					aResult = false;
				}
			}
		}
		catch (NumberFormatException e)
		{
			return true;
		}

		return aResult;
	}
	
	private void showCertSelectWindow(String aCallMode, long aSessionID, String aXaddr, int aMediaID, int aCertType, int aMediaType, String aSearchValue, String aCertSerial, int aPasswdTryLimit, String aData, int aOption)
	{
		Intent aIntent = null;
		aIntent = new Intent(mGlobalContext, SignCertSelectWindow.class);
		
		aIntent.putExtra(SignCertSelectWindow.mPluginSessionIDKey, aSessionID);
		aIntent.putExtra(SignCertSelectWindow.mXaddrKey, aXaddr);
		aIntent.putExtra(SignCertSelectWindow.mMediaIDKey, aMediaID);
		aIntent.putExtra(SignCertSelectWindow.mCertTypeKey, aCertType);
		aIntent.putExtra(SignCertSelectWindow.mMediaTypeKey, aMediaType);
		aIntent.putExtra(SignCertSelectWindow.mSearchValueKey, aSearchValue);
		aIntent.putExtra(SignCertSelectWindow.mCertSerialKey, aCertSerial);
		aIntent.putExtra(SignCertSelectWindow.mCallModeKey, aCallMode);
		aIntent.putExtra(SignCertSelectWindow.mPainTextKey, aData);
		aIntent.putExtra(SignCertSelectWindow.mSignOption, aOption);
		
		mBlockerActivityResult = BlockerActivityUtil.startBlockerActivity(mGlobalContext, aIntent);
	}
	
	@JavascriptInterface
	public String SignDataCMS(String aXaddr, String aCaName, String aData, int aOption, String aDesc, int aPasswdTryLimit)
	{
		return this.SignDataCMSWithSerial(aXaddr, aCaName, "", 1, aData, aOption, aDesc, aPasswdTryLimit);
	}
	
	@SuppressLint("NewApi")
	@JavascriptInterface
	public String SignDataCMSWithSerial(String aXaddr, String aCaName, String aCertSerial, int aCertLocation, String aData, int aOption, String aDesc, int aPasswdTryLimit)
	{
		int aMediaID = mDefaultMediaID;
		int aMediaType = 20;
		String aSearchValue = aCaName;
		int aSearchCondition = 14;
		byte[] aPassword = null;
		byte[] aRandomValue = null;
		String aEncryptedData = null;
		String aSubjectDN = "";
		String aResult = null;
		
		mCoreUtil.resetError();
		
		if (EnvironmentConfig.mSDCardOnlyUse == true)
		{
			aMediaID = XecureSmartCertMgr.mCERT_LOCATION_SDCARD;
		}
		else
		{
			aMediaID = XecureSmartCertMgr.mCERT_LOCATION_APPDATA;
		}
		
		if ((aOption & XW_FVIEW_CERT_LOGIN) != 0)
		{ // sign with the log-on certificate only
			aMediaID = mSessionMgr.getSessionClientMedia(aXaddr);
			aMediaType = 14;
			aSearchValue = mSessionMgr.getSessionClientRDN(aXaddr);
		}
		
		if (isShowSelectCertWindow(mSessionID, aXaddr))
		{
			showCertSelectWindow(SignCertSelectWindow.mCallModeSign, mSessionID, aXaddr, aMediaID, 2, aMediaType, aSearchValue, aCertSerial, aPasswdTryLimit, aData, aOption);
			
			if (mBlockerActivityResult.getResultCode() == Activity.RESULT_CANCELED)
			{ /* cancel cert select */
				mCoreUtil.setError(XErrorCode.XW_ERROR_PLUGINS_SIGN_CANCEL);
				return "";
			}
			else if (mBlockerActivityResult.getResultCode() == SignCertPasswordWindowWithXK.RESULT_PASSWD_FAIL)
			{
				mCoreUtil.setError(XErrorCode.XW_ERROR_PLUGINS_CERT_PWD_FAIL);
				return "";
			}
			
			aMediaID = mBlockerActivityResult.getData().getIntExtra(XApplication.mMediaIDKey, 1);
			aSubjectDN = mBlockerActivityResult.getData().getStringExtra(XApplication.mSubjectRDNKey);
			
			//
			aRandomValue = mBlockerActivityResult.getData().getByteArrayExtra(XApplication.mRandomValueKey);
			aEncryptedData = mBlockerActivityResult.getData().getStringExtra(XApplication.mE_ncryptedDataKey);
		}
		
		//
		aOption = aOption + 512;
		
		//
		aResult = mSignEnvelopMgr.signDataCMS(mSessionID, aXaddr, aMediaID, aSubjectDN, aRandomValue, aEncryptedData, aData, aOption);
		
		XUtil.resetByteArray(aPassword, aRandomValue);
		
		return aResult;
	}
	
	@JavascriptInterface
	public String GetVidInfo()
	{

		mCoreUtil.resetError();
		return mSignEnvelopMgr.getVidInfo();
	}
	
	@JavascriptInterface
	public String SignDataWithVID(String aXaddr, String aAcceptCert, String aData, String aCert, int aOption, String aDesc, int aPasswdTryLimit)
	{
		int aMediaID = mDefaultMediaID;
		int aMediaType = 20;
		String aSearchValue = aAcceptCert;
		int aSearchCondition = 14;
		
		byte[] aRandomValue = null;
		String aEncryptedData = null;
		String aSubjectDN = "";
		
		int aResult = 0;
		String aStrResult = "";
		
		mCoreUtil.resetError();

		if (EnvironmentConfig.mSDCardOnlyUse == true)
		{
			aMediaID = XecureSmartCertMgr.mCERT_LOCATION_SDCARD;
		}
		else
		{
			aMediaID = XecureSmartCertMgr.mCERT_LOCATION_APPDATA;
		}
		
		if ((aOption & XW_FVIEW_CERT_LOGIN) != 0)
		{ // sign with the log-on certificate only
			aMediaID = mSessionMgr.getSessionClientMedia(aXaddr);
			aMediaType = 14;
			aSearchValue = mSessionMgr.getSessionClientRDN(aXaddr);
		}
		
		if (isShowSelectCertWindow(mSessionID, aXaddr)) 
		{
			showCertSelectWindow(SignCertSelectWindow.mCallModeSign, mSessionID, aXaddr, aMediaID, 2, aMediaType, aSearchValue, "", aPasswdTryLimit, aData, aOption);
			
			if (mBlockerActivityResult.getResultCode() == Activity.RESULT_CANCELED)
			{ /* cancel cert select */
				mCoreUtil.setError(XErrorCode.XW_ERROR_PLUGINS_SIGN_CANCEL);
				return "";
			}
			else if (mBlockerActivityResult.getResultCode() == SignCertPasswordWindowWithXK.RESULT_PASSWD_FAIL)
			{
				mCoreUtil.setError(XErrorCode.XW_ERROR_PLUGINS_CERT_PWD_FAIL);
				return "";
			}
			
			aMediaID = mBlockerActivityResult.getData().getIntExtra(XApplication.mMediaIDKey, 1);
			aSubjectDN = mBlockerActivityResult.getData().getStringExtra(XApplication.mSubjectRDNKey);
			
			//
			aRandomValue = mBlockerActivityResult.getData().getByteArrayExtra(XApplication.mRandomValueKey);
			aEncryptedData = mBlockerActivityResult.getData().getStringExtra(XApplication.mE_ncryptedDataKey);
		}
		
		if ((aOption & XW_FVIEW_CREATE_VID_FROM_WEB) != 0 || (aOption & XW_FVIEW_CREATE_VID_NO_IDN) != 0)
		{
			if ((aOption & XW_FVIEW_CREATE_VID_NO_IDN) != 0)
			{
				aResult = mSignEnvelopMgr.setIdNum("");
				if (aResult != 0)
				{
					return "";
				}
			}
		}
		else
		{
			Intent aIntent = null;
			
			// To Do...
		}
		
		//
		aStrResult = mSignEnvelopMgr.signDataCMS(mSessionID, aXaddr, aMediaID, aSubjectDN, aRandomValue, aEncryptedData, aData, aOption);
		
		if ((aOption & XW_FVIEW_CREATE_VID_FROM_IDN) != 0)
		{
			//
			aResult = mSignEnvelopMgr.envelopIdNum(mSessionID, aXaddr, aMediaID, aSubjectDN, aRandomValue, aEncryptedData, aCert);
		}
	
		return aStrResult;
	}

	/*
	 * Core config & util
	 */
	@JavascriptInterface
	public int LastErrCode()
	{
		return mCoreUtil.lastErrCode();
	}

	@JavascriptInterface
	public String LastErrMsg()
	{
		return mCoreUtil.lastErrMsg();
	}

	@JavascriptInterface
	public int EndSession(String aXaddr)
	{
		return mCoreUtil.endSession(aXaddr);
	}

	@JavascriptInterface
	public String GetAttribute(String aName)
	{
		return mCoreUtil.getAttribute(mSessionID, aName);
	}

	@JavascriptInterface
	public void SetAttribute(String aName, String aValue)
	{
		Log.d("DEBUG","mSessionID IS :"+ ( mSessionID ) );
		Log.d("DEBUG","mSessionID IS :"+  aName  );
		Log.d("DEBUG","mSessionID IS :"+  aValue );
		Log.d("DEBUG","mSessionID IS :"+  (mCoreUtil == null) );
		mCoreUtil.setAttribute(mSessionID, aName, aValue);
	}
	
	
	//////////////////////////////////////////////////////////////////////////
	// Custom Func Define
	//////////////////////////////////////////////////////////////////////////
	
	// 인증서 비밀번호 변경
	@JavascriptInterface
	public int ChangePasswordCertificate() {
		
		int aResult = -1;
		
		Intent aIntent = null;
		aIntent = new Intent( mGlobalContext, XecureSmartChangePasswordCertList.class);
		
		mBlockerActivityResult = BlockerActivityUtil.startBlockerActivity(mGlobalContext, aIntent);
		
		int resultCode = mBlockerActivityResult.getResultCode();
		
		// (성공) 
		if(resultCode == 0) {
			aResult = 0;
		} else if (resultCode == 123003) {  
			aResult = 123003;
		} else {
			aResult = -1;
		}
		
		return aResult;
	}
	

	// 인증서 삭제
	@JavascriptInterface
	public int DeleteCertificate() {
		
		int aResult = -1;
		
		Intent aIntent = null;
		aIntent = new Intent( mGlobalContext, XecureSmartDeleteCertList.class);
		
		mBlockerActivityResult = BlockerActivityUtil.startBlockerActivity(mGlobalContext, aIntent);
		
		int resultCode = mBlockerActivityResult.getResultCode();
		
		// (성공) 
		if(resultCode == 0) {
			aResult = 0;
		} else if (resultCode == 123004) {  
			aResult = 123004;
		} else {
			aResult = -1;
		}
		
		return aResult;
	}

	// 인증서 가져오기 (PC → Mobile)
	@JavascriptInterface
	public int ImportCertWithXCS() {
		
		int aResult = -1;
		
		Intent aIntent = null;
		
		aIntent = new Intent( mGlobalContext, ImportCertWithCertShare.class);
		
		mBlockerActivityResult = BlockerActivityUtil.startBlockerActivity(mGlobalContext, aIntent);
		
		int resultCode = mBlockerActivityResult.getResultCode();
		
		// (성공) 
		if(resultCode == 0) {
			aResult = 0;
		} else if (resultCode == 123001) {  
			aResult = 123001;
		} else {
			aResult = -1;
		}
		
		return aResult;
	}

	// 인증서 내보내기 (Mobile → PC)
	@JavascriptInterface
	public int ExportCertWithXCS() {
		
		int aResult = -1;
		
		Intent aIntent = null;
		aIntent = new Intent( mGlobalContext, SelectExportCertListWithCertShare.class);
		
		mBlockerActivityResult = BlockerActivityUtil.startBlockerActivity(mGlobalContext, aIntent);
		
		int resultCode = mBlockerActivityResult.getResultCode();
		
		// (성공) 
		if(resultCode == 0) {
			aResult = 0;
		} else if (resultCode == 123002) {  
			aResult = 123002;
		} else {
			aResult = -1;
		}
		
		return aResult;
	}
}
