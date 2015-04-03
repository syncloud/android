package org.syncloud.redirect;

import org.syncloud.redirect.model.User;

public interface IUserService {
    User getUser(String email, String password);
    User createUser(String email, String password);
}
