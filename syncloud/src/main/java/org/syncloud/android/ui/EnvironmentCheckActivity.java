package org.syncloud.android.ui;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.syncloud.android.R;
import org.syncloud.common.check.UPnP;
import org.syncloud.common.model.UPnPStatus;

public class EnvironmentCheckActivity extends ActionBarActivity {

    private static Logger logger = Logger.getLogger(EnvironmentCheckActivity.class.getName());

    private TextView statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_environment_check);

        statusView = (TextView) findViewById(R.id.check_status);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class CheckTask extends AsyncTask<Void, Void, Optional<UPnPStatus>> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Optional<UPnPStatus> doInBackground(Void... voids) {
            return new UPnP().checkStatus(10000, new AndroidUpnpServiceConfiguration());
        }

        @Override
        protected void onPostExecute(Optional<UPnPStatus> result) {
            if (result.isPresent()) {
                logger.debug(result.get().toString());
                statusView.setText(result.get().toString());
            } else {
                statusView.setText("UPnP is not available");
            }
        }
    }
}
