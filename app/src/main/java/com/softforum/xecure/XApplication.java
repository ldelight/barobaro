package com.softforum.xecure;

import android.content.Context;

public class XApplication extends android.app.Application {
	//public static final Class<?> mMainActivityClass = EnvironmentConfig.mMainActivityClass;
	
	private static XApplication mInstance;
	
	@Override
	public void onCreate() {
		super.onCreate();

		mInstance = this;
	}

	public static Context getContext() {
		return mInstance;
	}
	
	public final static String mMediaIDKey = "media_id_key";
	public final static String mSubjectRDNKey = "subject_rdn_key";
	public final static String m_P_assW_ordKey = "password_key";
	
	public final static String mRandomValueKey = "random_value_key";
	public final static String mE_ncryptedDataKey = "e_ncrypted_data_key";
}
