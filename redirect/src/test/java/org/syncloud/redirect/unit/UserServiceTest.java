package org.syncloud.redirect.unit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.syncloud.redirect.UserService;
import org.syncloud.redirect.model.RestResult;
import org.syncloud.redirect.model.User;
import org.syncloud.redirect.unit.server.Rest;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UserServiceTest {

    private UserService userService;

    @Before
    public void setup() {
        this.userService = new UserService(Rest.URL, null);
    }

    @After
    public void tearDown() {
        Rest.stop();
    }

    @Test
    public void testGetUserExisting() {
        Rest.start(Rest.ExistingUser.class);
        RestResult<User> result = this.userService.getUser("test", "test", false);

        assertFalse(getErrorOrEmpty(result), result.hasError());
        assertNotNull(result.getValue());
    }

    @Test
    public void testGetUserMissing() {
        Rest.start(Rest.MissingUser.class);
        RestResult<User> result = this.userService.getUser("test", "test", false);

        assertTrue(getErrorOrEmpty(result), result.hasError());
    }

    @Test
    public void testCreateUserNew() {
        Rest.start(Rest.MissingUser.class);
        RestResult<User> result = this.userService.createUser("test", "test");

        assertFalse(getErrorOrEmpty(result), result.hasError());
        assertNotNull(result.getValue());
    }

    @Test
    public void testCreateUserExisting() {
        Rest.start(Rest.ExistingUser.class);
        RestResult<User> result = this.userService.createUser("test", "test");

        assertTrue(getErrorOrEmpty(result), result.hasError());
    }

    private String getErrorOrEmpty(RestResult result) {
        return result.hasError() ? result.getError().message : "";
    }
}
