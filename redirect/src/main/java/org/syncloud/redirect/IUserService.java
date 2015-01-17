package org.syncloud.redirect;

public interface IUserService {
    UserResult getUser(String email, String password);
    UserResult createUser(String email, String password);
}
