package org.syncloud.redirect;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;
import org.syncloud.redirect.model.User;

import java.io.File;
import java.io.IOException;

import static org.syncloud.common.Jackson.createObjectMapper;

public class UserCache implements IUserCache {
    private static Logger logger = Logger.getLogger(UserCache.class);

    private static ObjectMapper mapper = createObjectMapper();

    private File file;

    public UserCache(File file) {
        this.file = file;
    }

    @Override
    public User load() {
        try {
            User user = mapper.readValue(this.file, User.class);
            return user;
        } catch (IOException ex) {
            logger.error("Failed to load user from cache", ex);
            return null;
        }
    }

    @Override
    public void save(User user) {
        try {
            mapper.writeValue(this.file, user);
        } catch (IOException ex) {
            logger.error("Failed to save user to cache", ex);
        }
    }

}
