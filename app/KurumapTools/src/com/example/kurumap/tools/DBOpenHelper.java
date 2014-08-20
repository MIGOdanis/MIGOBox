package com.example.kurumap.tools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
/**
 * 默认就在数据库里创建4张表
 * @author 阿福（trygf521@126.com）
 *
 */
public class DBOpenHelper extends SQLiteOpenHelper {
	
private static final int VERSION = 1;//資料庫版本  
	   
      //建構子
public DBOpenHelper(Context context, String name, CursorFactory factory,int version) {
super(context, name, factory, version);
}

public DBOpenHelper(Context context,String name) { 
this(context, name, null, VERSION); 
} 

public DBOpenHelper(Context context, String name, int version) {  
this(context, name, null, version);  
}  

//輔助類建立時運行該方法
@Override
public void onCreate(SQLiteDatabase db) {
	String DATABASE_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS userToken (token varchar(255) primary key); ";
	db.execSQL(DATABASE_CREATE_TABLE);
	DATABASE_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS userData (data text primary key); ";
	db.execSQL(DATABASE_CREATE_TABLE);
	DATABASE_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS category (id INTEGER primary key , parent_id INTEGER , name varchar(128) ,  level INTEGER , sort INTEGER); ";
	db.execSQL(DATABASE_CREATE_TABLE);
	DATABASE_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS systemParameter (id INTEGER primary key autoincrement , parameterKey varchar(255) , value varchar(255)); ";
	db.execSQL(DATABASE_CREATE_TABLE);
	
	db.execSQL("INSERT INTO systemParameter (parameterKey,value) values ('category_version','0')");	
	db.execSQL("INSERT INTO systemParameter (parameterKey,value) values ('category_id','0')");
	db.execSQL("INSERT INTO systemParameter (parameterKey,value) values ('category_id_parent_id','0')");
}

@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//oldVersion=舊的資料庫版本；newVersion=新的資料庫版本
db.execSQL("DROP TABLE IF EXISTS userToken"); //刪除舊有的資料表
db.execSQL("DROP TABLE IF EXISTS userData"); //刪除舊有的資料表
db.execSQL("DROP TABLE IF EXISTS category"); //刪除舊有的資料表
db.execSQL("DROP TABLE IF EXISTS systemParameter"); //刪除舊有的資料表
onCreate(db);
}

@Override   
public void onOpen(SQLiteDatabase db) {     
 super.onOpen(db);       
 // TODO 每次成功打開數據庫後首先被執行     
} 

@Override
public synchronized void close() {
  super.close();
}

}
