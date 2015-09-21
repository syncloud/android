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

import static java.lang.String.format;

public class Helpers {
    private static Logger logger = Logger.getLogger(Helpers.class);

    public static Optional<String> findAccessibleUrl(DomainModel device) {

        String url1 = format("http://%s:%s",
                device.device().localEndpoint().host(),
                device.device().localEndpoint().port());
        if (checkUrl(url1))
            return Optional.of(url1);

        String url2 = format("http://%s:%s",
                device.userDomain() + ".syncloud.it",
                device.device().remoteEndpoint().port());
        if (checkUrl(url2))
            return Optional.of(url2);

        return Optional.absent();
    }

    public static boolean checkUrl(String baseUrl) {
        try {
            logger.info("trying " + baseUrl);
            HttpClientBuilder builder = HttpClientBuilder.create();
            builder.disableAutomaticRetries();
            HttpClient httpClient = builder.build();
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
