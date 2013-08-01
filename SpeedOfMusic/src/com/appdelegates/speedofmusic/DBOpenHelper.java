package com.appdelegates.speedofmusic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = DBSchema.SOL_DATABASE_FILENAME;
	private static final int DATABASE_VERSION = 1;

	public DBOpenHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		
	}
	
	public DBOpenHelper(Context context){
		
		super( context, DATABASE_NAME, null, DATABASE_VERSION );
		
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDB) {
		createTables(sqLiteDB);
	}

	private void createTables(SQLiteDatabase sqLiteDB) {
		sqLiteDB.execSQL(DBSchema.SQL_CREATE_SCORE_TABLE);
		sqLiteDB.execSQL(DBSchema.SQL_CREATE_EVENT_TABLE);
		sqLiteDB.execSQL(DBSchema.SQL_CREATE_LOG_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
