package org.syncloud.android.ui;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.common.base.Optional;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EncodingUtils;
import org.apache.log4j.Logger;
import org.syncloud.android.Preferences;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.db.KeysStorage;
import org.syncloud.platform.ssh.model.Credentials;
import org.syncloud.platform.ssh.model.Device;
import org.syncloud.platform.ssh.model.Endpoint;
import org.syncloud.platform.ssh.model.Key;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public class DeviceWebView extends ActionBarActivity {

    private static Logger logger = Logger.getLogger(DeviceWebView.class);
    private KeysStorage storage;
    private WebView webview;
    private Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webview = new WebView(this);
        setContentView(webview);


        webview.getSettings().setJavaScriptEnabled(true);

        final Activity activity = this;
        webview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
                activity.setProgress(progress * 1000);
            }
        });
        webview.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });
        webview.clearCache(true);
        webview.addJavascriptInterface(this, "Android");

        SyncloudApplication application = (SyncloudApplication) getApplication();
        storage = application.keysStorage();
        preferences = application.getPreferences();

        if (getIntent().hasExtra(SyncloudApplication.DEVICE_OPEN)) {
            Device device = (Device) getIntent().getSerializableExtra(SyncloudApplication.DEVICE_OPEN);

            new UrlSelectorAsync(this, device, webview).execute();

        } else {
            Endpoint endpoint = (Endpoint) getIntent().getSerializableExtra(SyncloudApplication.DEVICE_DISCOVERY);

            String url = format("http://%s:81/server/html/activate.html?release=%s",
                    endpoint.host(),
                    preferences.getVersion());

            logger.info("GET: " + url.replace(preferences.getPassword(), "***"));

            webview.loadUrl(url);
        }
    }



    @JavascriptInterface
    public void saveCredentials(String mac_address, String user, String password) {
        logger.info("saving: " + mac_address + ", " + user);
        storage.upsert(new Key(mac_address, new Credentials(user, password)));
    }

    @JavascriptInterface
    public String getRedirectLogin() {
        return preferences.getEmail();
    }

    @JavascriptInterface
    public String getRedirectPassword() {
        return preferences.getPassword();
    }

    @JavascriptInterface
    public String getRelease() {
        return preferences.getVersion();
    }

    @JavascriptInterface
    public String getApiUrl() {
        return preferences.getApiUrl();
    }

    @JavascriptInterface
    public String getDomain() {
        return preferences.getDomain();
    }

    @Override
    public void onBackPressed() {
        if (webview.canGoBack()) {
            webview.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public static class UrlSelectorAsync extends AsyncTask<Void, Void, Optional<String>> {
        private Activity activity;
        private Device device;
        private WebView webview;

        public UrlSelectorAsync(Activity activity, Device device, WebView webview) {
            this.activity = activity;
            this.device = device;
            this.webview = webview;
        }

        @Override
        protected Optional<String> doInBackground(Void... voids) {
            return findAccessibleUrl(device);
        }

        @Override
        protected void onPostExecute(Optional<String> baseUrl) {

            if (!baseUrl.isPresent()) {
                logger.error("unable to connect ");
                activity.finish();
                return;
            }

            String url = format("%s/server/rest/login", baseUrl.get());
            String postData =
                    "name=" + device.credentials().login() + "&" +
                    "password=" + device.credentials().password();
            logger.info("POST: " + url);
            logger.info("data: " + postData.replace("=" + device.credentials().password(), "***"));
            webview.postUrl(url, EncodingUtils.getBytes(postData, "BASE64"));

        }

        private Optional<String> findAccessibleUrl(Device device) {

            String url1 = format("http://%s:%s", device.localEndpoint().host(), device.localEndpoint().port());
            if (checkUrl(url1))
                return Optional.of(url1);

            String url2 = format("http://%s:%s", device.remoteEndpoint().host(), device.remoteEndpoint().port());
            if (checkUrl(url2))
                return Optional.of(url2);

            return Optional.absent();
        }

        private boolean checkUrl(String baseUrl) {
            try {
                logger.info("trying " + baseUrl);
                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet(baseUrl);
                HttpParams params = httpGet.getParams();
                params.setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
                httpGet.setParams(params);
                int statusCode = httpClient.execute(httpGet).getStatusLine().getStatusCode();
                logger.info("status code: " + statusCode);
                if (statusCode == 302)
                    return true;
            } catch (IOException e) {
                logger.info(baseUrl + " failed with " + e.getMessage() + ", trying another endpoint");
            }
            return false;
        }
    }
}
