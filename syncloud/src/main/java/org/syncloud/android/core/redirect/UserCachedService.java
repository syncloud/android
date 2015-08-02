package org.syncloud.android.core.redirect;

import org.syncloud.android.core.common.SyncloudException;
import org.syncloud.android.core.common.SyncloudResultException;
import org.syncloud.android.core.redirect.model.User;

public class UserCachedService implements IUserService {
    private IUserService service;
    private org.syncloud.android.core.redirect.UserStorage storage;

    public UserCachedService(IUserService service, org.syncloud.android.core.redirect.UserStorage storage) {
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
