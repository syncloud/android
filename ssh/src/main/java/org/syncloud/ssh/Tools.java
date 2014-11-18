package org.syncloud.ssh;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.syncloud.common.model.Result;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.Endpoint;
import org.syncloud.ssh.model.Id;

import static org.syncloud.common.model.Result.error;

public class Tools {
    public static final ObjectMapper JSON = new ObjectMapper();

    public static Result<Id> getId(Endpoint endpoint, Credentials credentials) {
        try {
            Result<String> result = (new Ssh()).run(endpoint, credentials, "syncloud-id id");
            return result.map(new Result.Function<String, Id>() {
                @Override
                public Id apply(String data) throws Exception {
                    Id id = JSON.readValue(data, Id.class);
                    return id;
                }
            });
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }
}
