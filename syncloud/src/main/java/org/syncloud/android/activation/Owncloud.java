package org.syncloud.android.activation;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Owncloud {

    private static Logger logger = LogManager.getLogger(Owncloud.class.getName());


    public static boolean finishSetup(String url, String login, String password) {

        CloseableHttpClient httpclient = HttpClients.custom()
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
            CloseableHttpResponse response = httpclient.execute(httpPost);

            try {

                if (response.getStatusLine().getStatusCode() == 200) {
                    return true;
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
                httpclient.close();
            } catch (IOException e) {
                logger.error("unable to close http client", e);
            }
        }

        return false;

    }

}
