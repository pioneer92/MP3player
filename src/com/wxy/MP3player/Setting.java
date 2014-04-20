package com.wxy.MP3player;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener; 

public class Setting extends Activity{
	CheckBox duandianbofang=null;
	private Spinner spinner;
	private ArrayAdapter<?> adapter;
	private int sortOrder=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.setting);
		super.onCreate(savedInstanceState);
		duandianbofang=(CheckBox) findViewById(R.id.checkBox1);
		spinner = (Spinner) findViewById(R.id.spinner1);
		adapter = ArrayAdapter.createFromResource(this, R.array.sortOrder, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new SpinnerXMLSelectedListener());
		spinner.setVisibility(View.VISIBLE);
		DatabaseHelper databaseHelper=new DatabaseHelper(this, "Setting", null, 1);
		SQLiteDatabase dbDatabase = databaseHelper.getReadableDatabase();
		Cursor cursor = dbDatabase.query("setting", null, null, null, null, null, null);
		if(cursor.moveToNext()){
			cursor.moveToFirst();
			if(cursor.getColumnIndex("duandianbofang")!=-1){
				duandianbofang.setChecked(cursor.getInt(cursor.getColumnIndex("duandianbofang"))==1?true:false);
			}
			if(cursor.getColumnIndex("sortOrder")!=-1){
				sortOrder=cursor.getInt(cursor.getColumnIndexOrThrow("sortOrder"));
				spinner.setSelection(sortOrder);
			}
		}
		dbDatabase.close();
		databaseHelper.close();
	}
	
	class SpinnerXMLSelectedListener implements OnItemSelectedListener{
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			sortOrder=arg2;
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {

		}
		
	}
	
	@Override
	public void onBackPressed() {
		setResult(RESULT_OK, getIntent().putExtra("duandianbofang", duandianbofang.isChecked()).putExtra("sortOrder", sortOrder));
		DatabaseHelper databaseHelper=new DatabaseHelper(this, "Setting", null, 1);
		SQLiteDatabase database=databaseHelper.getWritableDatabase();
		ContentValues values=new ContentValues();
		values.put("duandianbofang", duandianbofang.isChecked());
		values.put("sortOrder", sortOrder);
		SQLiteDatabase dbDatabase = databaseHelper.getReadableDatabase();
		Cursor cursor = dbDatabase.query("setting", new String[]{"duandianbofang"}, null, null, null, null, null);
		if(cursor.moveToNext()){
			database.update("setting", values, null, null);
		}
		else {
			database.insert("setting", null, values);
		}
		dbDatabase.close();
		database.close();
		databaseHelper.close();
		finish();
	}
}