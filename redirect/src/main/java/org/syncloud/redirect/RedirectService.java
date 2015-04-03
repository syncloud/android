package org.syncloud.redirect;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.syncloud.redirect.model.RedirectApiException;
import org.syncloud.redirect.model.RedirectException;
import org.syncloud.redirect.model.RestResult;
import org.syncloud.redirect.model.RestUser;
import org.syncloud.redirect.model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static java.net.URLEncoder.encode;
import static org.syncloud.redirect.jackson.Jackson.createObjectMapper;

public class RedirectService implements IUserService {

    private static Logger logger = Logger.getLogger(RedirectService.class);

    private String apiUrl;

    public RedirectService(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    private static ObjectMapper mapper = createObjectMapper();

    public User getUser(String email, String password) {
        String urlString;
        try {
            //TODO: Vlad Here should be some abstraction for form and url parameters
            urlString = "/user/get?email=" + encode(email, "utf-8") + "&password=" + encode(password, "utf-8");
        } catch (UnsupportedEncodingException e) {
            String message = "Failed to form request";
            logger.error(message, e);
            throw new RedirectException(message);
        }

        HttpGet get = new HttpGet(apiUrl + urlString);

        String json = executeRequest(get);

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
        HttpPost post = new HttpPost(apiUrl + "/user/create");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("email", email));
        nvps.add(new BasicNameValuePair("password", password));
        post.setEntity(packParameters(nvps));

        String json = executeRequest(post);

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
        CloseableHttpClient http = HttpClients.createDefault();
        HttpPost post = new HttpPost(apiUrl + "/domain/drop_device");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("email", email));
        nvps.add(new BasicNameValuePair("password", password));
        nvps.add(new BasicNameValuePair("user_domain", user_domain));
        post.setEntity(packParameters(nvps));

        executeRequest(post);
    }

    private String executeRequest(HttpUriRequest request) {
        Response response = getResponse(request);
        RestResult restResponse = null;
        try {
            restResponse = mapper.readValue(response.output, RestResult.class);
        } catch (IOException e) {
            String message = "Failed to deserialize json";
            logger.error(message+" "+response.output, e);
            throw new RedirectException(message);
        }
        restResponse.statusCode = response.statusCode;
        checkStatusCode(restResponse);
        return response.output;
    }

    private void checkStatusCode(RestResult rest) {
        if (rest.statusCode != 200) {
            String message = "Response has bad status code: "+rest.statusCode;
            logger.error(message);
            throw new RedirectApiException(message, rest);
        }
    }

    public class Response {
        public String output;
        public int statusCode;

        public Response(int statusCode, String output) {
            this.statusCode = statusCode;
            this.output = output;
        }
    }

    private Response getResponse(HttpUriRequest request) {
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient http = HttpClients.createDefault();
            response = http.execute(request);
            InputStream jsonResponse = response.getEntity().getContent();
            String textJsonResponse = readText(jsonResponse);
            int statusCode = response.getStatusLine().getStatusCode();
            return new Response(statusCode, textJsonResponse);
        } catch (IOException e) {
            String message = "Failed to get response";
            logger.error("Failed to get response", e);
            throw new RedirectException(message);
        } finally {
            if (response != null)
                try { response.close(); } catch (IOException e) {}
        }
    }

    private static HttpEntity packParameters(List<NameValuePair> nvps) {
        try {
            return new UrlEncodedFormEntity(nvps);
        } catch (UnsupportedEncodingException e) {
            String message = "Failed to form request";
            logger.error(message, e);
            throw new RedirectException(message);
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
