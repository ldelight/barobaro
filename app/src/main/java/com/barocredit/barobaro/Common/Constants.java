package com.barocredit.barobaro.Common;

/**
 * Created by ctest on 2018-02-22.
 */

public class Constants {
    public static final String INITURL = "http://app.barocredit.iwi.co.kr";
    public static final String PERMISSIONURL = "http://app.barocredit.iwi.co.kr/contents/contiguity";
    public static final String MAIN_URL = "main.do";
    public static final String LOGIN_URL = "http://app.barocredit.iwi.co.kr";
    public static final String JOIN_URL = "join.do";
    public static boolean isAutoLoginYn = true;
    public static String gcmRegId = null;

    public static final String TYPE_IMAGE = "image/*";
    public static final int INPUT_FILE_REQUEST_CODE = 1;

//    public static final String AUTH_HEADER_KEY = "AuthorizationKey";
//    public static final String AUTH_HEADER_VAL = "AuthorizationVal";
    public static final String USER_AGENT_STRING = "BaroCreditApp";

    //XecureAppShield 관련 변수
    public static String XAS_BASE_DOMAIN = "appwas.barocredit.net";
    public static String XAS_DOMAIN = "http://" + XAS_BASE_DOMAIN + "/xasService/";
    public static String XAS_APPID = "barocredit";
    public static String XAS_APPVER = "1";
    public static boolean XAS_LIVE_UPDATE = true;

    //
    public static String NPROTECT_LICENSE_KEY = "F1AD22E711F5";
    public static String NPROTECT_USER_ID = "BARO_01";
}
