package org.syncloud.android.network;

import com.google.common.base.Optional;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.syncloud.android.core.platform.model.DomainModel;

import java.io.IOException;
import java.security.KeyStore;

public class Helpers {
    private static Logger logger = Logger.getLogger(Helpers.class);

    public static Optional<String> findAccessibleUrl(String mainDomain, DomainModel domain) {
        String dnsUrl = domain.getDnsUrl(mainDomain);
        if (dnsUrl != null && checkUrl(dnsUrl, 302))
            return Optional.of(dnsUrl);

        String externalUrl = domain.getExternalUrl();
        if (externalUrl != null && checkUrl(externalUrl, 302))
            return Optional.of(externalUrl);

        String internalUrl = domain.getInternalUrl();
        if (internalUrl != null && checkUrl(internalUrl, 302))
            return Optional.of(internalUrl);

        return Optional.absent();
    }

    public static boolean checkUrl(String url, int wnatedStatusCode) {
        try {
            logger.info("Trying: " + url);
            HttpClient httpClient = getNewHttpClient();
            HttpGet httpGet = new HttpGet(url);
            HttpParams params = httpGet.getParams();
            params.setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
            httpGet.setParams(params);
            int statusCode = httpClient.execute(httpGet).getStatusLine().getStatusCode();
            logger.info("status code: " + statusCode);
            if (statusCode == wnatedStatusCode)
                return true;
        } catch (IOException e) {
            logger.info("Trying " + url + " failed with error: " + e.getMessage());
        }
        return false;
    }

    public static HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SyncloudSSLSocketFactory sf = new SyncloudSSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }
}
