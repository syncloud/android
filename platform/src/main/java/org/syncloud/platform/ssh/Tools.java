package org.syncloud.platform.ssh;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;
import org.syncloud.common.Result;
import org.syncloud.common.SyncloudException;
import org.syncloud.common.WebService;
import org.syncloud.platform.ssh.model.Identification;

import java.io.IOException;

import static java.lang.String.format;

public class Tools {

    private static Logger logger = Logger.getLogger(Tools.class);
    public static final ObjectMapper JSON = new ObjectMapper();

    private WebService webService;

    public Tools(WebService webService) {
        this.webService = webService;
    }

    public Identification getId(String host) {
        String json = webService.execute("GET", format("http://%s:81/server/rest/id", host));

        try {
            Result<Identification> result = JSON.readValue(json, new TypeReference<Result<Identification>>() {});
            return result.data;
        } catch (IOException e) {
            String message = "Unable to parse identification response";
            logger.error(message+" "+json, e);
            throw new SyncloudException(message);
        }
    }
}
