package com.mr_lin.nfcapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;


public class InfoReadyToWrite extends Activity {

    Button btnToWrite;
    EditText cardIDText;
    EditText moneyText;
    EditText dateText;
    Spinner typeSpin;
    RelativeLayout main_layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_ready_to_write);
        btnToWrite = (Button)findViewById(R.id.button);
        cardIDText = (EditText)findViewById(R.id.editText);
        moneyText = (EditText)findViewById(R.id.editText2);
        dateText = (EditText)findViewById(R.id.editText3);
        typeSpin = (Spinner)findViewById(R.id.spinner);
        main_layout = (RelativeLayout)findViewById(R.id.info_ready_main_layout);

        String[] spinnerData = new String[]{"学生","教师"};
        typeSpin.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,spinnerData));

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

        btnToWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("cardID",cardIDText.getText().toString());
                bundle.putString("money",moneyText.getText().toString());
                bundle.putString("data",dateText.getText().toString());
                bundle.putString("type",typeSpin.getSelectedItem().toString());
                Log.d("NO ERR", "NO ERR");
                intent.putExtras(bundle);
                intent.setClass(InfoReadyToWrite.this,WriteTagActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onNavigateUp() {
        finish();
        return super.onNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_info_ready_to_write, menu);
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
    protected void onNewIntent(Intent intent) {
        //super.onNewIntent(intent);
        setIntent(intent);
    }
}
