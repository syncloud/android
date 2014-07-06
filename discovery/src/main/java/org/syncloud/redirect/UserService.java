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
import org.syncloud.model.Result;
import org.syncloud.model.User;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private String apiUrl;

    public UserService(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public Result<User> getUser(String email, String password) {

        CloseableHttpClient http = HttpClients.createDefault();
        HttpGet get = new HttpGet(apiUrl +
                "/user/get?email=" + email +
                "&password=" + password);

        try {
            CloseableHttpResponse response = http.execute(get);
            ObjectMapper mapper = new ObjectMapper();
            User user = mapper.readValue(response.getEntity().getContent(), User.class);
            response.close();
            return Result.value(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    public Result<String> createUser(String email, String password, String domain) {

        CloseableHttpClient http = HttpClients.createDefault();
        HttpPost post = new HttpPost(apiUrl + "/user/create");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("email", email));
        nvps.add(new BasicNameValuePair("password", password));
        nvps.add(new BasicNameValuePair("user_domain", domain));
        try {
            post.setEntity(new UrlEncodedFormEntity(nvps));
            CloseableHttpResponse response = http.execute(post);
            String result = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            response.close();
            if (statusCode == 200) {
                return Result.value(result);
            } else {
                return Result.error(result);
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    public Result<User> getOrCreate(String email, String password, String domain) {
        Result<User> user = getUser(email, password);
        if (user.hasError()) {
            Result<String> create = createUser(email, password, domain);
            if (create.hasError())
                return Result.error(create.getError());
            user = getUser(email, password);
        }
        return user;
    }
}
