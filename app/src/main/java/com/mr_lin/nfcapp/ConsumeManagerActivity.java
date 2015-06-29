package com.mr_lin.nfcapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;


public class ConsumeManagerActivity extends Activity {

    RelativeLayout main_layout;
    Button button;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consume_manager);
        main_layout = (RelativeLayout)findViewById(R.id.consume_manager_main_layout);
        button = (Button)findViewById(R.id.consumeButton);
        editText = (EditText)findViewById(R.id.consumeEditText);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Window mainWindow = this.getWindow();
            mainWindow.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            SystemBarTintManager systemBarTintManager = new SystemBarTintManager(this);
            systemBarTintManager.setStatusBarTintEnabled(true);
            systemBarTintManager.setStatusBarTintResource(R.color.consume_color);

            SystemBarTintManager.SystemBarConfig systemBarConfig = systemBarTintManager.getConfig();
            main_layout.setPadding(0, systemBarConfig.getPixelInsetTop(true), 0, systemBarConfig.getPixelInsetBottom());;
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("money",editText.getText().toString());
                intent.setClass(ConsumeManagerActivity.this, ConsumeActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_consume_manager, menu);
        return true;
    }

    @Override
    public boolean onNavigateUp() {
        finish();
        return super.onNavigateUp();
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
}
