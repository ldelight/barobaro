package com.softforum.xecure.ui.crypto;

public class XecureSmartCertMgr {

	//
	public static final String mOperationResultKey	= "xecure_smart_cert_mgr_result_key";
	
	//
	public static final int mCERT_TYPE_USER = 2;
	public static final int mCERT_TYPE_ALL = 3;
	
	public static final int mCERT_LOCATION_SDCARD = 101;
	public static final int mCERT_LOCATION_APPDATA = 1401;
	public static final int mCERT_LOCATION_PKCS11 = 401;
	
	public static final int mCERT_SEARCH_TYPE_ANY = 0;
	
	public static final int mCERT_SEARCH_TYPE_ISSUERRDN_CN = 20;
	public static final int mCERT_SEARCH_TYPE_ANY_EXCLUDE_EXPIRE = 25;
	
	public static final int mCERT_CONTENT_LEVEL_FULL = 0;
	public static final int mCERT_CONTENT_LEVEL_SIMPLE_LIST = 5;
	
	public static final int mResultForChangePassword = 1;
	
	/*
	 * XecureCertShare 사용하여 인증서 내보내기 관련 Result
	 */
	public static final int mResultForExportCertByXCSOK = 22;
	public static final int mResultForExportCertByXCSFail = 23;
	public static final int mResultForExportCertByXCSCancel = 24;
	public static final int mResultForExportCertByXCSCreateCNumFail = 25;
	
	/*
	 * XecureCertShare 사용하여 인증서 가져오기 관련 Result
	 */
	public static final int mResultForImportCertByXCSOK = 32;
	public static final int mResultForImportCertByXCSFail = 33;
	public static final int mResultForImportCertByXCSCreateCNumFail = 34;
}
