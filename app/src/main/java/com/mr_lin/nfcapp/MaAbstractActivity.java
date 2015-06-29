package com.mr_lin.nfcapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * Created by root on 15-6-21.
 */
public abstract class MaAbstractActivity extends Activity {

    protected RelativeLayout main_layout;
    protected int actionBarColorID;
    protected NfcAdapter nfcAdapter;
    protected PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle();
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

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Window mainWindow = this.getWindow();
            mainWindow.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            SystemBarTintManager systemBarTintManager = new SystemBarTintManager(this);
            systemBarTintManager.setStatusBarTintEnabled(true);
            systemBarTintManager.setStatusBarTintResource(actionBarColorID);

            SystemBarTintManager.SystemBarConfig systemBarConfig = systemBarTintManager.getConfig();
            main_layout.setPadding(0, systemBarConfig.getPixelInsetTop(true), 0, systemBarConfig.getPixelInsetBottom());
        }
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                        |Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //super.onNewIntent(intent);
        setIntent(intent);
    }

    protected abstract void setStyle();

    @Override
    public boolean onNavigateUp() {
        finish();
        return super.onNavigateUp();
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }
}
