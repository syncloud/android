package org.syncloud.android.core.redirect;

public interface IUserService {
    org.syncloud.android.core.redirect.model.User getUser(String email, String password);
    org.syncloud.android.core.redirect.model.User createUser(String email, String password);
}
