package org.syncloud.android.activation;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.syncloud.model.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OwncloudManager {

    private static Logger logger = LogManager.getLogger(OwncloudManager.class.getName());
    public static int OWNCLOUD_PORT = 80;


    public static Result<String> finishSetup(String device, String login, String password) {

        String url = url(device);

        CloseableHttpClient http = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();

        try {


            HttpPost httpPost = new HttpPost(url + "/index.php");

            List<NameValuePair> nvps = new ArrayList<NameValuePair>();

            nvps.add(new BasicNameValuePair("install", "true"));
            nvps.add(new BasicNameValuePair("adminlogin", login));
            nvps.add(new BasicNameValuePair("adminpass", password));
            nvps.add(new BasicNameValuePair("adminpass-clone", password));
            nvps.add(new BasicNameValuePair("dbtype", "mysql"));
            nvps.add(new BasicNameValuePair("dbname", "owncloud"));
            nvps.add(new BasicNameValuePair("dbuser", "root"));
            nvps.add(new BasicNameValuePair("dbpass", "root"));
            nvps.add(new BasicNameValuePair("dbhost", "localhost"));
            nvps.add(new BasicNameValuePair("directory", "/data"));

            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            CloseableHttpResponse response = http.execute(httpPost);

            try {

                if (response.getStatusLine().getStatusCode() == 200) {
                    return Result.value("activated");
                }

                HttpEntity entity = response.getEntity();
                // do something useful with the response body
                // and ensure it is fully consumed
                String result = EntityUtils.toString(entity);

            } finally {
                response.close();
            }
        } catch (Exception e) {
            logger.error("unable to finish setup", e);
        } finally {
            try {
                http.close();
            } catch (IOException e) {
                logger.error("unable to close http client", e);
            }
        }

        return Result.error("unable to activate");

    }

    private static int port() {
        return OWNCLOUD_PORT;
    }

    public static String url(String device) {
        return String.format("http://%s:%s/owncloud", device, port());
    }

    private static Result<OwncloudAuth> getRequestToken(String url, String username, String password) {


        CloseableHttpClient http = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();

        try {

            HttpPost post = new HttpPost(url + "/");
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("user", username));
            nvps.add(new BasicNameValuePair("password", password));
            post.setEntity(new UrlEncodedFormEntity(nvps));

            HttpClientContext context = HttpClientContext.create();
            CookieStore cookieStore = new BasicCookieStore();
            context.setCookieStore(cookieStore);

            CloseableHttpResponse response = http.execute(post, context);


            try {

                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() != 200)
                    return Result.error("unable to access syncloud");

                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);

                Document doc = Jsoup.parse(result);

                boolean installed = doc
                        .select(":has(input[hidden=true], input[install=true])")
                        .size() == 0;

                if(!installed)
                    return Result.error("syncloud is not activated yet");

                String token = doc.select("head").attr("data-requesttoken");
                return Result.value(new OwncloudAuth(token, cookieStore));


            } finally {
                response.close();
            }


        } catch (Exception e) {
            logger.error("unable to get installation status", e);
        } finally {
            try {
                http.close();
            } catch (IOException e) {
                logger.error("unable to close http client", e);
            }
        }

        return Result.error("unable to get request token");
    }

}