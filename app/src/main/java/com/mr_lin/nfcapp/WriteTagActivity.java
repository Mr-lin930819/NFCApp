package com.mr_lin.nfcapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;


public class WriteTagActivity extends Activity {

    NfcAdapter nfcAdapter;
    TextView showTag;

    String[] dataToWrite = null;
    PendingIntent pendingIntent;
    IntentFilter[] mIntentFilters = null;
    NdefMessage ndefMessage2Wirte = null;
    String[][] mTechLists = null;

    RelativeLayout main_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_tag);
        main_layout = (RelativeLayout)findViewById(R.id.write_main_layout);
        showTag = (TextView)findViewById(R.id.write_tag_text);
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

        Intent intent=getIntent();
        Bundle bundle;
        if( (bundle = intent.getExtras()) != null){
            Log.d("got Data","GotData");
            dataToWrite = new String[]{
                    bundle.getString("cardID"),
                    bundle.getString("money"),
                    bundle.getString("data"),
                    bundle.getString("type")
            };
        }else {
            Log.d("No Data","No Data");
            dataToWrite = new String[]{};
        }


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Window mainWindow = this.getWindow();
            mainWindow.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            SystemBarTintManager systemBarTintManager = new SystemBarTintManager(this);
            systemBarTintManager.setStatusBarTintEnabled(true);
            systemBarTintManager.setStatusBarTintResource(R.color.write_tag_color);

            SystemBarTintManager.SystemBarConfig systemBarConfig = systemBarTintManager.getConfig();
            main_layout.setPadding(0, systemBarConfig.getPixelInsetTop(true), 0, systemBarConfig.getPixelInsetBottom());;
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setTitle("写入信息");

        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                |Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        mIntentFilters = new IntentFilter[]{techDetected};

        mTechLists = new String[][]{
                new String[]{Ndef.class.getName()},
                new String[]{NdefFormatable.class.getName()}
        };

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_write_tag, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if(nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);

        if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            Tag recvTag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);


            String[] payloadStrs = {"105052012050","张三","2014-9-10","学生"};
            //ndefMessage2Wirte = getNdefMessageFromRTD_TEXT(payloadStr, true);
            ndefMessage2Wirte = getNdefMessageFromRTD_TEXTs(dataToWrite,true);
            Log.d("test----",ndefMessage2Wirte.getRecords()[0].getPayload().toString());
            //新建后台任务进行标签的写入
            showTag.setText("正在写入标签");
            new WriteTask(this, ndefMessage2Wirte, recvTag).execute();
            Toast.makeText(this,"读写完成",Toast.LENGTH_SHORT);
            finish();
        }

    }

    @Override
    public boolean onNavigateUp() {
        finish();
        return super.onNavigateUp();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //super.onNewIntent(intent);
        setIntent(intent);
    }

    public NdefMessage getNdefMessageFromRTD_TEXT(String content,boolean encodeInUtf8){
        Locale locale = new Locale("en","US");
        byte[] langByte = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = encodeInUtf8?Charset.forName("UTF-8"):Charset.forName("UTF-16");
        int utfBit = encodeInUtf8 ? 0:(1 << 7);
        char status = (char) (utfBit + langByte.length);

        byte[] textBytes = content.getBytes(utfEncoding);
        byte[] data = new byte[1+langByte.length+textBytes.length];

        data[0] = (byte)status;
        System.arraycopy(langByte,0,data,1,langByte.length);
        System.arraycopy(textBytes, 0, data, 1 + langByte.length, textBytes.length);

        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,NdefRecord.RTD_TEXT,new byte[0],data);

        return new NdefMessage(new NdefRecord[]{textRecord});
    }

    //添加多条记录
    static public NdefMessage getNdefMessageFromRTD_TEXTs(String[] content,boolean encodeInUtf8){
        Locale locale = new Locale("en","US");
        byte[] langByte = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = encodeInUtf8?Charset.forName("UTF-8"):Charset.forName("UTF-16");
        int utfBit = encodeInUtf8 ? 0:(1 << 7);
        char status = (char) (utfBit + langByte.length);

        NdefRecord[] records = new NdefRecord[content.length];
        int i = 0;
        for(String readyToWriteBytes:content) {
            byte[] textBytes = readyToWriteBytes.getBytes(utfEncoding);
            byte[] data = new byte[1 + langByte.length + textBytes.length];

            data[0] = (byte) status;
            System.arraycopy(langByte, 0, data, 1, langByte.length);
            System.arraycopy(textBytes, 0, data, 1 + langByte.length, textBytes.length);
            records[i] = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
            i++;
        }

        return new NdefMessage(records);
    }

    //并行后台执行
    static class WriteTask extends AsyncTask<Void,Void,Void>{
        Activity host = null;
        NdefMessage message = null;
        Tag tag = null;
        String text = null;

        WriteTask(Activity activity,NdefMessage message,Tag tag){
            this.host = activity;
            this.message = message;
            this.tag = tag;
        }

        @Override
        protected Void doInBackground(Void... params) {
            int size = message.toByteArray().length;

            Log.d("Write Tag:1",String.valueOf(size)+"to write");
            try {
                Ndef ndef = Ndef.get(tag);
                if (ndef == null) {
                    NdefFormatable formatable = NdefFormatable.get(tag);
                    if (formatable != null) {
                        try {
                            formatable.connect();
                            Log.d("Write Tag:1","formatable to write");
                            try {
                                formatable.format(message);
                            } catch (Exception e) {
                                text = "failed to format tag.";
                            }
                        } catch (Exception e) {
                            text = "failed to connect tag";
                        } finally {
                            try {
                                formatable.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        text = "NDEF is not supported in this tag";
                    }
                } else {
                    try {
                        ndef.connect();
                        Log.d("Write Tag:1", "ndef to write");
                        if (!ndef.isWritable()) {
                            text = "tag is read-only";
                        } else if (ndef.getMaxSize() < size) {
                            text = "the data cannot written to tag";
                        } else {
                            try {
                                ndef.writeNdefMessage(message);
                            } catch (FormatException e) {

                            }
                            text = "message is written tag";
                        }
                    } catch (IOException e) {
                        text = "tag refused to connect";
                        e.printStackTrace();

                    } finally {
                        ndef.close();
                    }
                }
            } catch (Exception e){
                text = "write operation is failed";
                e.printStackTrace();
            }

//            WriteTagActivity host_activity = (WriteTagActivity)host;
//            host_activity.showTag.setText(text);
            return null;
        }
    }
}
