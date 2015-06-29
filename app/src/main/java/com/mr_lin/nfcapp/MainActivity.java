package com.mr_lin.nfcapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.mr_lin.nfcapp.SystemBarTintManager;
import com.mr_lin.nfcapp.SystemBarTintManager.SystemBarConfig;
import com.mr_lin.component.MainListAdapter;

import android.R.anim;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {

	private ListView functionSelectMenu;
	private List<Map<String, Object>> listData;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		functionSelectMenu = (ListView)findViewById(R.id.funcMenuList);
		Vector<String> funcMenuData = new Vector<String>();
		
		listData = new ArrayList<Map<String,Object>>();

		addItemToMyListData("图书馆管理",R.drawable.book_manager_small);
		addItemToMyListData("宿舍管理", R.drawable.book_manager_small);
		addItemToMyListData("充值",R.drawable.book_manager_small);
		addItemToMyListData("消费", R.drawable.book_manager_small);
		addItemToMyListData("新卡注册", R.drawable.book_manager_small);
		
		
		funcMenuData.add("图书馆管理");
		funcMenuData.add("宿舍管理");
		funcMenuData.add("充值");
		funcMenuData.add("消费");
		funcMenuData.add("新卡注册");

		//functionSelectMenu.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, funcMenuData));
		functionSelectMenu.setAdapter(new MainListAdapter(this, listData));
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
			Window mainWindow = this.getWindow();
			mainWindow.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			
			SystemBarTintManager systemBarTintManager = new SystemBarTintManager(this);
			systemBarTintManager.setStatusBarTintEnabled(true);
			systemBarTintManager.setStatusBarTintResource(R.color.MainWindowColor);
			
			SystemBarConfig systemBarConfig = systemBarTintManager.getConfig();	
			functionSelectMenu.setPadding(0, systemBarConfig.getPixelInsetTop(true), 0, systemBarConfig.getPixelInsetBottom());;
		}
		
		functionSelectMenu.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Log.d("TAG", String.valueOf(position));

				Intent intent = new Intent();
				switch (position) {
					case 0:
						intent.setClass(MainActivity.this, LibraryManagerActivity.class);
						startActivity(intent);
						break;
					case 1:
						intent.setClass(MainActivity.this, DormManagerActivity.class);
						startActivity(intent);
						break;
					case 2:
						intent.setClass(MainActivity.this,SavingManagerActivity.class);
						startActivity(intent);
						break;
					//Consume
					case 3:
						intent.setClass(MainActivity.this, ConsumeManagerActivity.class);
						startActivity(intent);
						overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
						break;
					//新卡注册界面
					case 4:
						intent.setClass(MainActivity.this,InfoReadyToWrite.class);
						startActivity(intent);
					default:
						break;
				}
				
			}
		});
	}

	private void addItemToMyListData(String itemText,int icon_drawable){
		Map<String, Object> newItemBody = new HashMap<String, Object>();
		newItemBody.put("icon", icon_drawable);
		newItemBody.put("text", itemText);
		this.listData.add(newItemBody);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
		}else if (id == R.id.action_exit) {
			android.os.Process.killProcess(android.os.Process.myPid());   //获取PID 
			System.exit(0);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
