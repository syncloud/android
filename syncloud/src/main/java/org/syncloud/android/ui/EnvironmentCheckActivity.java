package org.syncloud.android.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.common.check.UPnP;
import org.syncloud.common.model.UPnPStatus;

import static java.lang.String.format;

public class EnvironmentCheckActivity extends ActionBarActivity {

    private static Logger logger = Logger.getLogger(EnvironmentCheckActivity.class.getName());

    private TextView statusView;
    private ProgressBar progressBar;
    private Button checkBtn;
    private ImageView statusGood;
    private ImageView statusBad;
    private SyncloudApplication application;
    private ImageButton sendbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_environment_check);

        application = (SyncloudApplication) getApplication();
        statusView = (TextView) findViewById(R.id.check_status);
        progressBar = (ProgressBar) findViewById(R.id.check_progress);
        checkBtn = (Button) findViewById(R.id.environment_check_btn);
        sendbtn = (ImageButton) findViewById(R.id.environment_status_send_btn);
        statusGood = (ImageView) findViewById(R.id.status_good);
        statusBad = (ImageView) findViewById(R.id.status_bad);

        check();
    }

    private void check() {
        new CheckTask().execute((Void) null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_environment_check, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(this, SettingsActivity.class), 2);
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendReport(View view) {
        application.reportError();
    }

    public void onCheck(View view) {
        check();
    }

    public class CheckTask extends AsyncTask<Void, Void, Optional<UPnPStatus>> {

        @Override
        protected void onPreExecute() {
            statusView.setText("Checking ...");
            checkBtn.setEnabled(false);
            sendbtn.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            statusBad.setVisibility(View.GONE);
            statusGood.setVisibility(View.GONE);
        }

        @Override
        protected Optional<UPnPStatus> doInBackground(Void... voids) {
            return new UPnP().checkStatus(10, new AndroidUpnpServiceConfiguration());
        }

        @Override
        protected void onPostExecute(Optional<UPnPStatus> result) {
            if (result.isPresent()) {
                UPnPStatus status = result.get();
                logger.debug(status.toString());
                statusView.setText(format("Name: %s\nExternal IP: %s", status.routerName, status.externalAddress));
                statusGood.setVisibility(View.VISIBLE);
            } else {
                statusView.setText("UPnP is not available");
                statusBad.setVisibility(View.VISIBLE);
            }
            progressBar.setVisibility(View.INVISIBLE);
            checkBtn.setEnabled(true);
            sendbtn.setEnabled(true);
        }
    }
}
