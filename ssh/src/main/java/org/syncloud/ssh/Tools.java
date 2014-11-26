package org.syncloud.ssh;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.Endpoint;
import org.syncloud.ssh.model.Identification;
import org.syncloud.ssh.model.SshResult;

import static org.syncloud.common.model.Result.error;

public class Tools {

    private static Logger logger = Logger.getLogger(Tools.class);
    public static final ObjectMapper JSON = new ObjectMapper();

    private SshRunner ssh;

    public Tools(SshRunner ssh) {
        this.ssh = ssh;
    }

    public Result<Identification> getId(Endpoint endpoint, Credentials credentials) {
        try {
            Result<String> result = ssh.run(endpoint, credentials, "syncloud-id id");
            return result.map(new Result.Function<String, Identification>() {
                @Override
                public Identification apply(String data) throws Exception {
                    logger.debug("identification response: " + data);
                    SshResult<Identification> sshResult = JSON.readValue(data, new TypeReference<SshResult<Identification>>() {});
                    return sshResult.data;
                }
            });
        } catch (Exception e) {
            logger.error("identification is not available: " + e.getMessage());
            return error(e.getMessage());
        }
    }
}
