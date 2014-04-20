package com.wxy.MP3player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class ItemLongClick extends ListActivity {
	private List<Map<String, String>> list =new ArrayList<Map<String,String>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		HashMap<String, String> map=new HashMap<String, String>();
		map.put("1", "从列表中删除歌曲");
		list.add(map);
		map=new HashMap<String, String>();
		map.put("1", "删除歌曲文件");
		list.add(map);
		map=new HashMap<String, String>();
		map.put("1", "歌曲信息");
		list.add(map);
		SimpleAdapter simpleAdapter=new SimpleAdapter(this, list, R.layout.long_click_item, new String[]{"1"}, new int[]{R.id.long_click_item});
		setListAdapter(simpleAdapter);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item_long_click);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		setResult(RESULT_OK, getIntent().putExtra("position", position).putExtra("onItemLongClickId", getIntent().getExtras().getInt("onItemLongClickId")));
		finish();
		super.onListItemClick(l, v, position, id);
	}
}
