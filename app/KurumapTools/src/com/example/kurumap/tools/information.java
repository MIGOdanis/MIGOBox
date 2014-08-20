package com.example.kurumap.tools;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class information  extends Fragment{
	private String baseUrl = "http://192.168.1.111/";
	//SQLiteDatabase對象
	SQLiteDatabase db;
	//資料庫名
	public String db_name = "kurumapTools";
	//表名
	public String table_name = "userToken";
	public String token = null;	
	DBOpenHelper helper;
	private ProgressDialog progressDialog;
	public FragmentActivity activity;
	public View view;
	protected static final int REFRESH_DATA = 0;
	public ArrayList<String> myList = new ArrayList<String>();
	private GridView gridView;
	
	public void onActivityCreated (Bundle savedInstanceState){
		
		
	}
	
	public View initIndex(View vv , FragmentActivity fa) {
		activity = fa;
		view = vv;
		
		helper = new DBOpenHelper(activity, db_name);
		
	
		getToken();
		
		if(token != null){
			progressDialog = ProgressDialog.show(activity, "連線中", "請稍後..",true);
			getMyComapny();
		}else{
			TextView tv = (TextView) view.findViewById(R.id.name);
			tv.setText("token is null");
		}
		
		Button button = (Button) view.findViewById(R.id.newInfor);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				activity.setContentView(R.layout.activity_main);
				Intent intent = new Intent();
				intent.setClass(activity, NewInformation.class);
				activity.startActivity(intent); 
				activity.finish(); 
			}
		});
		
		return view;
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
	                String result = httpRequest(params,"kurumap/companyApp/myInformation");
	                if(result == null){
	                	connectionFails.obtainMessage(REFRESH_DATA, result).sendToTarget();
	                }else{
	                	setData.obtainMessage(REFRESH_DATA, result).sendToTarget();
	                }
	            }catch (Exception e) {  
	                e.printStackTrace();  
	            }   
			} 
		});
		t.start();
	
	}

	
	public String getDate(long time)
	{			
		long lt = time * (long) 1000;
	    Date date = new Date(lt); // *1000 is to convert seconds to milliseconds
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // the format of your date
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
	
	
	
	public void setData(String company,String information, int count)
	{			
		
		List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();
		for (int i = 0; i < count; i++) {
		     String start_time = getDate(Long.parseLong(getDataByIndex(information, "start_time", i)));
		     String end_time = getDate(Long.parseLong(getDataByIndex(information, "end_time", i)));
		     
		     Map<String, Object> item = new HashMap<String, Object>();
		     item.put("company", getDataByKey(company, "name"));
		     item.put("setime", "由 "+start_time+" 至 "+end_time);
		     item.put("content", getDataByIndex(information, "content", i));
		     String type = getDataByIndex(information, "type", i);
		     String top = getDataByIndex(information, "top", i);
		     int typeicon;
		     
		     
		     if(type.equals("2")){
		    	 typeicon = R.drawable.iipc;
		     }else{
		    	 typeicon = R.drawable.iic;
		     }
		     
		     item.put("icon", typeicon);
		     items.add(item);
		}
		SimpleAdapter adapter = new SimpleAdapter(activity, 
			    items, R.layout.information_item, new String[]{"company", "setime", "content", "icon"},
			    new int[]{R.id.company, R.id.setime, R.id.content, R.id.icon});
		
		gridView = (GridView) view.findViewById(R.id.main_page_gridview);
		gridView.setAdapter(adapter);	
	}	
	
	Handler setData = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{			
			progressDialog.dismiss();
			String check = getDataByKey((String) msg.obj, "check");
			String company = getDataByKey((String) msg.obj, "company");
			String information = getDataByKey((String) msg.obj, "information");
			Integer count = Integer.parseInt(getDataByKey((String) msg.obj, "informationCount"));
			
			//Toast.makeText(activity,count,Toast.LENGTH_SHORT).show();
			
        	if(check.equals("ok")){
        		if(count > 0){
        			setData(company,information,count);
        		}
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
			progressDialog.dismiss();
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
	
	private String getDataByIndex(String data, String key, int index)
	{	
		String returnData = null;
		try {
			
			returnData = new JSONArray(data).getJSONObject(index).getString(key);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return returnData;
	}		

	
}
