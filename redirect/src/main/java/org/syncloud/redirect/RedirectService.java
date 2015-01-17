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
import org.syncloud.redirect.model.RestUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.syncloud.common.Jackson.createObjectMapper;
import static org.syncloud.redirect.UserResult.error;

public class RedirectService implements IUserService {

    private String apiUrl;

    public RedirectService(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    private static ObjectMapper mapper = createObjectMapper();

    public UserResult getUser(String email, String password) {

        CloseableHttpClient http = HttpClients.createDefault();
        HttpGet get = new HttpGet(apiUrl + "/user/get?email=" + email + "&password=" + password);

        try {
            CloseableHttpResponse response = http.execute(get);
            InputStream jsonResponse = response.getEntity().getContent();
            String textJsonResponse = readText(jsonResponse);
            int statusCode = response.getStatusLine().getStatusCode();
            response.close();
            RestUser restUser = mapper.readValue(textJsonResponse, RestUser.class);
            return new UserResult(statusCode, restUser);
        } catch (Exception e) {
            return error("Failed to get user", e);
        }
    }

    public UserResult createUser(String email, String password) {
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
            return new UserResult(statusCode, restUser);
        } catch (Exception e) {
            return error("Failed to create user", e);
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
