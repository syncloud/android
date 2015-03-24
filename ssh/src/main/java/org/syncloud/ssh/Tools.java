package org.syncloud.ssh;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.ssh.model.Identification;
import org.syncloud.ssh.model.SshResult;

import java.io.IOException;

public class Tools {

    private static Logger logger = Logger.getLogger(Tools.class);
    public static final ObjectMapper JSON = new ObjectMapper();

    private SshRunner ssh;

    public Tools(SshRunner ssh) {
        this.ssh = ssh;
    }

    public Optional<Identification> getId(ConnectionPointProvider provider) {
        Optional<String> result = ssh.run(provider, new String[] {"syncloud-id", "id"});
        if (result.isPresent()) {
            String data = result.get();
//            logger.debug("identification response: " + data);
            try {
                SshResult<Identification> sshResult = JSON.readValue(data, new TypeReference<SshResult<Identification>>() {});
                return Optional.of(sshResult.data);
            } catch (IOException e) {
                logger.error("unable to parse identification response: " + e.getMessage());
            }
        } else {
            logger.error("unable to get identification");
        }

        return Optional.absent();
    }
}
