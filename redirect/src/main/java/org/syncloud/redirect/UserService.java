package org.syncloud.redirect;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.syncloud.redirect.model.RestError;
import org.syncloud.redirect.model.RestResult;
import org.syncloud.redirect.model.RestUser;
import org.syncloud.redirect.model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.syncloud.common.Jackson.createObjectMapper;
import static org.syncloud.redirect.model.RestResult.error;
import static org.syncloud.redirect.model.RestResult.value;

public class UserService {

    private String apiUrl;
    private IUserCache cache;

    public UserService(String apiUrl, IUserCache cache) {
        this.apiUrl = apiUrl;
        this.cache = cache;
    }

    private static ObjectMapper mapper = createObjectMapper();

    public RestResult<User> getUser(String email, String password, boolean canUseCache) {

        CloseableHttpClient http = HttpClients.createDefault();
        HttpGet get = new HttpGet(apiUrl +
                "/user/get?email=" + email +
                "&password=" + password);

        try {
            CloseableHttpResponse response = http.execute(get);
            InputStream jsonResponse = response.getEntity().getContent();
            String textJsonResponse = readText(jsonResponse);
            int statusCode = response.getStatusLine().getStatusCode();
            response.close();
            RestUser restUser = mapper.readValue(textJsonResponse, RestUser.class);
            if (statusCode == 200) {
                User user = restUser.data;
                cache.save(user);
                return value(user);
            }
            if (statusCode != 403 && canUseCache) {
                User user = cache.load();
                return value(user);
            }
            return error(restUser);
        } catch (Exception e) {
            return error(new RestError(e.getMessage()));
        }
    }

    public RestResult<User> createUser(String email, String password) {
        CloseableHttpClient http = HttpClients.createDefault();
        HttpPost post = new HttpPost(apiUrl + "/user/create");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("email", email));
        nvps.add(new BasicNameValuePair("password", password));
        try {
            post.setEntity(new UrlEncodedFormEntity(nvps));
            CloseableHttpResponse response = http.execute(post);
            InputStream jsonResponse = response.getEntity().getContent();
            String textJsonResponse = readText(jsonResponse);
            int statusCode = response.getStatusLine().getStatusCode();
            response.close();
            RestUser restUser = mapper.readValue(textJsonResponse, RestUser.class);
            if (statusCode == 200)
                return value(restUser.data);
            return error(restUser);
        } catch (Exception e) {
            return error(new RestError(e.getMessage()));
        }
    }

    private static String readText(InputStream inputStream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        return total.toString();
    }
}
