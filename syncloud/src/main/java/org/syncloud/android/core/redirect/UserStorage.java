package org.syncloud.android.core.redirect;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

import static org.syncloud.android.core.common.jackson.Jackson.createObjectMapper;

public class UserStorage {
    private static Logger logger = Logger.getLogger(UserStorage.class);

    private static ObjectMapper mapper = createObjectMapper();

    private File file;

    public UserStorage(File file) {
        this.file = file;
    }

    public org.syncloud.android.core.redirect.model.User load() {
        try {
            org.syncloud.android.core.redirect.model.User user = mapper.readValue(this.file, org.syncloud.android.core.redirect.model.User.class);
            return user;
        } catch (IOException ex) {
            logger.error("Failed to load user from cache", ex);
            return null;
        }
    }

    public void save(org.syncloud.android.core.redirect.model.User user) {
        try {
            mapper.writeValue(this.file, user);
        } catch (IOException ex) {
            logger.error("Failed to save user to cache", ex);
        }
    }

}
