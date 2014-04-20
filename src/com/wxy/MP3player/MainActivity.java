package com.wxy.MP3player;

import java.io.File;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Contacts.People;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.MediaStore;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class MainActivity extends ListActivity implements OnItemLongClickListener {
	
	private List<HashMap<String, String>> t=new ArrayList<HashMap<String,String>>();
	private String[] modeStrings={"单曲播放","单曲循环","列表播放","列表循环","随机播放"};
	public static Button playButton=null;
	private Button stopButton=null;
	private MediaPlayer mediaPlayer=null;
	private SeekBar seekBar=null;
	private Timer mTimer=null;
	private TextView timeTextView=null;
	private SimpleAdapter simpleAdapter;
	private boolean flag=true;
	private int ItemLongClick=111;
	private int Setting=222;
	private boolean duandianbofang=true;
	private int[] idMap=null;
	private int playListSize=0;
	private int sortOrder=0;
	private int _id=-1;
	private int position=-1;
	
	private int previousMusicId=0;
	private int playingMusicId=0;
	private int nextMusicId=1;
	
	private int STOP=-1;
	private int PLAYING=0;
	private int PAUSE=1;
	private int RELEASED=2;
	private int STATE=RELEASED;
	
	private int modeNumber=5;
	private int SINGLEPLAY=0;
	private int SINGLEREPEAT=1;
	private int LISTPLAY=2;
	private int LISTEREPEAT=3;
	private int RANDOM=4;
	private int MODE=LISTEREPEAT;

	@Override
 	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		playButton=(Button) findViewById(R.id.playButton);
		stopButton=(Button) findViewById(R.id.NextButton);
		seekBar=(SeekBar) findViewById(R.id.seekBar1);
		timeTextView=(TextView) findViewById(R.id.textView1);
		playButton.setOnClickListener((OnClickListener) new Play());
		stopButton.setOnClickListener((OnClickListener) new Next());
		seekBar.setOnSeekBarChangeListener(new SeekBarChange());
		seekBar.setMax(0);
		getListView().setOnItemLongClickListener(this);
		loadPlayList();
		loadSetting();
		if (duandianbofang) {
//			loadMusic();
		}
		mTimer = new Timer();  
		mTimer.schedule(new TimerTask() {    
            public void run() {
            	if(STATE==PLAYING){
                	if(STATE==PLAYING && flag && mediaPlayer!=null){
                		try {
                    		seekBar.setProgress(mediaPlayer.getCurrentPosition());
						} catch (IllegalStateException e) {
							e.printStackTrace();
						}
                	}
                	try {
                		if(!mediaPlayer.isPlaying() && STATE==PLAYING){
                    		nextMusic();
                    	}
					} catch (IllegalStateException e) {
						e.printStackTrace();
					}
                	runOnUiThread(new Runnable() {
    					public void run() {
    						if(STATE==PLAYING && mediaPlayer!=null){
        						String sec=""+mediaPlayer.getCurrentPosition()/1000%60;
        						String min=""+mediaPlayer.getCurrentPosition()/1000/60;
        						if(sec.length()==1)
        							sec="0"+sec;
        						if(min.length()==1)
        							min="0"+min;
        						timeTextView.setText(min+":"+sec);
        						getWindow().setTitle(getText(R.string.app_name)+"---"+getData(idMap[playingMusicId],new String[]{"title"}));
        					}
    					}
    				});
            	}
            }
        }, 100,100);
	}
 	
	private String getData(int album_id,String[] projection) {
		Cursor cur = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projection, "_id=?", new String[]{album_id+""}, null);
		String path = null;
		if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
			cur.moveToNext();
			path = cur.getString(0);
		}
		cur.close();
		cur = null;
		return path;
	}
	
	@Override
	protected void onStart() {
		loadPlayList();
		super.onStart();
	}

	@Override
	protected void onDestroy() {	
		stopService(new Intent(this,PlayMusic.class));
		if(duandianbofang && STATE!=RELEASED)
			writeMusicPositionToDatabase(idMap[playingMusicId],mediaPlayer.getCurrentPosition());
		writeSettingToDatabase();
		releaseMusic();
		super.onDestroy();
	}
	
	private void writeMusicPositionToDatabase(int _id,int position){	
		DatabaseHelper databaseHelper=new DatabaseHelper(this, "Setting", null, 1);
		SQLiteDatabase database=databaseHelper.getWritableDatabase();
		ContentValues values=new ContentValues();
		values.put("_id", _id);
		values.put("position", position);
		database.update("setting", values, null, null);
		database.close();
		databaseHelper.close();
	}
	
	private void writeSettingToDatabase(){	
		DatabaseHelper databaseHelper=new DatabaseHelper(this, "Setting", null, 1);
		SQLiteDatabase database=databaseHelper.getWritableDatabase();
		ContentValues values=new ContentValues();
		values.put("mode", MODE);	
		SQLiteDatabase dbDatabase = databaseHelper.getReadableDatabase();
		Cursor cursor = dbDatabase.query("setting", new String[]{"mode"}, null, null, null, null, null);
		if(cursor.moveToNext()){
			database.update("setting", values, null, null);
		}
		else {
			database.insert("setting", null, values);
		}
		dbDatabase.close();
		database.close();
		databaseHelper.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		getMenuInflater().inflate(R.menu.main, menu);
		menu.add(0, 1, 1, R.string.update_music_list);
		menu.add(0, 2, 2, modeStrings[MODE]);
		return true;
	}

	class Play implements OnClickListener{
		@Override
		public void onClick(View v) {
			if (STATE==PLAYING) {
				pauseMusic();
			}
			else if (STATE==PAUSE){
				mediaPlayer.start();
				STATE=PLAYING;
				startService(new Intent(MainActivity.this,PlayMusic.class).putExtra("cmd", false));
				playButton.setBackgroundResource(R.drawable.pause);
			}
			else {
				playMusic();
				playButton.setBackgroundResource(R.drawable.pause);
			}
		}
	}
	
	class Next implements OnClickListener{
		@Override
		public void onClick(View v) {
			nextMusic();
			playButton.setBackgroundResource(R.drawable.pause);
		}		
	}

	private void loadPlayList(){
		String[] projection = new String[] { "_id","title","album","artist","_data","duration","_size" };
		DatabaseHelper databaseHelper=new DatabaseHelper(this, "Deleted", null, 1);
		SQLiteDatabase rDatabase=databaseHelper.getReadableDatabase();
		Cursor cursor=rDatabase.query("deleted_file", null, null, null, null, null, null);
		String selection="_size > 1048576";
		String[] selectionArgs=new String[cursor.getCount()];
		int i=0;
		t.clear();
		while (cursor.moveToNext()) {
			selection+=" and _id !=?";
			selectionArgs[i++]=cursor.getInt(cursor.getColumnIndex("_id"))+"";
		}
		rDatabase.close();
		databaseHelper.close();
		String[] sortString={"title","title desc","artist","artist desc","album","album desc","duration","duration desc"};
		Cursor qCursor=getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortString[sortOrder]);
		if(qCursor==null)
			return;
		idMap=new int[qCursor.getCount()];
		HashMap<String, String> map=null;
		i=0;
		while(qCursor.moveToNext()){
			idMap[i++]=qCursor.getInt(qCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
			map=new HashMap<String, String>();
			String sec=""+qCursor.getInt(qCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))/1000%60;
			String min=""+qCursor.getInt(qCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))/1000/60;
			if(sec.length()==1)
				sec="0"+sec;
			if(min.length()==1)
				min="0"+min;
			map.put("mp3_title", i+". "+qCursor.getString(qCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
//			map.put("mp3_artist", "歌手: "+qCursor.getString(qCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
//			map.put("mp3_album", "专辑: "+qCursor.getString(qCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
			map.put("mp3_duration", min+":"+sec);
			t.add(map);
		}
		playListSize=i;
		simpleAdapter=new SimpleAdapter(this, t, R.layout.mp3_item, new String[]{"mp3_title","mp3_duration","mp3_artist","mp3_album"}, new int[]{R.id.mp3_title,R.id.mp3_duration,R.id.mp3_artist,R.id.mp3_album});
		setListAdapter(simpleAdapter);
	}
	
	private void loadSetting(){
		DatabaseHelper databaseHelper=new DatabaseHelper(this, "Setting", null, 1);
		SQLiteDatabase dbDatabase = databaseHelper.getReadableDatabase();
		Cursor cursor = dbDatabase.query("setting", new String[]{"mode","duandianbofang","sortOrder","_id","position"}, null, null, null, null, null);
		if(cursor.moveToNext()){
			cursor.moveToFirst();
			if(cursor.getColumnIndex("duandianbofang")!=-1){
				duandianbofang=cursor.getInt(cursor.getColumnIndex("duandianbofang"))==1?true:false;
				if(duandianbofang && cursor.getColumnIndex("_id")!=-1 && cursor.getColumnIndex("position")!=-1){
					_id=cursor.getInt(cursor.getColumnIndex("_id"));
					position=cursor.getInt(cursor.getColumnIndex("position"));
				}
			}
			if(cursor.getColumnIndex("mode")!=-1){
				MODE=cursor.getInt(cursor.getColumnIndex("mode"));
			}
			if(cursor.getColumnIndex("sortOrder")!=-1){
				sortOrder=cursor.getInt(cursor.getColumnIndexOrThrow("sortOrder"));
			}
		}
		dbDatabase.close();
		databaseHelper.close();
	}
	
	private void loadMusic(){
		for (int i = 0; i < idMap.length; i++) {
			if (idMap[i]==_id) {
				nextMusicId=i;
				playMusic();
				mediaPlayer.seekTo(position);
				break;
			}
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		playingMusicId=-1;
		nextMusicId=position;
		playMusic();
		playButton.setBackgroundResource(R.drawable.pause);
		super.onListItemClick(l, v, position, id);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==1){		//更新列表
			DatabaseHelper databaseHelper=new DatabaseHelper(this, "Deleted", null, 1);
			SQLiteDatabase wDatabase=databaseHelper.getWritableDatabase();
			wDatabase.delete("deleted_file", null, null);
			wDatabase.close();
			databaseHelper.close();
			loadPlayList();
		}
		else if(item.getItemId()==2){		//循环方式
			MODE=(MODE+1)%modeNumber;
			item.setTitle(modeStrings[MODE]);
		}
		else if(item.getTitle() == getText(R.string.action_settings)){		//设置
			startActivityForResult(new Intent(this,Setting.class), Setting);
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void playMusic(){
		if(STATE != RELEASED){
			releaseMusic();
		}
		if(!new File(getData(idMap[nextMusicId],new String[]{"_data"})).exists()){
			Toast.makeText(this, "歌曲文件文件不存在，请刷新列表", Toast.LENGTH_SHORT).show();
			return;
		}
		startService(new Intent(this,PlayMusic.class).putExtra("_id", idMap[nextMusicId]));
//		mediaPlayer=MediaPlayer.create(this, Uri.parse("file://"+getData(idMap[nextMusicId],new String[]{"_data"})));
//		mediaPlayer.setLooping(false);
//		mediaPlayer.start();
//		STATE=PLAYING;
//		playingMusicId=nextMusicId;
//		seekBar.setMax(mediaPlayer.getDuration());
	}	
	
	private void pauseMusic(){
		if(mediaPlayer!=null){
//			mediaPlayer.pause();
			startService(new Intent(this,PlayMusic.class).putExtra("cmd", true));
//			STATE=PAUSE;
//			playButton.setBackgroundResource(R.drawable.play);
//			STATE=PAUSE;
		}
	}
	
	private void releaseMusic(){
		if(mediaPlayer!=null){
			mediaPlayer.release();
			STATE=RELEASED;
		}
	}
	
	private void nextMusic(){
		previousMusicId=playingMusicId;
		if(MODE == SINGLEPLAY){
			nextMusicId=STOP;
		}
		else if (MODE == SINGLEREPEAT) {
			nextMusicId=playingMusicId;
		} 
		else if(MODE == LISTPLAY){
			nextMusicId=playingMusicId+1;
		}
		else if(MODE == LISTEREPEAT){
			nextMusicId=(playingMusicId+1)%playListSize;
		}
		else if(MODE == RANDOM){
			while((nextMusicId=(int) (Math.random()*Double.MAX_VALUE%playListSize))==playingMusicId);
		}
		if(nextMusicId>-1 && nextMusicId<playListSize){
			playMusic();
		}
		else {
			nextMusicId=0;
		}
	}	
	
	class SeekBarChange implements OnSeekBarChangeListener{
		int arg1;
		boolean arg2;

		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {	
			if(STATE==RELEASED){
				seekBar.setProgress(0);					
			}	
			this.arg1=arg1;
			this.arg2=arg2;
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			flag=false;
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			flag=true;
			if(arg2){
					mediaPlayer.seekTo(arg1);
					if (STATE==PAUSE){
						mediaPlayer.start();
						STATE=PLAYING;
						//playButton.setText(R.string.suspend);
						playButton.setBackgroundResource(R.drawable.pause);
					}
			}
		}		
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this).setTitle("确认退出吗？")
		.setIcon(android.R.drawable.ic_dialog_info)
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
				finish();
		    }
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    }
		}).show();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		startActivityForResult(new Intent(this,ItemLongClick.class).putExtra("onItemLongClickId", arg2), ItemLongClick);
		return false;
	}	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==ItemLongClick && resultCode==RESULT_OK){
			if(data.getExtras().getInt("position")!=2 && data.getExtras().getInt("onItemLongClickId")==playingMusicId){
				if(STATE==PLAYING)
					nextMusic();
				else
					nextMusicId=0;
			}
			if(data.getExtras().getInt("position")==0){
				DatabaseHelper databaseHelper=new DatabaseHelper(this, "Deleted", null, 1);
				SQLiteDatabase wDatabase=databaseHelper.getWritableDatabase();
				ContentValues values=new ContentValues();
				values.put("_id", idMap[data.getExtras().getInt("onItemLongClickId")]);
				wDatabase.insert("deleted_file", null, values);
				wDatabase.close();
				databaseHelper.close();
				loadPlayList();
			}
			else if(data.getExtras().getInt("position")==1){
				new File(getData(idMap[data.getExtras().getInt("onItemLongClickId")],new String[]{"_data"})).delete();
				getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "_id = ?", new String[]{idMap[data.getExtras().getInt("onItemLongClickId")]+""});
				loadPlayList();
			}	
			else if(data.getExtras().getInt("position")==2){
				int id=idMap[data.getExtras().getInt("onItemLongClickId")];
				startActivity(new Intent(this,MusicDetails.class).putExtra("_id", getData(id, new String[]{"_id"})));
			}
		}
		if (requestCode==Setting && resultCode==RESULT_OK) {
			duandianbofang=data.getExtras().getBoolean("duandianbofang");
			sortOrder=data.getExtras().getInt("sortOrder");
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
		
}
