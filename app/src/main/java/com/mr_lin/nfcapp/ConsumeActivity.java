package com.mr_lin.nfcapp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import com.mr_lin.component.NFCReader;
import com.mr_lin.nfcapp.SystemBarTintManager.SystemBarConfig;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class ConsumeActivity extends Activity {

	NfcAdapter nfcAdapter; 
	String readResult = null;
	TextView showTag;
	private String[] dataToWrite;
	private ArrayList<String> dataFromReader;
	private int consumeMoney;
	PendingIntent pendingIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_consume);
		showTag = (TextView)findViewById(R.id.consume_text);
		ActionBar actionBar=getActionBar();
		if(actionBar == null)
			Log.d("STATU","NO");
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
			Window mainWindow = this.getWindow();
			mainWindow.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			
			SystemBarTintManager systemBarTintManager = new SystemBarTintManager(this);
			systemBarTintManager.setStatusBarTintEnabled(true);
			systemBarTintManager.setStatusBarTintResource(R.color.consume_color);
			
			SystemBarConfig systemBarConfig = systemBarTintManager.getConfig();	
			RelativeLayout main_layout = (RelativeLayout)findViewById(R.id.consume_main);
			main_layout.setPadding(0, systemBarConfig.getPixelInsetTop(true), 0, systemBarConfig.getPixelInsetBottom());;
		}
		
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);  
        if (nfcAdapter == null) {
        	Toast.makeText(this, "设备不支持NFC！", Toast.LENGTH_SHORT).show();
            finish();  
            return;  
        }  
        if (!nfcAdapter.isEnabled()) {
        	Toast.makeText(this,"请在系统设置中先启用NFC功能！",Toast.LENGTH_SHORT).show();
            finish();  
            return;  
        }
		pendingIntent = PendingIntent.getActivity(
				this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		Intent intent = getIntent();
		if(intent.getStringExtra("money") != null) {
			consumeMoney = Integer.valueOf(intent.getStringExtra("money")).intValue();
		}else{
			consumeMoney = 0;
		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	}
	
	@Override
	public boolean onNavigateUp() {
		// TODO Auto-generated method stub
		
		finish();
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		return super.onNavigateUp();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.consume, menu);
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

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
		//if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {
		//getActionBar().setTitle("尝试读取数据");
		if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())){
			getActionBar().setTitle("解析数据");
			Tag recvTag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
			showTag.setText("消费价格"+consumeMoney+"元");
			NFCReader nfcReader = new NFCReader();
			dataFromReader = nfcReader.resolveIntent(getIntent());

			int nowMoney = Integer.valueOf(dataFromReader.get(1)).intValue() - consumeMoney ;
			//Build Write Data
			dataToWrite = new String[]{
					dataFromReader.get(0),
					String.valueOf(nowMoney),
					dataFromReader.get(2),
					dataFromReader.get(3)
			};

			NdefMessage ndefMessage2Wirte = WriteTagActivity.getNdefMessageFromRTD_TEXTs(dataToWrite, true);
			//新建后台任务进行标签的写入
			new WriteTagActivity.WriteTask(this, ndefMessage2Wirte, recvTag).execute();
			pushDialog("成功消费" + String.valueOf(consumeMoney) + "元\n目前卡内余额为" + String.valueOf(nowMoney)+"元");
		}else {
			getActionBar().setTitle("无法读取");
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		//super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	protected void onPause() {
		super.onPause();
		nfcAdapter.disableForegroundDispatch(this);
	}

	private void processIntent(Intent intent) {
		// TODO Auto-generated method stub
        //取出封装在intent中的TAG  
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);  
        for (String tech : tagFromIntent.getTechList()) {  
            System.out.println(tech);  
        }  
        boolean auth = false;  
        //读取TAG  
        MifareClassic mfc = MifareClassic.get(tagFromIntent);  
        try {  
            String metaInfo = "";  
            //Enable I/O operations to the tag from this TagTechnology object.  
            mfc.connect();  
            int type = mfc.getType();//获取TAG的类型  
            int sectorCount = mfc.getSectorCount();//获取TAG中包含的扇区数  
            String typeS = "";  
            switch (type) {  
            case MifareClassic.TYPE_CLASSIC:  
                typeS = "TYPE_CLASSIC";  
                break;  
            case MifareClassic.TYPE_PLUS:  
                typeS = "TYPE_PLUS";  
                break;  
            case MifareClassic.TYPE_PRO:  
                typeS = "TYPE_PRO";  
                break;  
            case MifareClassic.TYPE_UNKNOWN:  
                typeS = "TYPE_UNKNOWN";  
                break;  
            }  
            metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"  
                    + mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize() + "B\n";  
            for (int j = 0; j < sectorCount; j++) {  
                //Authenticate a sector with key A.  
                auth = mfc.authenticateSectorWithKeyA(j,  
                        MifareClassic.KEY_DEFAULT);  
                int bCount;  
                int bIndex;  
                if (auth) {  
                    metaInfo += "Sector " + j + ":验证成功\n";  
                    // 读取扇区中的块  
                    bCount = mfc.getBlockCountInSector(j);  
                    bIndex = mfc.sectorToBlock(j);  
                    for (int i = 0; i < bCount; i++) {  
                        byte[] data = mfc.readBlock(bIndex);  
                        metaInfo += "Block " + bIndex + " : "  
                                + bytesToHexString(data) + "\n";  
                        bIndex++;  
                    }  
                } else {  
                    metaInfo += "Sector " + j + ":验证失败\n";  
                }  
            }  
            //promt.setText(metaInfo);
			//Toast.makeText(this,metaInfo,Toast.LENGTH_SHORT);
			showTag.setText(metaInfo);
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
	
	 private String bytesToHexString(byte[] src) {  
        StringBuilder stringBuilder = new StringBuilder("0x");  
        if (src == null || src.length <= 0) {  
            return null;  
        }  
        char[] buffer = new char[2];  
        for (int i = 0; i < src.length; i++) {  
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);  
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);  
            System.out.println(buffer);  
            stringBuilder.append(buffer);  
        }  
        return stringBuilder.toString();  
    }


	 private boolean readFromTag(Intent intent){  
	    Parcelable[] rawArray = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);  
	    NdefMessage mNdefMsg = (NdefMessage)rawArray[0];  
	    NdefRecord mNdefRecord = mNdefMsg.getRecords()[0];  
	    try {  
	        if(mNdefRecord != null){  
	            readResult = new String(mNdefRecord.getPayload(),"UTF-8");  
	            showTag.setText(readResult);

				return true;
	         }  
	    }  
	    catch (UnsupportedEncodingException e) {  
	         e.printStackTrace();  
	    };  
	    return false;  
	 }  
	 
	 @SuppressLint("NewApi")
	private boolean writeToTag() {
		 Tag tag=getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);  
	        Ndef ndef=Ndef.get(tag);  
	        try {  
	            ndef.connect();  
	            NdefRecord ndefRecord=NdefRecord.createTextRecord(null, "test");  
	            NdefRecord[] records={ndefRecord};  
	            NdefMessage ndefMessage=new NdefMessage(records);  
	            ndef.writeNdefMessage(ndefMessage); 
	            
	        } catch (IOException e1) {  
	            // TODO Auto-generated catch block  
	            e1.printStackTrace();  
	        } catch (FormatException e) {  
	        	e.printStackTrace();  
	        } 
		return true;
	}

	private void pushDialog(String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setTitle("读写完成");
		builder.setPositiveButton("继续消费", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				finish();
			}
		});
		builder.setNegativeButton("再刷一次", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
}


