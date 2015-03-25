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
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.syncloud.redirect.model.RestError;
import org.syncloud.redirect.model.RestUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.net.URLEncoder.encode;
import static org.syncloud.redirect.jackson.Jackson.createObjectMapper;
import static org.syncloud.redirect.UserResult.error;

public class RedirectService implements IUserService {

    private static Logger logger = Logger.getLogger(RedirectService.class);

    private String apiUrl;

    public RedirectService(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    private static ObjectMapper mapper = createObjectMapper();

    public UserResult getUser(String email, String password) {

        try {
            CloseableHttpClient http = HttpClients.createDefault();
            String urlString = "/user/get?email=" + encode(email, "utf-8") + "&password=" + encode(password, "utf-8");
            HttpGet get = new HttpGet(apiUrl + urlString);
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
            logger.info("calling: " + post.getURI());
            logger.info("entity: " + EntityUtils.toString(post.getEntity()));
            CloseableHttpResponse response = http.execute(post);
            InputStream jsonResponse = response.getEntity().getContent();
            String textJsonResponse = readText(jsonResponse);
            int statusCode = response.getStatusLine().getStatusCode();
            response.close();
            logger.debug(textJsonResponse);
            RestUser restUser = mapper.readValue(textJsonResponse, RestUser.class);
            return new UserResult(statusCode, restUser);
        } catch (Exception e) {
            return error("Failed to create user", e);
        }
    }

    public void dropDevice(String email, String password, String user_domain) {
        CloseableHttpClient http = HttpClients.createDefault();
        HttpPost post = new HttpPost(apiUrl + "/domain/drop_device");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("email", email));
        nvps.add(new BasicNameValuePair("password", password));
        nvps.add(new BasicNameValuePair("user_domain", user_domain));
        try {
            post.setEntity(new UrlEncodedFormEntity(nvps));
            CloseableHttpResponse response = http.execute(post);
            InputStream jsonResponse = response.getEntity().getContent();
            String textJsonResponse = readText(jsonResponse);
            int statusCode = response.getStatusLine().getStatusCode();
            response.close();
            RestError restResponse = mapper.readValue(textJsonResponse, RestError.class);
            checkError(statusCode, restResponse);
        } catch (Exception e) {
            throw new RedirectError("Drop device failed", e);
        }
    }

    private void checkError(int statusCode, RestError rest) {
        if (statusCode != 200) {
            boolean expected = statusCode == 400 && statusCode == 403;
            throw new RedirectError(expected, rest);
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
