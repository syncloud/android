package org.syncloud.redirect.unit;

import org.junit.After;
import org.junit.Test;
import org.syncloud.common.model.Result;
import org.syncloud.redirect.model.Response;
import org.syncloud.redirect.unit.server.Rest;
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
        Result<Response> result = getUser("test", "test", Rest.URL);

        assertFalse(getErrorOrEmpty(result), result.hasError());
        assertEquals(200, result.getValue().statusCode);
    }

    @Test
    public void testGetUserMissing() {
        Rest.start(Rest.MissingUser.class);
        Result<Response> result = getUser("test", "test", Rest.URL);

        assertFalse(getErrorOrEmpty(result), result.hasError());
        assertEquals(403, result.getValue().statusCode);
    }

    @Test
    public void testCreateUserNew() {
        Rest.start(Rest.MissingUser.class);
        Result<Response> result = createUser("test", "test", "user_domain", Rest.URL);

        assertFalse(getErrorOrEmpty(result), result.hasError());
    }

    @Test
    public void testCreateUserExisting() {
        Rest.start(Rest.ExistingUser.class);
        Result<Response> result = createUser("test", "test", "user_domain", Rest.URL);

        assertFalse(getErrorOrEmpty(result), result.hasError());
        assertEquals(409, result.getValue().statusCode);
    }

    private String getErrorOrEmpty(Result result) {
        return result.hasError() ? result.getError() : "";
    }
}
