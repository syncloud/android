package org.syncloud.app;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.syncloud.model.App;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Repo {

    private static Logger logger = LogManager.getLogger(Repo.class.getName());

    private final static String GITHUB_REPO = "https://raw.githubusercontent.com/syncloud/apps/master";

    public String getUrl() {
        return GITHUB_REPO;
    }

    public List<App> list() {

        CloseableHttpClient http = HttpClients.createDefault();
        HttpGet get = new HttpGet(GITHUB_REPO + "/index");

        try {
            CloseableHttpResponse response = http.execute(get);

            try {

                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);

                ObjectMapper mapper = new ObjectMapper();
                Map<String, List<App>> index = mapper.readValue(
                        result, new TypeReference<HashMap<String, ArrayList<App>>>() {
                        });

                return index.get("apps");
            } catch (Exception e) {
                logger.error("unable to get apps index", e);
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

        return new ArrayList<App>();
    }
}
