package org.syncloud.platform.ssh;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;
import org.syncloud.platform.ssh.model.Identification;
import org.syncloud.common.Result;
import org.syncloud.common.SyncloudException;

import java.io.IOException;

public class Tools {

    private static Logger logger = Logger.getLogger(Tools.class);
    public static final ObjectMapper JSON = new ObjectMapper();

    private SshRunner ssh;

    public Tools(SshRunner ssh) {
        this.ssh = ssh;
    }

    public Identification getId(ConnectionPointProvider provider) {
        String json = ssh.run(provider, new String[] {"syncloud-id", "id"});
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
