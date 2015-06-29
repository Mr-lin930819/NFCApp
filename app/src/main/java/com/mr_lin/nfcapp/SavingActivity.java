package com.mr_lin.nfcapp;

import android.app.Activity;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Locale;
import com.mr_lin.component.NFCReader;

public class SavingActivity extends MaAbstractActivity {

    private String[] dataToWrite;
    private ArrayList<String> dataFromReader;
    private int savingMoney;
    private TextView showTag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_saving);
        super.onCreate(savedInstanceState);
        dataToWrite = new String[]{};
        showTag = (TextView)findViewById(R.id.savingLabel);
        dataFromReader = new ArrayList<String>(){};

        Intent intent=getIntent();
        Bundle bundle;
        if( (bundle = intent.getExtras()) != null){
            Log.d("got Data","GotData");
            savingMoney = Integer.valueOf(bundle.getString("money")).intValue();
        }else {
            Log.d("No Data","No Data");
            savingMoney = 0;
        }
    }

    @Override
    protected void setStyle() {
        this.main_layout = (RelativeLayout)findViewById(R.id.save_main_layout);
        this.actionBarColorID = R.color.saving_manager_color;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_saving, menu);
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
    protected void onResume() {
        super.onResume();
        if(nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);

        if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            Tag recvTag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);

            NFCReader nfcReader = new NFCReader();
            dataFromReader = nfcReader.resolveIntent(getIntent());
            savingMoney += Integer.valueOf(dataFromReader.get(1)).intValue();
            //Build Write Data
            dataToWrite = new String[]{
                    dataFromReader.get(0),
                    String.valueOf(savingMoney),
                    dataFromReader.get(2),
                    dataFromReader.get(3)
            };

            //ndefMessage2Wirte = getNdefMessageFromRTD_TEXT(payloadStr, true);
            NdefMessage ndefMessage2Wirte = getNdefMessageFromRTD_TEXTs(dataToWrite,true);
            Log.d("test----",ndefMessage2Wirte.getRecords()[0].getPayload().toString());
            //新建后台任务进行标签的写入
            showTag.setText("正在写入标签");
            new WriteTask(this, ndefMessage2Wirte, recvTag).execute();
            Toast.makeText(this, "读写完成", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    //添加多条记录
    public NdefMessage getNdefMessageFromRTD_TEXTs(String[] content,boolean encodeInUtf8){
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
    static class WriteTask extends AsyncTask<Void,Void,Void> {
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

            Log.d("Write Tag:1", String.valueOf(size) + "to write");
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
