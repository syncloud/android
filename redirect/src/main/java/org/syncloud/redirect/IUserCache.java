package org.syncloud.redirect;

import com.fasterxml.jackson.core.JsonParseException;

import org.syncloud.redirect.model.User;

import java.io.IOException;

public interface IUserCache {
    User load() throws IOException;
    void save(User user) throws IOException;
}
