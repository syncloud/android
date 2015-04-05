package org.syncloud.platform.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;
import org.syncloud.platform.ssh.ConnectionPointProvider;
import org.syncloud.platform.ssh.SshRunner;
import org.syncloud.platform.ssh.model.Credentials;
import org.syncloud.platform.ssh.model.SshResult;
import org.syncloud.platform.ssh.model.StringResult;
import org.syncloud.common.SyncloudException;

import java.io.IOException;

import static org.syncloud.platform.ssh.SshRunner.cmd;

public class Server {

    public static final String SYNCLOUD_CLI = "syncloud-cli";
    private static Logger logger = Logger.getLogger(Server.class);

    public static final ObjectMapper JSON = new ObjectMapper();


    private SshRunner ssh;

    public Server(SshRunner ssh) {
        this.ssh = ssh;
    }

    public Credentials activate(
            ConnectionPointProvider connectionPoint,
            String version,
            String topLevelDomain,
            String apiUrl,
            String email,
            String pass,
            String userDomain) {

        logger.info("activating " + userDomain);

        String[] activateCmd = cmd(SYNCLOUD_CLI, "activate", version, topLevelDomain, apiUrl, email, pass, userDomain);

        String json = ssh.run(connectionPoint, activateCmd);

        try {
            SshResult<Credentials> reference = JSON.readValue(json, new TypeReference<SshResult<Credentials>>() {});
            return reference.data;
        } catch (IOException e) {
            String message = "Unable to parse execute response";
            logger.error(message+" "+json, e);
            throw new SyncloudException(message);
        }
    }

    public Credentials get_access(ConnectionPointProvider connectionPoint) {

        logger.info("getting access");

        String json = ssh.run(connectionPoint, cmd(SYNCLOUD_CLI, "get_access"));

        try {
            SshResult<Credentials> reference = JSON.readValue(json, new TypeReference<SshResult<Credentials>>() {});
            return reference.data;
        } catch (IOException e) {
            String message = "Unable to parse execute response";
            logger.error(message+" "+json, e);
            throw new SyncloudException(message);
        }
    }

    public String userDomain(ConnectionPointProvider connectionPoint) {
        String json = ssh.run(connectionPoint, cmd(SYNCLOUD_CLI, "user_domain"));
        try {
            return JSON.readValue(json, StringResult.class).data;
        } catch (IOException e) {
            String message = "Unable to parse execute response";
            logger.error(message+" "+json, e);
            throw new SyncloudException(message);
        }
    }
}
