package org.syncloud.android.ui;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.apache.log4j.Logger;
import org.syncloud.android.Preferences;
import org.syncloud.android.SyncloudApplication;

import static java.lang.String.format;

public class DeviceWebView extends ActionBarActivity {

    private static Logger logger = Logger.getLogger(DeviceWebView.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webview = new WebView(this);
        setContentView(webview);

        String host = getIntent().getStringExtra(SyncloudApplication.DEVICE_HOST);

//        getWindow().requestFeature(Window.FEATURE_PROGRESS);

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

        Preferences preferences = ((SyncloudApplication) getApplication()).getPreferences();


        String url = format(
                "http://%s:81/server/html/activate.html?redirect-email=%s&redirect-password=%s",
                host, preferences.getEmail(), preferences.getPassword());
        webview.loadUrl(url);
    }



    @JavascriptInterface
    public void saveCredentials(String user, String password) {
        logger.info("user: " + user);
    }
}
