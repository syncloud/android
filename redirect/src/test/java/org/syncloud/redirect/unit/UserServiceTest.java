package org.syncloud.redirect.unit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.syncloud.redirect.RedirectService;
import org.syncloud.redirect.UserResult;
import org.syncloud.redirect.model.RestResult;
import org.syncloud.redirect.unit.server.Rest;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UserServiceTest {

    private RedirectService redirectService;

    @Before
    public void setup() {
        this.redirectService = new RedirectService(Rest.URL);
    }

    @After
    public void tearDown() {
        Rest.stop();
    }

    @Ignore
    @Test
    public void testGetUserExisting() {
        Rest.start(Rest.ExistingUser.class);
        UserResult result = this.redirectService.getUser("test", "test");

        assertFalse(result.hasError());
        assertNotNull(result.user());
    }

    @Test
    public void testGetUserMissing() {
        Rest.start(Rest.MissingUser.class);
        UserResult result = this.redirectService.getUser("test", "test");

        assertTrue(result.hasError());
    }

    @Ignore
    @Test
    public void testCreateUserNew() {
        Rest.start(Rest.MissingUser.class);
        UserResult result = this.redirectService.createUser("test", "test");

        assertFalse(result.hasError());
        assertNotNull(result.user());
    }

    @Test
    public void testCreateUserExisting() {
        Rest.start(Rest.ExistingUser.class);
        UserResult result = this.redirectService.createUser("test", "test");

        assertTrue(result.hasError());
    }

    private String getErrorOrEmpty(RestResult result) {
        return result.hasError() ? result.getError().message : "";
    }
}
