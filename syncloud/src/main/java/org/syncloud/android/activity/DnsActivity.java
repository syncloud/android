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
import org.syncloud.model.Result;


public class DnsActivity extends Activity {

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dns);
        url = getIntent().getExtras().getString("url");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dns, menu);
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

    public void activateName(View view) {

        EditText loginText = (EditText) findViewById(R.id.name_login);
        EditText passText = (EditText) findViewById(R.id.name_pass);
        EditText emailText = (EditText) findViewById(R.id.name_email);

        EditText owncloudUserText = (EditText) findViewById(R.id.login);
        EditText owncloudPasswordText = (EditText) findViewById(R.id.pass);
        //TODO: Some validation

        String login = loginText.getText().toString();
        String pass = passText.getText().toString();
        String email = emailText.getText().toString();
        String owncloudUser = owncloudUserText.getText().toString();
        String owncloudPassword = owncloudPasswordText.getText().toString();

        TextView status = (TextView) findViewById(R.id.dns_status);
        boolean valid = true;
        if (login.matches("")){
            status.setText("enter name login");
            valid = false;
        }

        if (pass.matches("")) {
            status.setText("enter name password");
            valid = false;
        }
       /* if (email.matches(""))
            status.setText("enter login");*/
        if (owncloudUser.matches("")) {
            status.setText("enter device login");
            valid = false;
        }
        if (owncloudPassword.matches("")) {
            status.setText("enter device password");
            valid = false;
        }

        if (valid)
            activateNameAsync().execute(url, login, pass, email, owncloudUser, owncloudPassword);
    }

    private AsyncTask<String, Void, Result<String>> activateNameAsync() {
        return new AsyncTask<String, Void, Result<String>>() {

            @Override
            protected void onPreExecute() {
                TextView status = (TextView) findViewById(R.id.dns_status);
                status.setText("activating name ...");
            }

            @Override
            protected Result<String> doInBackground(String... input) {
                return Owncloud.activateName(input[0], input[1], input[2], input[3], input[4], input[5]);
            }

            @Override
            protected void onPostExecute(Result<String> result) {
                TextView status = (TextView) findViewById(R.id.dns_status);
                status.setText(result.hasError() ? result.getError() : result.getValue());
            }
        };
    }
}
