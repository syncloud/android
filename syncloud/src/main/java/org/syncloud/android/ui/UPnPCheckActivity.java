package org.syncloud.android.ui;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.base.Optional;

import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.network.Network;
import org.syncloud.android.ui.dialog.WifiDialog;
import org.syncloud.android.upnp.Router;
import org.syncloud.android.upnp.UPnP;

import static java.lang.String.format;

public class UPnPCheckActivity extends FragmentActivity {

    public static String PARAM_FIRST_TIME = "paramFirstTime";

    private SyncloudApplication application;

    private int checksInFlight = 0;
    private boolean checkSuccess = false;

    private LinearLayout layoutIntro;
    private LinearLayout layoutFail;
    private LinearLayout layoutButtons;
    private Button btnShare;

    //TODO: How do I do a widget?
    private TextView routerText;
    private ProgressBar routerProgress;
    private ImageView routerStatusGood;
    private ImageView routerStatusBad;

    //TODO: Widget
    private TextView ipText;
    private ProgressBar ipProgress;
    private ImageView ipStatusGood;
    private ImageView ipStatusBad;

    //TODO: Widget
    private TextView portsText;
    private ProgressBar portsProgress;
    private ImageView portsStatusGood;
    private ImageView portsStatusBad;

    //TODO: Widget
    private TextView manipulationText;
    private ProgressBar manipulationProgress;
    private ImageView manipulationStatusGood;
    private ImageView manipulationStatusBad;
    private Network network;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upnp_check);

        application = (SyncloudApplication) getApplication();
        network = new Network((WifiManager) getSystemService(Context.WIFI_SERVICE));

        routerText = (TextView) findViewById(R.id.upnp_router_status);
        routerProgress = (ProgressBar) findViewById(R.id.upnp_router_progress);
        routerStatusGood = (ImageView) findViewById(R.id.upnp_router_good);
        routerStatusBad = (ImageView) findViewById(R.id.upnp_router_bad);

        ipText = (TextView) findViewById(R.id.upnp_ip_status);
        ipProgress = (ProgressBar) findViewById(R.id.upnp_ip_progress);
        ipStatusGood = (ImageView) findViewById(R.id.upnp_ip_good);
        ipStatusBad = (ImageView) findViewById(R.id.upnp_ip_bad);

        portsText = (TextView) findViewById(R.id.upnp_ports_status);
        portsProgress = (ProgressBar) findViewById(R.id.upnp_ports_progress);
        portsStatusGood = (ImageView) findViewById(R.id.upnp_ports_good);
        portsStatusBad = (ImageView) findViewById(R.id.upnp_ports_bad);

        manipulationText = (TextView) findViewById(R.id.upnp_manipulation_status);
        manipulationProgress = (ProgressBar) findViewById(R.id.upnp_manipulation_progress);
        manipulationStatusGood = (ImageView) findViewById(R.id.upnp_manipulation_good);
        manipulationStatusBad = (ImageView) findViewById(R.id.upnp_manipulation_bad);

        layoutIntro = (LinearLayout) findViewById(R.id.layout_intro);
        layoutFail = (LinearLayout) findViewById(R.id.layout_fail);
        layoutButtons = (LinearLayout) findViewById(R.id.layout_buttons);
        btnShare = (Button) findViewById(R.id.btn_share);

        Intent intent = getIntent();
        boolean firstTime = intent.getBooleanExtra(PARAM_FIRST_TIME, false);

        if (firstTime) {
            layoutIntro.setVisibility(View.VISIBLE);
        } else {
            layoutIntro.setVisibility(View.GONE);
            check();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==WifiDialog.WIFI_SETTINGS)
        {
            check();
        }
    }

    private void setLayoutEnabled(ViewGroup layout, boolean enabled) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            View view = layout.getChildAt(i);
            view.setEnabled(enabled);
        }
    }

    private void check() {
        resetTasks();
        if (application.isWifiConnected()) {
            checksInFlight = 0;
            checkSuccess = true;
            new RouterTask().execute();
        } else {
            WifiDialog dialog = new WifiDialog();
            dialog.setMessage("Router check requires Wi-Fi.");
            dialog.show(getSupportFragmentManager(), "upnp_check");
        }
    }

    private void resetTasks() {
        routerText.setText("");
        routerStatusBad.setVisibility(View.GONE);
        routerStatusGood.setVisibility(View.GONE);
        routerProgress.setVisibility(View.GONE);

        ipText.setText("");
        ipStatusBad.setVisibility(View.GONE);
        ipStatusGood.setVisibility(View.GONE);
        ipProgress.setVisibility(View.GONE);

        portsText.setText("");
        portsStatusBad.setVisibility(View.GONE);
        portsStatusGood.setVisibility(View.GONE);
        portsProgress.setVisibility(View.GONE);

        manipulationText.setText("");
        manipulationStatusBad.setVisibility(View.GONE);
        manipulationStatusGood.setVisibility(View.GONE);
        manipulationProgress.setVisibility(View.GONE);

        layoutIntro.setVisibility(View.GONE);
        layoutFail.setVisibility(View.GONE);
        layoutButtons.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_upnp_check, menu);
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

    public void onShare(View view) {
        application.reportError();
    }

    public void onCheck(View view) {
        check();
    }

    public void onSkip(View view) {
        application.getPreferences().setCheckNeeded(false);
        finish();
    }

    public void onClose(View view) {
        finish();
    }

    private void startTask() {
        checksInFlight++;
    }

    private void finishTask(boolean success) {
        checksInFlight--;
        checkSuccess &= success;
        if (checksInFlight == 0) {
            allTasksFinished();
        }
    }

    private void allTasksFinished() {
        if (checkSuccess) {
            application.getPreferences().setCheckNeeded(false);
            btnShare.setVisibility(View.GONE);
        } else {
            layoutFail.setVisibility(View.VISIBLE);
            btnShare.setVisibility(View.VISIBLE);
        }
        layoutButtons.setVisibility(View.VISIBLE);
    }

    private UPnP getUPnP() {
        return new UPnP();
    }

    public class RouterTask extends AsyncTask<Void, Void, Optional<Router>> {

        @Override
        protected void onPreExecute() {
            startTask();
            routerText.setText("Checking ...");
            routerStatusBad.setVisibility(View.GONE);
            routerStatusGood.setVisibility(View.GONE);
            routerProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Optional<Router> doInBackground(Void... voids) {
            UPnP upnp = getUPnP();
            return upnp.find();
        }

        @Override
        protected void onPostExecute(Optional<Router> result) {
            if (result.isPresent()) {
                Router router = result.get();
                routerText.setText(router.getName());
                routerStatusGood.setVisibility(View.VISIBLE);
                new IPTask(router).execute();
                new PortsTask(router).execute();
                new ManipulationTask(router).execute();
            } else {
                routerText.setText("Not able to find UPnP router");
                routerStatusBad.setVisibility(View.VISIBLE);
            }
            routerProgress.setVisibility(View.GONE);
            finishTask(result.isPresent());
        }
    }

    public class IPTask extends AsyncTask<Void, Void, Optional<String>> {

        private Router router;

        public IPTask(Router router) {
            this.router = router;
        }

        @Override
        protected void onPreExecute() {
            startTask();
            ipText.setText("Checking ...");
            ipStatusBad.setVisibility(View.GONE);
            ipStatusGood.setVisibility(View.GONE);
            ipProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Optional<String> doInBackground(Void... voids) {
            return router.getExternalIP();
        }

        @Override
        protected void onPostExecute(Optional<String> ip) {
            if (ip.isPresent()) {
                ipText.setText(ip.get());
                ipStatusGood.setVisibility(View.VISIBLE);
            } else {
                ipText.setText("Not able to find IP");
                ipStatusBad.setVisibility(View.VISIBLE);
            }
            ipProgress.setVisibility(View.GONE);
            finishTask(ip.isPresent());
        }
    }

    public class PortsTask extends AsyncTask<Void, Void, Integer> {

        private Router router;

        public PortsTask(Router router) {
            this.router = router;
        }

        @Override
        protected void onPreExecute() {
            startTask();
            portsText.setText("Checking ...");
            portsStatusBad.setVisibility(View.GONE);
            portsStatusGood.setVisibility(View.GONE);
            portsProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return router.getPortMappingsCount();
        }

        @Override
        protected void onPostExecute(Integer ports) {
            if (ports > 0) {
                portsText.setText(format("%s mapped ports", ports));
            } else {
                portsText.setText("No mapped ports, may be fine");
            }
            portsStatusGood.setVisibility(View.VISIBLE);
            portsProgress.setVisibility(View.GONE);
            finishTask(true);
        }
    }

    public class ManipulationTask extends AsyncTask<Void, Void, Boolean> {

        private Router router;
        private Optional<String> ip;

        public ManipulationTask(Router router) {
            this.router = router;
        }

        @Override
        protected void onPreExecute() {
            startTask();
            manipulationText.setText("Checking ...");
            manipulationStatusBad.setVisibility(View.GONE);
            manipulationStatusGood.setVisibility(View.GONE);
            manipulationProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            ip = network.ip();
            if (!ip.isPresent())
                return false;

            return router.canManipulatePorts(ip.get());
        }

        @Override
        protected void onPostExecute(Boolean works) {
            String ipText = " to local ip: " + ip.get();
            if (works) {
                manipulationText.setText("Can modify port mappings" + ipText);
                manipulationStatusGood.setVisibility(View.VISIBLE);
            } else {
                manipulationText.setText("Unable to modify port mappings" + ipText);
                manipulationStatusBad.setVisibility(View.VISIBLE);
            }
            manipulationProgress.setVisibility(View.GONE);
            finishTask(works);
        }
    }
}
