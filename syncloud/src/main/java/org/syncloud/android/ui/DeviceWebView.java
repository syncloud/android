package org.syncloud.android.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.apache.http.util.EncodingUtils;
import org.apache.log4j.Logger;
import org.syncloud.android.Preferences;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.db.KeysStorage;
import org.syncloud.platform.ssh.model.Credentials;
import org.syncloud.platform.ssh.model.Identification;
import org.syncloud.platform.ssh.model.Key;

import static java.lang.String.format;

public class DeviceWebView extends ActionBarActivity {

    private static Logger logger = Logger.getLogger(DeviceWebView.class);
    private KeysStorage storage;
    private WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webview = new WebView(this);
        setContentView(webview);

        String url = getIntent().getStringExtra(SyncloudApplication.DEVICE_URL);

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

        if (getIntent().hasExtra(SyncloudApplication.DEVICE_CREDENTIALS)) {
            Credentials credentials = (Credentials) getIntent().getSerializableExtra(SyncloudApplication.DEVICE_CREDENTIALS);
            String postData = "name=" + credentials.login() +  "&password=" + credentials.password();
            webview.postUrl(url, EncodingUtils.getBytes(postData, "BASE64"));
        } else {
            webview.loadUrl(url);
        }
    }

    @JavascriptInterface
    public void saveCredentials(String user, String password) {
        logger.info("user: " + user);
        Identification identification = (Identification) getIntent().getSerializableExtra(SyncloudApplication.DEVICE_ID);
        storage.upsert(new Key(identification.mac_address, new Credentials(user, password)));
    }

    @Override
    public void onBackPressed() {
        if (webview.canGoBack()) {
            webview.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
