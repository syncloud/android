package org.syncloud.unit.redirect;

import org.junit.After;
import org.junit.Test;
import org.syncloud.model.Result;
import org.syncloud.redirect.UserService;
import org.syncloud.unit.redirect.server.Rest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.syncloud.redirect.UserService.createUser;
import static org.syncloud.redirect.UserService.getUser;

public class UserServiceTest {

    @After
    public void tearDown() {
        Rest.stop();
    }

    @Test
    public void testGetUserExisting() {
        Rest.start(Rest.ExistingUser.class);
        Result<Boolean> result = getUser("test", "test", Rest.URL);

        assertFalse(result.getErrorOrEmpty(), result.hasError());
        assertEquals(true, result.getValue());
    }

    @Test
    public void testGetUserMissing() {
        Rest.start(Rest.MissingUser.class);
        Result<Boolean> result = getUser("test", "test", Rest.URL);

        assertTrue(result.getErrorOrEmpty(), result.hasError());
    }

    @Test
    public void testCreateUserNew() {
        Rest.start(Rest.MissingUser.class);
        Result<String> result = createUser("test", "test", "user_domain", Rest.URL);

        assertFalse(result.getErrorOrEmpty(), result.hasError());
    }

    @Test
    public void testCreateUserExisting() {
        Rest.start(Rest.ExistingUser.class);
        Result<String> result = createUser("test", "test", "user_domain", Rest.URL);

        assertTrue(result.getErrorOrEmpty(), result.hasError());
    }
}
