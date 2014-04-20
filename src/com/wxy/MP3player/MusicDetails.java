package com.wxy.MP3player;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.TextView;

public class MusicDetails extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.musuc_details);
		TextView t1=(TextView) findViewById(R.id.textView1);
		TextView t2=(TextView) findViewById(R.id.textView2);
		TextView t3=(TextView) findViewById(R.id.textView3);
		TextView t4=(TextView) findViewById(R.id.textView4);
		TextView t5=(TextView) findViewById(R.id.textView5);
		TextView t6=(TextView) findViewById(R.id.textView6);
		TextView t7=(TextView) findViewById(R.id.textView7);
		String[] projection=new String[]{"title","album","artist","_data","duration","_size"};
		Cursor cur = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projection, "_id=?", new String[]{getIntent().getExtras().getString("_id")}, null);
		if(cur.moveToFirst()){
			t1.setText("ID："+getIntent().getExtras().getString("_id"));
			t2.setText("名称："+cur.getString(cur.getColumnIndex("title")));
			t3.setText("专辑："+cur.getString(cur.getColumnIndex("album")));
			t4.setText("作者："+cur.getString(cur.getColumnIndex("artist")));
			t5.setText("位置："+cur.getString(cur.getColumnIndex("_data")));
			double _size=cur.getInt(cur.getColumnIndex("_size"));
			String sec=""+cur.getInt(cur.getColumnIndex("duration"))/1000%60;
			String min=""+cur.getInt(cur.getColumnIndex("duration"))/1000/60;
			if(sec.length()==1)
				sec="0"+sec;
			if(min.length()==1)
				min="0"+min;
			t6.setText("时间："+min+":"+sec);
			t7.setText("大小："+String.format("%.2f", _size/1024/1024)+" MB");
		}
	}

}
