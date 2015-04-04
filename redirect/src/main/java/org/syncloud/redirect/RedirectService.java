package org.syncloud.redirect;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.syncloud.redirect.model.RedirectException;
import org.syncloud.redirect.model.RestUser;
import org.syncloud.redirect.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.syncloud.redirect.jackson.Jackson.createObjectMapper;

public class RedirectService implements IUserService {

    private static Logger logger = Logger.getLogger(RedirectService.class);

    private WebService webService;

    public RedirectService(String apiUrl) {
        this.webService = new WebService(apiUrl);
    }

    private static ObjectMapper mapper = createObjectMapper();

    public User getUser(String email, String password) {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("email", email));
        parameters.add(new BasicNameValuePair("password", password));

        String json = webService.execute("GET", "/user/get", parameters);

        try {
            RestUser restUser = mapper.readValue(json, RestUser.class);
            return restUser.data;
        } catch (IOException e) {
            String message = "Failed to deserialize json";
            logger.error(message+" "+json, e);
            throw new RedirectException(message);
        }
    }

    public User createUser(String email, String password) {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("email", email));
        parameters.add(new BasicNameValuePair("password", password));

        String json = webService.execute("POST", "/user/create", parameters);

        try {
            RestUser restUser = mapper.readValue(json, RestUser.class);
            return restUser.data;
        } catch (IOException e) {
            String message = "Failed to deserialize json";
            logger.error(message+" "+json, e);
            throw new RedirectException(message);
        }
    }

    public void dropDevice(String email, String password, String user_domain) {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("email", email));
        parameters.add(new BasicNameValuePair("password", password));
        parameters.add(new BasicNameValuePair("user_domain", user_domain));

        webService.execute("POST", "/domain/drop_device", parameters);
    }
}
