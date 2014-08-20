package com.example.kurumap.tools;

import java.util.ArrayList;
import java.util.Calendar;
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

import android.support.v7.app.ActionBarActivity;
import android.app.ProgressDialog;
import android.app.DatePickerDialog.OnDateSetListener;
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
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;



public class NewInformation extends ActionBarActivity {
	
	public int sy,sm,sd,ey,em,ed;
	public String statrtime = null;
	public String endtime = null;
	public String informationTypeSelected = "1";
	public String ontop = null;
	public int dateType;
	EditText st;
	EditText et;
	Calendar calender = Calendar.getInstance();
	private ArrayList<String> informationType = new ArrayList<String>();
	private ArrayList<String> informationTypeId = new ArrayList<String>();
	private String baseUrl = "http://192.168.1.111/";
	//SQLiteDatabase對象
	SQLiteDatabase db;
	//資料庫名
	public String db_name = "kurumapTools";
	public String token = null;	
	private ProgressDialog progressDialog;
	DBOpenHelper helper = new DBOpenHelper(NewInformation.this, db_name);
	
	protected static final int REFRESH_DATA = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_information);
		getToken();
		
		
		informationType.add("一般訊息");
		informationType.add("優惠訊息");
		
		informationTypeId.add("1");
		informationTypeId.add("2");
    	
		Spinner spinner = (Spinner) findViewById(R.id.inforType);
		ArrayAdapter<String> setSpinner = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, informationType);
		spinner.setAdapter(setSpinner);	
		
		
		spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
	        @Override
	        public void onItemSelected(AdapterView<?> arg0, View arg1,int position, long arg3) {
	        	informationTypeSelected = informationTypeId.get(position);
	        }
		    @Override
		    public void onNothingSelected(AdapterView<?> arg0) {
		       // TODO Auto-generated method stub
		    }
		});
		
		final CheckBox ontopCheck = (CheckBox)findViewById(R.id.ontop);
		ontopCheck.setOnCheckedChangeListener (new CheckBox.OnCheckedChangeListener(){
		
		        @Override
		        public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
		            // TODO Auto-generated method stub
			    
				    if  (ontopCheck.isChecked()){
				    	ontop = "1";
				    } else {
				    	ontop = "0";
				    }
		
		        }
		    
		});
		
		
		st = (EditText)findViewById(R.id.startTime);
        st.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	dateType = 1;
            	if(statrtime == null){
            		sy = calender.get(Calendar.YEAR);
            		sm = calender.get(Calendar.MONTH);
            		sd = calender.get(Calendar.DAY_OF_MONTH);
            	}
            	showDatePicker(sy,sm,sd);
            }
            
        });
		et = (EditText)findViewById(R.id.endTime);
        et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	dateType = 2;
            	if(endtime == null){
            		ey = calender.get(Calendar.YEAR);
            		em = calender.get(Calendar.MONTH);
            		ed = calender.get(Calendar.DAY_OF_MONTH);
            	}
            	showDatePicker(ey,em,ed);
            }
            
        });
	}
	
	private void showDatePicker(int y , int m , int d) {
		DatePickerFragment date = new DatePickerFragment();
		/**
		* Set Up Current Date Into dialog
		*/
		Calendar calender = Calendar.getInstance();
		Bundle args = new Bundle();
		args.putInt("year", y);
		args.putInt("month", m);
		args.putInt("day", d);
		date.setArguments(args);
		/**
		* Set Call back to capture selected date
		*/
		date.setCallBack(ondate);
		date.show(getSupportFragmentManager(), "Date Picker");
	}

	OnDateSetListener ondate = new OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			if(dateType == 1){
				statrtime = String.valueOf(year) + "/" + String.valueOf(monthOfYear+1) + "/" + String.valueOf(dayOfMonth);
        		sy = year;
        		sm = monthOfYear;
        		sd = dayOfMonth;				
				st.setText(statrtime);
			}else{
				endtime = String.valueOf(year) + "/" + String.valueOf(monthOfYear+1) + "/" + String.valueOf(dayOfMonth);
        		ey = year;
        		em = monthOfYear;
        		ed = dayOfMonth;				
				et.setText(endtime);				
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_information, menu);
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
	
	public void sendInformaiton(View view) { 
		progressDialog = ProgressDialog.show(NewInformation.this, "連線中", "請稍後..",true);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {				
	            try{      	
	            	Thread.sleep(1000);
	            	EditText content = (EditText)findViewById(R.id.infomation);
	        		List<NameValuePair> params = new ArrayList<NameValuePair>();
	        		params.add(new BasicNameValuePair("token", token)); 
	        		params.add(new BasicNameValuePair("message",content.getText().toString()));
	        		params.add(new BasicNameValuePair("start", statrtime));
	        		params.add(new BasicNameValuePair("end", endtime));
	        		params.add(new BasicNameValuePair("type", informationTypeSelected));
	        		params.add(new BasicNameValuePair("top", ontop));
	        		
	                String result = httpRequest(params,"kurumap/companyApp/newInformation");
	                Log.d("result",result);
	                afterUpadte.obtainMessage(REFRESH_DATA, result).sendToTarget();
	          
	            }catch (Exception e) {  
	                e.printStackTrace();  
	            }   
			} 
		});
		t.start();
	}	
	
	Handler afterUpadte = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			progressDialog.dismiss(); 
			String check = getDataByKey((String) msg.obj, "check");
			String up_msg = null;
			if(check.equals("ok")){		
				setContentView(R.layout.activity_main);
				Intent intent = new Intent();
				intent.setClass(NewInformation.this, MainActivity.class);
				startActivity(intent); 
				NewInformation.this.finish(); 
				
			}else{
				Toast.makeText(NewInformation.this, "發表失敗", Toast.LENGTH_LONG).show();
			}

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
			
			Log.d("dbe","not 200");
			return  null;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		Log.d("dbe","dbe");
		return  null;
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
	
}
