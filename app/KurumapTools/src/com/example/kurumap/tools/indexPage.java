package com.example.kurumap.tools;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.text.util.Linkify;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class indexPage  extends Fragment{
	private String baseUrl = "http://192.168.1.111/";
	//SQLiteDatabase對象
	SQLiteDatabase db;
	//資料庫名
	public String db_name = "kurumapTools";
	//表名
	public String table_name = "userToken";
	public String token = null;	
	public String user = null;	
	
	DBOpenHelper helper;
	private ProgressDialog progressDialog;
	public FragmentActivity activity;
	public View view;
	protected static final int REFRESH_DATA = 0;
	
	public void onActivityCreated (Bundle savedInstanceState){
		
		
	}
	
	public View initIndex(View vv , FragmentActivity fa) {
		activity = fa;
		view = vv;
		
		helper = new DBOpenHelper(activity, db_name);
		
	
		getToken();
		
		if(token != null){
			getUserData();
			if(user != null){
				getMyComapnyByDB();
			}else{
				progressDialog = ProgressDialog.show(activity, "連線中", "請稍後..",true);
				getMyComapny();
			}
		}else{
			TextView tv = (TextView) view.findViewById(R.id.name);
			tv.setText("token is null");
		}
		
		return view;
	}	

	public void getMyComapnyByDB() { 
		//Toast.makeText(activity,"取得失敗" + token,Toast.LENGTH_SHORT).show();
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {				
	            try{      	
	                if(user == null){
	                	connectionFails.obtainMessage(REFRESH_DATA, user).sendToTarget();
	                }else{
	                	setData.obtainMessage(REFRESH_DATA, user).sendToTarget();
	                	Thread.sleep(2000);
	                	getMyComapny();
	                }
	            }catch (Exception e) {  
	                e.printStackTrace();  
	            }   
			} 
		});
		t.start();
	}	
	
	public void getMyComapny() { 
		//Toast.makeText(activity,"取得失敗" + token,Toast.LENGTH_SHORT).show();
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {				
	            try{      	
	            	Thread.sleep(1000);
	        		List<NameValuePair> params = new ArrayList<NameValuePair>();
	        		params.add(new BasicNameValuePair("token", token)); 
	                String result = httpRequest(params,"kurumap/companyApp/myCompany");
	                
	                if(user == null){
	                	progressDialog.dismiss();
	                }
	                
	                if(result == null){
	                	connectionFails.obtainMessage(REFRESH_DATA, result).sendToTarget();
	                }else{
	                	getUserData();
		                if(user!= null){
		                	delUserData();
		                }
	                	newUserData(result);
	                	setData.obtainMessage(REFRESH_DATA, result).sendToTarget();
	                }
	            }catch (Exception e) {  
	                e.printStackTrace();  
	            }   
			} 
		});
		t.start();
	}

	public void setData(String data)
	{			
		TextView name = (TextView) view.findViewById(R.id.name);
		TextView tel = (TextView) view.findViewById(R.id.tel);
		TextView fax = (TextView) view.findViewById(R.id.fax);
		TextView address = (TextView) view.findViewById(R.id.address);
		TextView oetime = (TextView) view.findViewById(R.id.oetime);
		TextView closeday = (TextView) view.findViewById(R.id.closeday);
		TextView web  = (TextView) view.findViewById(R.id.web);
		TextView fbpage  = (TextView) view.findViewById(R.id.fbpage);
		TextView profile  = (TextView) view.findViewById(R.id.profile);
		
		name.setText(getDataByKey(data, "name"));
		
		String te = getDataByKey(data, "tel");
		if((te != null && !te.equals(""))){
			LinearLayout telLayout = (LinearLayout) view.findViewById(R.id.telLayout);
			telLayout.setVisibility(View.VISIBLE);
			tel.setAutoLinkMask(Linkify.PHONE_NUMBERS);
			tel.setText(te);
		}	
		
		String fa = getDataByKey(data, "fax");
		if((fa != null && !fa.equals(""))){
			LinearLayout faxLayout = (LinearLayout) view.findViewById(R.id.faxLayout);
			faxLayout.setVisibility(View.VISIBLE);
			fax.setText(fa);
		}			

		String ad = getDataByKey(data, "full_address");
		if((ad != null && !ad.equals(""))){
			LinearLayout addressLayout = (LinearLayout) view.findViewById(R.id.addressLayout);
			addressLayout.setVisibility(View.VISIBLE);
			address.setAutoLinkMask(Linkify.MAP_ADDRESSES);
			address.setText(ad);
		}			
		
		String openTime = getDataByKey(data, "open_time");
		String endTime = getDataByKey(data, "close_time");
		String oe = null;
		
		
		if((openTime != null && !openTime.equals(""))){
			oe = openTime;
			if((endTime != null && !endTime.equals(""))){
				oe += " ~　" + endTime;
			}
			LinearLayout oetimeLayout = (LinearLayout) view.findViewById(R.id.oetimeLayout);
			oetimeLayout.setVisibility(View.VISIBLE);
			oetime.setText(oe);
		}
		
		
		String cd = getDataByKey(data, "close_day");
		if((cd != null && !cd.equals(""))){
			LinearLayout closedayLayout = (LinearLayout) view.findViewById(R.id.closedayLayout);
			closedayLayout.setVisibility(View.VISIBLE);
			closeday.setText(cd);
		}	
		
		String wb = getDataByKey(data, "web");
		if((wb != null && !wb.equals(""))){
			LinearLayout webLayout = (LinearLayout) view.findViewById(R.id.webLayout);
			webLayout.setVisibility(View.VISIBLE);
			web.setAutoLinkMask(Linkify.ALL);
			web.setText(wb);
		}	
		
		String fb = getDataByKey(data, "fans_page");
		if((fb != null && !fb.equals(""))){
			LinearLayout fbpageLayout = (LinearLayout) view.findViewById(R.id.fbpageLayout);
			fbpageLayout.setVisibility(View.VISIBLE);
			fbpage.setAutoLinkMask(Linkify.ALL);
			fbpage.setText(fb);
		}		

		String pf = getDataByKey(data, "profile");
		if((pf != null && !pf.equals(""))){
			LinearLayout profileLayout = (LinearLayout) view.findViewById(R.id.profileLayout);
			profileLayout.setVisibility(View.VISIBLE);
			profile.setText(pf);
		}			
		
	}	
	
	Handler setData = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{			
			
			String check = getDataByKey((String) msg.obj, "check");
			String company = getDataByKey((String) msg.obj, "company");
			
        	if(check.equals("ok")){
        		
				String category_id = getDataByKey((String) msg.obj, "company_category");	
				updateSystemParameter("category_id" , category_id);
				String company_category_parent_id = getDataByKey((String) msg.obj, "company_category_parent_id");	
				updateSystemParameter("category_id_parent_id" , company_category_parent_id);
				
        		setData(company);
        	}else{
        		Toast.makeText(activity,"取得失敗" + token,Toast.LENGTH_SHORT).show();
        	}
		}
	};
	
	Handler connectionFails = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
			dialog.setTitle("連線失敗 :-<"); //設定dialog 的title顯示內容
			dialog.setMessage((String) msg.obj);
			dialog.setCancelable(false); //關閉 Android 系統的主要功能鍵(menu,home等...)
			dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {  
			    public void onClick(DialogInterface dialog, int which) {  
			      // 按下"收到"以後要做的事情
			    }  
			});
			dialog.show();
		}
	};		
	
	
	private String httpRequest(List<NameValuePair> params, String url)
	{	
		
    	HttpPost httpRequest = new HttpPost(baseUrl + url);
    			
		try
		{
			/* 發出HTTP request */

			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			/* 取得HTTP response */
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
			HttpResponse httpResponse = new DefaultHttpClient(httpParams)
					.execute(httpRequest);
			
			/* 若狀態碼為200 ok */
			if (httpResponse.getStatusLine().getStatusCode() == 200)
			{
				/* 取出回應字串 */
				String strResult = EntityUtils.toString(httpResponse
						.getEntity());
				// 回傳回應字串
				return strResult;
			}
			
			return  null;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return  null;
	}	
	
	public void getToken() { 
		
		
		SQLiteDatabase db = helper.getReadableDatabase();
		 
        String[] columns = new String[] { "token"};
        Cursor cursor = db.query(table_name, columns, null, null, null, null, "1");

        //String token = null;
        while(cursor.moveToNext()){
       	 if(cursor.getString(0) == "null"){
       		 token = null;
       	 }else{
       		 token = cursor.getString(0);
       	 }
	         
        }
        cursor.close();
        db.close();
   }  	
	
	public void newUserData(String data) {  
		db = helper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("data", data);
		db.insert("userData", "", cv);
	}  
	
	public void delUserData() {
		SQLiteDatabase db = helper.getReadableDatabase();
		db.delete("userData", "1", null);
	}
	
	public void getUserData() {  
		SQLiteDatabase db = helper.getReadableDatabase();
		 
        String[] columns = new String[] { "data"};
        Cursor cursor = db.query("userData", columns, null, null, null, null, "1");

        //String token = null;
        while(cursor.moveToNext()){
       	 if(cursor.getString(0) == "null"){
       		user = null;
       	 }else{
       		user = cursor.getString(0);
       	 }
	         
        }
        cursor.close();
        db.close();
   } 

	private String getDataByKey(String data, String key)
	{	
		String returnData = null;
		try {
			
			returnData = new JSONObject((String) data).getString(key);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return returnData;
	}
	
	public void updateSystemParameter(String Key, String Value) {  
		SQLiteDatabase db = helper.getReadableDatabase();
		
		ContentValues values = new ContentValues();
		values.put("value", Value);//key為欄位名，value為值
		
        String selection = "parameterKey=?" ;
        String[] selectionArgs = new String[]{ Key };
        
        db.update("systemParameter", values, selection, selectionArgs);
        
        db.close();
   } 
	
}
