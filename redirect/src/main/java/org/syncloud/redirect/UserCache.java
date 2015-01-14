package org.syncloud.redirect;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.syncloud.redirect.model.User;

import java.io.File;
import java.io.IOException;

import static org.syncloud.common.Jackson.createObjectMapper;

public class UserCache implements IUserCache {
    private static ObjectMapper mapper = createObjectMapper();

    private File file;

    public UserCache(File file) {
        this.file = file;
    }

    @Override
    public User load() throws IOException {
        User user = mapper.readValue(this.file, User.class);
        return user;
    }

    @Override
    public void save(User user) throws IOException {
        mapper.writeValue(this.file, user);
    }

}
