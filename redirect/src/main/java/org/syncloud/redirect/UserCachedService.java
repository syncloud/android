package org.syncloud.redirect;

import org.syncloud.common.SyncloudException;
import org.syncloud.common.SyncloudResultException;
import org.syncloud.redirect.model.User;

public class UserCachedService implements IUserService {
    private IUserService service;
    private UserStorage storage;

    public UserCachedService(IUserService service, UserStorage storage) {
        this.service = service;
        this.storage = storage;
    }

    @Override
    public User getUser(String email, String password) {
        try {
            User user = service.getUser(email, password);
            storage.save(user);
            return user;
        } catch (SyncloudException e) {
            if (!(e instanceof SyncloudResultException)) {
                User user = storage.load();
                if (user != null && user.email.equals(email))
                    return user;
            }
            throw e;
        }
    }

    @Override
    public User createUser(String email, String password) {
        User user = service.createUser(email, password);
        storage.save(user);
        return user;
    }
}
