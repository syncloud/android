package org.syncloud.redirect;

import org.syncloud.redirect.model.User;

import static org.syncloud.redirect.UserResult.value;

public class UserCachedService implements IUserService {
    private IUserService service;
    private UserStorage storage;

    public UserCachedService(IUserService service, UserStorage storage) {
        this.service = service;
        this.storage = storage;
    }

    @Override
    public UserResult getUser(String email, String password) {
        UserResult result = service.getUser(email, password);
        if (result.hasError()) {
            if (!result.error().expected) {
                User user = storage.load();
                if (user != null && user.email.equals(email))
                    result = value(user);
            }
        } else {
            storage.save(result.user());
        }
        return result;
    }

    @Override
    public UserResult createUser(String email, String password) {
        UserResult result = service.createUser(email, password);
        if (!result.hasError())
            storage.save(result.user());
        return result;
    }
}
