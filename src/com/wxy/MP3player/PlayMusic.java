package com.wxy.MP3player;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

public class PlayMusic extends Service{
	private MediaPlayer mediaPlayer=null;
	private int STOP=-1;
	private int PLAYING=0;
	private int PAUSE=1;
	private int RELEASED=2;
	private int STATE=RELEASED;

	@Override
	public void onCreate() {
		Log.e("PlayMusic----->", "onCreate");
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Log.e("PlayMusic----->", "onDestroy");
		releaseMusic();
		super.onDestroy();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("PlayMusic----->", "onStartCommand");
		Log.e("_id", intent.getExtras().getInt("_id")+"");
		Log.e("cmd", intent.getExtras().getBoolean("cmd")+"");
		Log.e("position", intent.getExtras().getInt("position")+"");
		if (intent.getExtras().getInt("_id")>0) {
			playMusic(intent.getExtras().getInt("_id"));
		}
		if(intent.getExtras().getBoolean("cmd")){
			pauseMusic();
		}
		else {
			if(mediaPlayer!=null)
				mediaPlayer.start();
			else{
				Cursor qCursor=getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,new String[] { "_id" }, null, null, null);
				if (qCursor.moveToFirst()) {
					playMusic(qCursor.getInt(qCursor.getColumnIndex("_id")));
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.e("PlayMusic----->", "onBind");
		return null;
	}
	
	private void playMusic(int _id){
		if(STATE != RELEASED){
			releaseMusic();
		}
		mediaPlayer=MediaPlayer.create(this, Uri.parse("file://"+getData(_id,new String[]{"_data"})));
		mediaPlayer.setLooping(false);
		mediaPlayer.start();
		STATE=PLAYING;
	}	
	
	private void pauseMusic(){
		if(mediaPlayer!=null){
			mediaPlayer.pause();
			STATE=PAUSE;
//			playButton.setBackgroundResource(R.drawable.play);
			STATE=PAUSE;
		}
	}
	
	private void releaseMusic(){
		if(mediaPlayer!=null){
			mediaPlayer.release();
			STATE=RELEASED;
		}
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

}
