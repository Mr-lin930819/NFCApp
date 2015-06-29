package com.mr_lin.component;

import java.util.List;
import java.util.Map;

import com.mr_lin.nfcapp.MainActivity;

import android.R;
import android.R.integer;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.media.Image;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MainListAdapter extends BaseAdapter {

	private List<Map<String, Object>> data;
	private LayoutInflater listContainer;
    private LayoutInflater layoutInflater;  
    private Context context;
    private AbsListView.LayoutParams lp;
    private WindowManager wm = null;
    
    public final class ListItem{
    	ImageView listIcon;
    	TextView listText;
    }
    
    public MainListAdapter(Context context,List<Map<String, Object>> data) {
		// TODO Auto-generated constructor stub
    	this.context = context;
    	this.layoutInflater = LayoutInflater.from(context);
    	this.data = data;
    	wm = (WindowManager)this.context.getSystemService(Context.WINDOW_SERVICE);
	}
    
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Log.d("TTT", "getView");
		ListItem listItem = null;
		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();
		lp = new AbsListView.LayoutParams(width, height/5);
		if(convertView == null){
			
			listItem = new ListItem();
			if(layoutInflater == null)
				Log.d("sss","layoutInflater NULL");
			convertView = layoutInflater.inflate(com.mr_lin.nfcapp.R.layout.main_list, null);
			convertView.setLayoutParams(lp);
			if(convertView == null)
				Log.d("sss","convertView NULL");
			listItem.listIcon = (ImageView)convertView.findViewById(com.mr_lin.nfcapp.R.id.mainlist_item_icon);
			listItem.listText = (TextView)convertView.findViewById(com.mr_lin.nfcapp.R.id.mainlist_item_text);
			convertView.setTag(listItem);
		}else{
			listItem = (ListItem)convertView.getTag();
		}
		if(listItem == null)
			Log.d("sss","listItem NULL");
		listItem.listIcon.setImageResource((Integer)data.get(position).get("icon"));
		listItem.listText.setText((String)data.get(position).get("text"));
		
		if(convertView == null)
			Log.d("sss","convertView NULL");
		
		return convertView;
	}

}
