package org.syncloud.android.core.common;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.syncloud.android.core.common.jackson.Jackson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static java.net.URLEncoder.encode;

public class WebService {

    private static Logger logger = Logger.getLogger(WebService.class);

    private String apiUrl = "";

    public WebService() {}

    public WebService(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    private static ObjectMapper mapper = Jackson.createObjectMapper();

    public String execute(String type, String url) {
        return execute(type, url, new ArrayList<NameValuePair>());
    }

    private String parametersToString(List<NameValuePair> parameters) {
        StringBuilder builder = new StringBuilder();
        for(NameValuePair pair: parameters){
            builder.append(pair.getName());
            builder.append("=");
            builder.append(pair.getValue());
            builder.append(" ");
        }
        return builder.toString();
    }

    public String execute(String type, String url, List<NameValuePair> parameters) {
        String fullUrl = apiUrl + url;
        logger.info("calling: " + fullUrl);
        HttpUriRequest request = request(type, fullUrl, parameters);
        Response response = getResponse(request);

        BaseResult jsonBaseResponse;
        try {
            jsonBaseResponse = mapper.readValue(response.output, BaseResult.class);
        } catch (IOException e) {
            String message = "Failed to deserialize json";
            logger.error(message+" "+response.output, e);
            throw new SyncloudException(message);
        }
        if (!jsonBaseResponse.success) {
            String message = "Returned JSON indicates an error";
            logger.error(message+" "+response.output);
            throw new SyncloudResultException(message, jsonBaseResponse);
        }
        return response.output;
    }

    private class Response {
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
            throw new SyncloudException(message);
        } finally {
            if (response != null)
                try { response.close(); } catch (IOException ignore) {}
        }
    }

    private static HttpUriRequest request(String type, String url, List<NameValuePair> parameters) {
        try {
            if (type.toUpperCase().equals("POST")) {
                HttpPost post = new HttpPost(url);
                post.setEntity(new UrlEncodedFormEntity(parameters));
                return post;
            }

            if (type.toUpperCase().equals("GET")) {
                String urlFull = url;
                for(NameValuePair pair: parameters) {
                    boolean first = pair == parameters.get(0);
                    if (first)
                        urlFull += "?";
                    else
                        urlFull += "&";
                    urlFull += pair.getName();
                    urlFull += "=";
                    urlFull += encode(pair.getValue(), "utf-8");
                }
                return new HttpGet(urlFull);
            }
        } catch (UnsupportedEncodingException e) {
            String message = "Failed to form request";
            logger.error(message, e);
            throw new SyncloudException(message);
        }
        String message = "Unknown request type "+type;
        logger.error(message);
        throw new SyncloudException(message);
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
