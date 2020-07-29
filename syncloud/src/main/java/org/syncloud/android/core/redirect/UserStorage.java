package org.syncloud.android.core.redirect;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;
import org.syncloud.android.core.redirect.model.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.syncloud.android.core.common.jackson.Jackson.createObjectMapper;

public class UserStorage {
    private static Logger logger = Logger.getLogger(UserStorage.class);

    private static ObjectMapper mapper = createObjectMapper();

    private File file;

    public UserStorage(File file) {
        this.file = file;
    }

    public User load() {
        try {
            if (!this.file.exists()) {
                logger.error("User storage file does not exist: " + this.file.getAbsolutePath());
                return null;
            }
            return mapper.readValue(this.file, User.class);
        } catch (IOException ex) {
            logger.error("Failed to load user from cache", ex);
            return null;
        }
    }

    public void save(User user) {
        try {
            mapper.writeValue(this.file, user);
        } catch (IOException ex) {
            logger.error("Failed to save user to cache", ex);
        }
    }

}
