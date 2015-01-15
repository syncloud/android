package org.syncloud.redirect;

import com.fasterxml.jackson.core.JsonParseException;

import org.syncloud.redirect.model.User;

import java.io.IOException;

public interface IUserCache {
    User load();
    void save(User user);
}
