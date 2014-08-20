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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.animation.AnimatorSet.Builder;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class LoginActivity extends ActionBarActivity {
	
	private String baseUrl = "http://192.168.1.111/";
	private String username = null;
	private String passwd = null;
	//SQLiteDatabase對象
	SQLiteDatabase db;
	//資料庫名
	public String db_name = "kurumapTools";
	//表名
	public String token = null;	
	public String categoryVersion = null;	
	public String newCategoryVersion = null;
	
	
	
	DBOpenHelper helper = new DBOpenHelper(LoginActivity.this, db_name);
	
	protected static final int REFRESH_DATA = 0;
	
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		getToken();
		getCategoryVersion();
		
		if(token != null){
			progressDialog = ProgressDialog.show(LoginActivity.this, "連線中", "請稍後..",true);
			checkToken();
		}	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void nextActivity()
	{
		SQLiteDatabase db = helper.getReadableDatabase();
		db.delete("userData", "1", null);
		
		setContentView(R.layout.activity_main);
		Intent intent = new Intent();
		intent.setClass(LoginActivity.this, MainActivity.class);
		startActivity(intent); 
		LoginActivity.this.finish(); 
	}	

	public void getCategory(){
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {	
				
                try{     
                	Thread.sleep(1000);
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
            		params.add(new BasicNameValuePair("token", token)); 
                    String result = httpRequest(params,"kurumap/companyApp/getCategory");
                    if(result == null){
                    	canNotGetCategory.obtainMessage(REFRESH_DATA, result).sendToTarget();
                    }
                    
                    
                    SQLiteDatabase db = helper.getReadableDatabase();
                    db.execSQL("DROP TABLE IF EXISTS category"); //刪除舊有的資料表
                	db.execSQL("CREATE TABLE IF NOT EXISTS category (id int(11) primary key , parent_id int(11) , name varchar(128) ,  level int(1) , sort int(2)); ");
                	
                    JSONObject category = new JSONObject(result);
                    Log.d("debugTest","a");
                    JSONArray jsonArray = category.getJSONArray("category");
                    Log.d("debugTest","b");
                    int length = jsonArray.length(); 
                    
                    for(int i = 0;i < length; i++)
                    {
                    	Log.d("debugTest",Integer.toString(i));
                    	newCategory(jsonArray.getJSONObject(i));
                    }

                    afterGetCategory.obtainMessage(REFRESH_DATA, category).sendToTarget();
                    
                }catch (Exception e) {  
                	 Log.d("debugTest","e");
                    e.printStackTrace();  
                   
                }   
			} 
		});
		t.start();
	}	
	
	public void checkToken(){
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {	
				
                try{     
                	Thread.sleep(1000);
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
            		params.add(new BasicNameValuePair("token", token)); 
                    String result = httpRequest(params,"kurumap/companyApp/checkToken");
                    if(result == null){
                    	connectionFails.obtainMessage(REFRESH_DATA, result).sendToTarget();
                    }else{
                    	checkToken.obtainMessage(REFRESH_DATA, result).sendToTarget();
                    }
                }catch (Exception e) {  
                    e.printStackTrace();  
                }   
			} 
		});
		t.start();
	}		
	
	public void onClick(View v){

		EditText usernameText = (EditText)findViewById(R.id.username);
		EditText passwdText = (EditText)findViewById(R.id.passwd);
		
		username = (String) usernameText.getText().toString();
		passwd = (String) passwdText.getText().toString();
		
		progressDialog = ProgressDialog.show(LoginActivity.this, "登入中", "請稍後..",true);
		
		
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {				
                try{      	
                	Thread.sleep(1000);
            		List<NameValuePair> params = new ArrayList<NameValuePair>();
            		params.add(new BasicNameValuePair("username", username)); 
            		params.add(new BasicNameValuePair("passwd", passwd)); 
                    String result = httpRequest(params,"kurumap/companyApp/companyLogin");
                    if(result == null){
                    	connectionFails.obtainMessage(REFRESH_DATA, result).sendToTarget();
                    }else{
                    	mHandler.obtainMessage(REFRESH_DATA, result).sendToTarget();
                    }
                }catch (Exception e) {  
                    e.printStackTrace();  
                }   
			} 
		});
		t.start();
	}	
	
	Handler afterGetCategory = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			updateSystemParameter("category_version" , newCategoryVersion);
			Log.d("newCategoryVersion",newCategoryVersion);
			Log.d("categoryVersion",categoryVersion);
			progressDialog.dismiss(); 
			nextActivity();
		}
	};	
	
	Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			progressDialog.dismiss(); 
			
			String newToken = getDataByKey((String) msg.obj, "token");
			newCategoryVersion = getDataByKey((String) msg.obj, "categoryVersion");		

			if(newToken != "null"){
				
				if(token != "null"){
					delToken();
				}
				newToken(newToken);
				getToken();			
				
				Toast.makeText(LoginActivity.this,"登入完成 newtoken : " + token,Toast.LENGTH_SHORT).show();
				

				
				if(!categoryVersion.equals(newCategoryVersion)){
					progressDialog = ProgressDialog.show(LoginActivity.this, "資料同步中", "正在下載更資料，可能需要幾分鐘",true);
					getCategory();	
				}else{
					nextActivity();
				}	
				
			}else{
				AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
				dialog.setTitle("驗證失敗"); //設定dialog 的title顯示內容
				dialog.setMessage(getDataByKey((String) msg.obj, "msg"));
				dialog.setCancelable(false); //關閉 Android 系統的主要功能鍵(menu,home等...)
				dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {  
				    public void onClick(DialogInterface dialog, int which) {  
				      // 按下"收到"以後要做的事情
				    }  
				});
				dialog.show();
			}
		}
	};	

	Handler connectionFails = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			progressDialog.dismiss(); 
			AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
			dialog.setTitle("連線失敗 :-<"); //設定dialog 的title顯示內容
			dialog.setMessage("現在可能無法連線，請確認網路環境在試一次");
			dialog.setCancelable(false); //關閉 Android 系統的主要功能鍵(menu,home等...)
			dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {  
			    public void onClick(DialogInterface dialog, int which) {  
			      // 按下"收到"以後要做的事情
			    }  
			});
			dialog.show();
		}
	};		

	Handler canNotGetCategory = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			progressDialog.dismiss(); 
			AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
			dialog.setTitle("連線失敗 :-<"); //設定dialog 的title顯示內容
			dialog.setMessage("取得類別清單失敗，請確認網路環境");
			dialog.setCancelable(false); //關閉 Android 系統的主要功能鍵(menu,home等...)
			dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {  
			    public void onClick(DialogInterface dialog, int which) {  
			      // 按下"收到"以後要做的事情
			    }  
			});
			dialog.show();
		}
	};
	
	Handler checkToken = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			progressDialog.dismiss(); 
			
			newCategoryVersion = getDataByKey((String) msg.obj, "categoryVersion");	
			String check = getDataByKey((String) msg.obj, "check");
			
        	if(check.equals("1")){
        		if(!categoryVersion.equals(newCategoryVersion)){
					
					progressDialog = ProgressDialog.show(LoginActivity.this, "資料同步中", "正在下載新資料，可能需要幾分鐘",true);
					getCategory();	
				}else{
					nextActivity();
				}
        	}else{
        		delToken();
        	}
		}
	};	
	
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

	public void newCategory(JSONObject data) {  
		String id = getDataByjsonoj(data, "id");
		String parent_id = getDataByjsonoj(data, "parent_id");
		String name = getDataByjsonoj(data, "name");
		String level = getDataByjsonoj(data, "level");
		String sort = getDataByjsonoj(data, "sort");
		
		db = helper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("id", id);
		cv.put("parent_id", parent_id);
		cv.put("name", name);
		cv.put("level", level);
		cv.put("sort", sort);
		
		db.insert("category", "", cv);
	} 
	
	private String getDataByjsonoj(JSONObject data, String key)
	{	
		String returnData = null;
		try {
			
			returnData = data.getString(key);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return returnData;
	}
	
	public void newToken(String TOKEN) {  
		db = helper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("token", TOKEN);
		db.insert("userToken", "", cv);
	}  	

	public void delToken() {
		SQLiteDatabase db = helper.getReadableDatabase();
		db.delete("userToken", "1", null);
	} 	
	

	
	public void getToken() {  
		 SQLiteDatabase db = helper.getReadableDatabase();
		 
         String[] columns = new String[] { "token"};
         Cursor cursor = db.query("userToken", columns, null, null, null, null, "1");

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
	
	
	public void updateSystemParameter(String Key, String Value) {  
		SQLiteDatabase db = helper.getReadableDatabase();
		
		ContentValues values = new ContentValues();
		values.put("value", Value);//key為欄位名，value為值
		
        String selection = "parameterKey=?" ;
        String[] selectionArgs = new String[]{ Key };
        
        db.update("systemParameter", values, selection, selectionArgs);
        
        db.close();
   } 	
	
	public void getCategoryVersion() {  
		SQLiteDatabase db = helper.getReadableDatabase();
		 
        String[] columns = new String[] { "parameterKey" , "value"};
        String selection = "parameterKey=?" ;
        String[] selectionArgs = new String[]{ "category_version" };
        
        Cursor cursor = db.query("systemParameter", columns, selection, selectionArgs, null, null, "1");

        //String token = null;
        while(cursor.moveToNext()){
       		categoryVersion = cursor.getString(1);  
        }
        cursor.close();
        db.close();
   }  	
	
}
