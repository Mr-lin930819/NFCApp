package com.mr_lin.nfcapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.mr_lin.component.NFCReader;

import java.util.ArrayList;


public class DormManagerActivity extends MaAbstractActivity {

    private ArrayList<String> userData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_dorm_manager);
        super.onCreate(savedInstanceState);
        userData = new ArrayList<String>(){};
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        Log.d("----------------", "get");
        if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            NFCReader nfcReader = new NFCReader();
            userData = nfcReader.resolveIntent(getIntent());
            String showMsg = "学号:"+userData.get(0)+"\n学生可以进入此宿舍";
            pushDialog(showMsg);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.test, menu);
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
    protected void setStyle() {
        this.main_layout = (RelativeLayout)findViewById(R.id.dorm_main_layout);
        this.actionBarColorID = R.color.dorm_manager_color;
    }

    private void pushDialog(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(DormManagerActivity.this);
        builder.setMessage(message);
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                DormManagerActivity.this.finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
