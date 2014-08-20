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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class preference  extends Fragment{
	private String baseUrl = "http://192.168.1.111/";
	//SQLiteDatabase對象
	SQLiteDatabase db;
	//資料庫名
	public String db_name = "kurumapTools";
	//表名
	public String table_name = "userToken";
	public String token = null;	
	public String user = null;	
	public String MyCategoryId = null;	
	public String MyCategoryParent = null;	
	public String MyCategoryOnSelect = null;	
	public int fristSetLock = 0;	
	public int indexOfMyCID = 0;
	
	DBOpenHelper helper;
	private ProgressDialog progressDialog;
	public FragmentActivity activity;
	public View view;
	protected static final int REFRESH_DATA = 0;
	public ArrayList<String> myList = new ArrayList<String>();
	private GridView gridView;

	private ArrayList<String> categoryLv1 = new ArrayList<String>();
	private ArrayList<String> categoryLv1Id = new ArrayList<String>();
	private ArrayList<String> categoryLv2 = new ArrayList<String>();
	private ArrayList<String> categoryLv2Id = new ArrayList<String>();
	
	public void onActivityCreated (Bundle savedInstanceState){
		
		
	}
	
	public View initIndex(View vv , FragmentActivity fa) {
		activity = fa;
		view = vv;
		
		helper = new DBOpenHelper(activity, db_name);
		
		getToken();
		getCategoryLv1();
		
		MyCategoryId = getSystemParameter("category_id");
		MyCategoryParent = getSystemParameter("category_id_parent_id");
		
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
		
		Button button = (Button) view.findViewById(R.id.update);
		button.setOnClickListener(new OnClickListener() {
		  @Override
		  public void onClick(View arg0) {
			progressDialog = ProgressDialog.show(activity, "更新中", "請稍後..",true);
		    updatePreference();
		  }
		});
		
		
		return view;
	}	

	public void updatePreference() { 
		//Toast.makeText(activity,"取得失敗" + token,Toast.LENGTH_SHORT).show();
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {				
	            try{      	
	            	Thread.sleep(1000);
	            	
	    			EditText abbreviationET = (EditText) view.findViewById(R.id.abbreviation);
	    			String abbreviation = abbreviationET.getText().toString();
	    			
	        		List<NameValuePair> params = new ArrayList<NameValuePair>();
	        		
	        		params.add(new BasicNameValuePair("categoryId", MyCategoryOnSelect)); 
	        		params.add(new BasicNameValuePair("abbreviation", abbreviation)); 
	        		params.add(new BasicNameValuePair("token", token)); 
	        		
	                String result = httpRequest(params,"kurumap/companyApp/updatePreference");
	                
	                progressDialog.dismiss();
	                
	                afterUpadte.obtainMessage(REFRESH_DATA, result).sendToTarget();
	            }catch (Exception e) {  
	                e.printStackTrace();  
	            }   
			} 
		});
		t.start();
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
	
	public void setData(String company,String information, int count)
	{			

	}	
	
	public void setCategoryLv2(String parent_id) {  
		SQLiteDatabase db = helper.getReadableDatabase();
		 
        String[] columns = new String[] { "name" , "id" };
        String selection = "parent_id=?" ;
        String[] selectionArgs = new String[]{ parent_id };
        
        Cursor cursor = db.query("category", columns, selection, selectionArgs, null, null, "sort ASC");

        while(cursor.moveToNext()){
        	categoryLv2.add(cursor.getString(0));
        	categoryLv2Id.add(cursor.getString(1)); 
        }
        cursor.close();
        db.close();
        
   }
	
	Handler setData = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{	
			String company = getDataByKey((String) msg.obj, "company");
			EditText abbreviationET = (EditText) view.findViewById(R.id.abbreviation);
			String abbreviation = getDataByKey(company, "abbreviation");
			abbreviationET.setText(abbreviation);
			
			Spinner spinner = (Spinner) view.findViewById(R.id.cat1);
			final Spinner spinner2 = (Spinner) view.findViewById(R.id.cat2);
			
			ArrayAdapter<String> setCategoryLv1 = new ArrayAdapter<String>(activity,android.R.layout.simple_spinner_item, categoryLv1);
			spinner.setAdapter(setCategoryLv1);		
			
			final int indexOfMyCID = categoryLv1Id.indexOf(MyCategoryParent);
			Log.d("indexOfMyCID",MyCategoryParent);
			Log.d("indexOfMyCID",Integer.toString(indexOfMyCID));
			spinner.setSelection(indexOfMyCID);
			
			setCategoryLv2(MyCategoryParent);
			ArrayAdapter<String> setCategoryLv2 = new ArrayAdapter<String>(activity,android.R.layout.simple_spinner_item, categoryLv2);
			spinner2.setAdapter(setCategoryLv2);
			int indexOfMyCID2 = categoryLv2Id.indexOf(MyCategoryId);
			Log.d("indexOfMyCID2",MyCategoryId);
			Log.d("indexOfMyCID2",Integer.toString(indexOfMyCID2));
			spinner2.setSelection(indexOfMyCID2);
			MyCategoryOnSelect = categoryLv2Id.get(indexOfMyCID2);
			
							
			 spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
		        @Override
		        public void onItemSelected(AdapterView<?> arg0, View arg1,int position, long arg3) {
		        	
		        	if(indexOfMyCID != position){
						categoryLv2.clear();
						categoryLv2Id.clear();
						
						categoryLv2.add("請選擇");
						categoryLv2Id.add("0");
											
						Log.d("position",Integer.toString(position));
						Log.d("get(position)",categoryLv1Id.get(position));
						setCategoryLv2(categoryLv1Id.get(position));
						spinner2.setSelection(0);
		        	}
		    	
		        }
			    @Override
			    public void onNothingSelected(AdapterView<?> arg0) {
			       // TODO Auto-generated method stub
			    }
			});
			 
			 spinner2.setOnItemSelectedListener(new OnItemSelectedListener(){
			        @Override
			        public void onItemSelected(AdapterView<?> arg0, View arg1,int position, long arg3) {
			        	MyCategoryOnSelect = categoryLv2Id.get(position);
			        }
				    @Override
				    public void onNothingSelected(AdapterView<?> arg0) {
				       // TODO Auto-generated method stub
				    }
				});
			 	 
		}
	};
	
	
	
	
	
	Handler afterUpadte = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			String check = getDataByKey((String) msg.obj, "check");
			String up_msg = null;
			if(check.equals("ok")){
				up_msg = "更新成功";
			}else{
				up_msg = getDataByKey((String) msg.obj, "msg");
			}
			
			String category_id = getDataByKey((String) msg.obj, "company_category");	
			updateSystemParameter("category_id" , category_id);
			String company_category_parent_id = getDataByKey((String) msg.obj, "company_category_parent_id");	
			updateSystemParameter("category_id_parent_id" , company_category_parent_id);

			delUserData();
        	newUserData(getDataByKey((String) msg.obj, "comapny"));
        	
			Toast.makeText(view.getContext(), up_msg, Toast.LENGTH_LONG).show();
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

	public void getCategoryLv1() {  
		SQLiteDatabase db = helper.getReadableDatabase();
		 
        String[] columns = new String[] { "name" , "id" };
        String selection = "level=?" ;
        String[] selectionArgs = new String[]{ "1" };
        
        Cursor cursor = db.query("category", columns, selection, selectionArgs, null, null, "sort ASC");

        //String token = null;
        while(cursor.moveToNext()){
        	categoryLv1.add(cursor.getString(0));
        	categoryLv1Id.add(cursor.getString(1));
        	//categoryLv1[] = cursor.getString(1);  
        }
        cursor.close();
        db.close();
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
	
	public String getSystemParameter(String Key) { 
		String value = null;
		SQLiteDatabase db = helper.getReadableDatabase();
		 
        String[] columns = new String[] { "parameterKey" , "value"};
        String selection = "parameterKey=?" ;
        String[] selectionArgs = new String[]{ Key };
        
        Cursor cursor = db.query("systemParameter", columns, selection, selectionArgs, null, null, "1");

        //String token = null;
        while(cursor.moveToNext()){
        	value = cursor.getString(1);  
        }
        cursor.close();
        db.close();
        
        return value;
   }  	
	
}
