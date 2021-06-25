package org.syncloud.android.core.redirect;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.syncloud.android.core.common.SyncloudException;
import org.syncloud.android.core.common.WebService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.syncloud.android.core.common.jackson.Jackson.createObjectMapper;

public class RedirectService implements IUserService {

    public static String getApiUrl(String mainDomain) {
        return "https://api."+mainDomain;
    }

    private static Logger logger = Logger.getLogger(RedirectService.class);

    private WebService webService;

    public RedirectService(String apiUrl) {
        this.webService = new WebService(apiUrl);
    }

    private static ObjectMapper mapper = createObjectMapper();

    public org.syncloud.android.core.redirect.model.User getUser(String email, String password) {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("email", email));
        parameters.add(new BasicNameValuePair("password", password));

        String json = webService.execute("GET", "/user/get", parameters);

        try {
            org.syncloud.android.core.redirect.model.UserResult restUser = mapper.readValue(json, org.syncloud.android.core.redirect.model.UserResult.class);
            return restUser.data;
        } catch (IOException e) {
            String message = "Failed to deserialize json";
            logger.error(message+" "+json, e);
            throw new SyncloudException(message);
        }
    }

    public org.syncloud.android.core.redirect.model.User createUser(String email, String password) {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("email", email));
        parameters.add(new BasicNameValuePair("password", password));

        String json = webService.execute("POST", "/user/create", parameters);

        try {
            org.syncloud.android.core.redirect.model.UserResult restUser = mapper.readValue(json, org.syncloud.android.core.redirect.model.UserResult.class);
            return restUser.data;
        } catch (IOException e) {
            String message = "Failed to deserialize json";
            logger.error(message+" "+json, e);
            throw new SyncloudException(message);
        }
    }

}
