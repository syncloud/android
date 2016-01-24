package org.syncloud.android.network;

import com.google.common.base.Optional;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;
import org.syncloud.android.core.platform.model.DomainModel;

import java.io.IOException;

public class Helpers {
    private static Logger logger = Logger.getLogger(Helpers.class);

    public static Optional<String> findAccessibleUrl(String mainDomain, DomainModel domain) {
        String dnsUrl = domain.getDnsUrl(mainDomain);
        if (dnsUrl != null && checkUrl(dnsUrl))
            return Optional.of(dnsUrl);

        String externalUrl = domain.getExternalUrl();
        if (externalUrl != null && checkUrl(externalUrl))
            return Optional.of(externalUrl);

        String internalUrl = domain.getInternalUrl();
        if (internalUrl != null && checkUrl(internalUrl))
            return Optional.of(internalUrl);

        return Optional.absent();
    }

    public static boolean checkUrl(String url) {
        try {
            logger.info("Trying: " + url);
            HttpClientBuilder builder = HttpClientBuilder.create();
            builder.disableAutomaticRetries();
            HttpClient httpClient = builder.build();
            HttpGet httpGet = new HttpGet(url);
            HttpParams params = httpGet.getParams();
            params.setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
            httpGet.setParams(params);
            int statusCode = httpClient.execute(httpGet).getStatusLine().getStatusCode();
            logger.info("status code: " + statusCode);
            if (statusCode == 302)
                return true;
        } catch (IOException e) {
            logger.info("Trying " + url + " failed with error: " + e.getMessage());
        }
        return false;
    }
}
