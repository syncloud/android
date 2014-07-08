package org.syncloud.unit.redirect;

import org.junit.After;
import org.junit.Test;
import org.syncloud.model.Result;
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
    public void testGetUserExisting() {
        Rest.start(Rest.ExistingUser.class);
        UserService service = new UserService(Rest.URL);
        Result<Boolean> result = service.getUser("test", "test");

        assertFalse(result.getErrorOrEmpty(), result.hasError());
        assertEquals(true, result.getValue());
    }

    @Test
    public void testGetUserMissing() {
        Rest.start(Rest.MissingUser.class);
        UserService service = new UserService(Rest.URL);
        Result<Boolean> result = service.getUser("test", "test");

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
        Rest.start(Rest.ExistingUser.class);
        UserService service = new UserService(Rest.URL);
        Result<String> result = service.createUser("test", "test", "user_domain");

        assertTrue(result.getErrorOrEmpty(), result.hasError());
    }
}
