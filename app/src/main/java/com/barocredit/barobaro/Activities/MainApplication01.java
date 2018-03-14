package com.barocredit.barobaro.Activities;

import com.softforum.xecure.XApplication;


public class MainApplication01 extends XApplication {

	//
	private static XApplication mInstance;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		//
		mInstance = this;
	}
}
