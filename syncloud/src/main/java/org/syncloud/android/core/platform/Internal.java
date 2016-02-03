package org.syncloud.android.core.platform;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.syncloud.android.core.common.Result;
import org.syncloud.android.core.common.SyncloudException;
import org.syncloud.android.core.common.WebService;
import org.syncloud.android.core.platform.model.Identification;

import java.io.IOException;
import java.util.ArrayList;

import static java.lang.String.format;

public class Internal {

    private static Logger logger = Logger.getLogger(Tools.class);
    public static final ObjectMapper JSON = new ObjectMapper();

    public Optional<Identification> activate(
            String host,
            String redirectDomain,
            String redirectEmail,
            String redirectPassword,
            String domain,
            String login,
            String password) {

        ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("domain", redirectDomain));
        parameters.add(new BasicNameValuePair("redirect-email", redirectEmail));
        parameters.add(new BasicNameValuePair("redirect-password", redirectPassword));
        parameters.add(new BasicNameValuePair("redirect-domain", domain));
        parameters.add(new BasicNameValuePair("name", login));
        parameters.add(new BasicNameValuePair("password", password));

        WebService webService = new WebService(format("http://%s:81/server/rest", host));
        String json = webService.execute("POST", "/activate", parameters);

        try {
            Result<Identification> result = JSON.readValue(json, new TypeReference<Result<Identification>>() {});
            return Optional.of(result.data);
        } catch (IOException e) {
            String message = "Unable to parse activate response";
            logger.error(message+" "+json, e);
            throw new SyncloudException(message);
        }
    }
}
