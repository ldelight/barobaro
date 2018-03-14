package com.softforum.xecure.util;

public class EnvironmentConfig {

	/**
	 * WebViewActivity - Home Url
	 */
	public static final String mHomeUrl = "http://1.237.174.154:8888/xsm/emkim/webview_index.jsp";
	
	/**
	 * 인증서 인증서 암호 재시도 가능 횟수 기본값.
	 */
	public static final int m_P_assW_ordTryLimit = 3;
	
	/**
	 * XecureCertShare 서버 주소
	 */
	public static final String mCertShareAddr = "http://1.237.174.154:8888/xcs/"; //"http://reaver.softforum.com:8087/xcs";
	
	/**
	 *  XecureKeypad Full View 사용
	 */
	//public static boolean mXecureKeypadFullViewUsage = false;
	public static boolean mXecureKeypadFullViewUsage = true;
	
	/**
	 *  XecureKeypad Normal View 사용
	 */
	public static boolean mXecureKeypadNormalViewUsage = false;
	
	/**
	 *  XecureKeypad 암호화 모드 
	 */
	//public static boolean mXecureKeypadEncryptionUsage = false;
	public static boolean mXecureKeypadEncryptionUsage = true;
	
	
	/* Library Load 관련. 반드시 Preload시에만 변경 */

	/**
	 * 단말기에 Preload 되는 경우 활성화.
	 */
	public static boolean mPreloaded = false;
	
	/**
	 * 로드할 XWClientSM 라이브러리 파명.<br>
	 * 
	 * 2010.1.11. Preload 어플과의 충돌 문제로 절대 경로/파일명 기준으로 수정 됨. 
	 */
	public static String mXWClientLibFilePath = "libXWClientSM_jni.so";
	
	/**
	 * 로드할 XecureCrypto 라이브러리명.<br>
	 * 
	 * CMVP 적용하기 위해 추가된 라이브러리.
	 */
	public static String mXecureCryptoLibFilePath = "libXecureCrypto.so";
	
	/**
	 * 로드할 인증서 가져오기 라이브러리명.<br>
	 * null인 경우 로드하지 않는다.<br>
	 * 
	 * 2010.1.11. Preload 어플과의 충돌 문제로 절대 경로/파일명 기준으로 수정 됨. 
	 */
	//public static String mMysignLibFilePath = "libKeySharp_Android_Core.so";
	public static String mMysignLibFilePath = "";
	
	/**
	 * 인증서 암호 설정 규칙 강화 시행에 따른 인증서 암호 유효성 검증 사용 
	 */
	public static final boolean mUseNewPasswordValidCheck = true;

	/**
	 * 기존 SDCARD 저장공간만 사용
	 */
	//public static boolean mSDCardOnlyUse = false;
	public static boolean mSDCardOnlyUse = true;

	/**
	 * 유효기간 만료된 인증서를 인증서 리스트에서 제외
	 */
	public static boolean mExcludeExpiredCert = false;
	
	/**
	 * 로그레벨 설정
	 */
	public static final int NONE = 0;
	public static final int INFO = 1;
	public static final int ERROR = 2;
	public static final int DEBUG = 3;
	//
	public static int mLogLevel = DEBUG;
	
	
}
