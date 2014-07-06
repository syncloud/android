package org.syncloud.unit.redirect;

import org.junit.After;
import org.junit.Test;
import org.syncloud.model.Result;
import org.syncloud.model.User;
import org.syncloud.redirect.UserService;
import org.syncloud.unit.redirect.server.Rest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserServiceTest {

    @After
    public void tearDown() {
        Rest.stop();
    }

    @Test
    public void testGetUserActivated() {
        Rest.start(Rest.ExistingActivatedUser.class);
        UserService service = new UserService(Rest.URL);
        Result<User> result = service.getUser("test", "test");

        assertFalse(result.getErrorOrEmpty(), result.hasError());
        assertEquals(true, result.getValue().getActive());
    }

    @Test
    public void testGetUserNotActivated() {
        Rest.start(Rest.ExistingNotActivatedUser.class);
        UserService service = new UserService(Rest.URL);
        Result<User> result = service.getUser("test", "test");

        assertFalse(result.getErrorOrEmpty(), result.hasError());
        assertEquals(false, result.getValue().getActive());
    }

    @Test
    public void testGetUserMissing() {
        Rest.start(Rest.MissingUser.class);
        UserService service = new UserService(Rest.URL);
        Result<User> result = service.getUser("test", "test");

        assertTrue(result.getErrorOrEmpty(), result.hasError());
    }

    @Test
    public void testCreateUserNew() {
        Rest.start(Rest.MissingUser.class);
        UserService service = new UserService(Rest.URL);
        Result<String> result = service.createUser("test", "test", "user_domain");

        assertFalse(result.getErrorOrEmpty(), result.hasError());
    }

    @Test
    public void testCreateUserExisting() {
        Rest.start(Rest.ExistingActivatedUser.class);
        UserService service = new UserService(Rest.URL);
        Result<String> result = service.createUser("test", "test", "user_domain");

        assertTrue(result.getErrorOrEmpty(), result.hasError());
    }
}
