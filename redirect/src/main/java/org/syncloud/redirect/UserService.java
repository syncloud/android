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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.syncloud.redirect.model.RestResult.error;
import static org.syncloud.redirect.model.RestResult.value;

public class UserService {

    public static RestResult<String> getUser(String email, String password, String apiUrl1) {

        CloseableHttpClient http = HttpClients.createDefault();
        HttpGet get = new HttpGet(apiUrl1 +
                "/user/get?email=" + email +
                "&password=" + password);

        try {
            CloseableHttpResponse response = http.execute(get);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200)
                return value("User exists");
            ObjectMapper mapper = new ObjectMapper();
            InputStream jsonResponse = response.getEntity().getContent();
// Uncomment to see json in debug
//            String textJsonResponse = readText(jsonResponse);
            RestError restError = mapper.readValue(jsonResponse, RestError.class);
            response.close();
            return error(restError);
        } catch (Exception e) {
            return error(new RestError(e.getMessage()));
        }
    }

    public static RestResult<String> createUser(String email, String password, String apiUrl1) {
        return createUser(email, password, null, apiUrl1);
    }

    public static RestResult<String> createUser(String email, String password, String domain, String apiUrl1) {

        CloseableHttpClient http = HttpClients.createDefault();
        HttpPost post = new HttpPost(apiUrl1 + "/user/create");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("email", email));
        nvps.add(new BasicNameValuePair("password", password));
        if (domain != null)
            nvps.add(new BasicNameValuePair("user_domain", domain));
        try {
            post.setEntity(new UrlEncodedFormEntity(nvps));
            CloseableHttpResponse response = http.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200)
                return value("User registered");

            ObjectMapper mapper = new ObjectMapper();
            InputStream jsonResponse = response.getEntity().getContent();
// Uncomment to see json in debug
//            String textJsonResponse = readText(jsonResponse);
            RestError restError = mapper.readValue(jsonResponse, RestError.class);
            response.close();
            return error(restError);
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
