package org.syncloud.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.android.activation.Owncloud;
import org.syncloud.android.activation.Result;


public class Device extends Activity {

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        TextView deviceAddress = (TextView) findViewById(R.id.device_address);
        url = getIntent().getExtras().getString("url");
        deviceAddress.setText(url);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.device, menu);
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

   public void dns(View view) {
       Intent intent = new Intent(this, DnsActivity.class);
       intent.putExtra("url", url);
       startActivity(intent);
   }

    public void owncloud(View view) {
        Intent intent = new Intent(this, OwncloudActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }
}
