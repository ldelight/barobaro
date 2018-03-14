package com.softforum.xecure.util;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.barocredit.barobaro.R;

public class XDetailDataRowAdapter extends ArrayAdapter<XDetailData> {
	Activity mContext;
	ArrayList<XDetailData> mXDetailDataRowArrayList;

	public XDetailDataRowAdapter(Activity context, ArrayList<XDetailData> items)
	{
		super(context, R.layout.xdetail_data_row, items);
		
		if( null != items ){
			mXDetailDataRowArrayList = items;
		}
		else
			mXDetailDataRowArrayList = new ArrayList<XDetailData>();
		
		mContext = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View aRowView = convertView;
		XDetailDataUIHolder aXDetailDataUIHolder = null;
		
		XDetailData aSelectedData= mXDetailDataRowArrayList.get( position );
		
		if( null == aRowView ) {
			LayoutInflater aInflater = mContext.getLayoutInflater();
			aRowView = aInflater.inflate(R.layout.xdetail_data_row , null);
			
			aXDetailDataUIHolder = new XDetailDataUIHolder();
			aXDetailDataUIHolder.icon = (ImageView) aRowView.findViewById(R.id.icon);
			aXDetailDataUIHolder.mainMsg = (TextView) aRowView.findViewById(R.id.main_msg);
			aXDetailDataUIHolder.subMsg1 = (TextView) aRowView.findViewById(R.id.sub_msg1);
			aXDetailDataUIHolder.subMsg2 = (TextView) aRowView.findViewById(R.id.sub_msg2);
			
			aRowView.setTag( aXDetailDataUIHolder );
			
		}
		else {
			aXDetailDataUIHolder = (XDetailDataUIHolder) aRowView.getTag();
		}
		
		if( "0".equals( aSelectedData.getValue( XDetailData.CERT_KEY_SIMPLE.STATE ) ) ){
			aXDetailDataUIHolder.icon.setImageResource( R.drawable.cert_state_normal );
			aXDetailDataUIHolder.icon.setContentDescription("인증서가 정상입니다.");
		} else if ( "1".equals( aSelectedData.getValue(XDetailData.CERT_KEY_SIMPLE.STATE ) ) ) {
			aXDetailDataUIHolder.icon.setImageResource( R.drawable.cert_state_update );
			aXDetailDataUIHolder.icon.setContentDescription("인증서가 30일 이내에 만료됩니다.");
		} else if ( "2".equals( aSelectedData.getValue(XDetailData.CERT_KEY_SIMPLE.STATE ) ) ) {
			aXDetailDataUIHolder.icon.setImageResource( R.drawable.cert_state_revoke );
			aXDetailDataUIHolder.icon.setContentDescription("인증서가 만료되었습니다.");
		}
		
		aXDetailDataUIHolder.mainMsg.setText( XUtil.getCNFromRDN(aSelectedData.getValue(XDetailData.CERT_KEY_SIMPLE.SUBJECT_RDN) ) );
		aXDetailDataUIHolder.subMsg1.setText( aSelectedData.getKeyText( XDetailData.CERT_KEY_SIMPLE.ISSUER_RDN )
											  +" : "
											  + aSelectedData.getValue(XDetailData.CERT_KEY_SIMPLE.ISSUER_RDN ) );
		aXDetailDataUIHolder.subMsg2.setText( aSelectedData.getKeyText( XDetailData.CERT_KEY_SIMPLE.TO )
				                              +" : "
				                              +aSelectedData.getValue( XDetailData.CERT_KEY_SIMPLE.TO ) );
		
		String tmp = aXDetailDataUIHolder.mainMsg.getText() + ". " +
				aXDetailDataUIHolder.icon.getContentDescription().toString() + ". " +
				aXDetailDataUIHolder.subMsg1.getText() + ". " +
				aXDetailDataUIHolder.subMsg2.getText();
		
		aRowView.setContentDescription(tmp);
		
		return aRowView;
	}
	
	private class XDetailDataUIHolder {
		ImageView icon;
		TextView mainMsg;
		TextView subMsg1;
		TextView subMsg2;
	}
}
