package org.syncloud.android.activity;

import android.app.Activity;
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


public class OwncloudActivity extends Activity {

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owncloud);
        url = getIntent().getExtras().getString("url");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.owncloud, menu);
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

    public void activate(View view) {

        EditText loginText = (EditText) findViewById(R.id.login);
        EditText passText = (EditText) findViewById(R.id.pass);
        //TODO: Some validation

        String login = loginText.getText().toString();
        String pass = passText.getText().toString();


        finishSetupAsync().execute(url, login, pass);

    }

    private AsyncTask<String, Void, Result<String>> finishSetupAsync() {
        return new AsyncTask<String, Void, Result<String>>() {

            @Override
            protected void onPreExecute() {
                TextView status = (TextView) findViewById(R.id.owncloud_status);
                status.setText("activating ...");
            }

            @Override
            protected Result<String> doInBackground(String... input) {
                return Owncloud.finishSetup(input[0], input[1], input[2]);
            }

            @Override
            protected void onPostExecute(Result<String> result) {
                TextView status = (TextView) findViewById(R.id.owncloud_status);
                status.setText(result.hasError() ? result.getError() : result.getValue());
            }
        };
    }
}
