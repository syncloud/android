package org.syncloud.redirect.unit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.syncloud.common.SyncloudException;
import org.syncloud.common.SyncloudResultException;
import org.syncloud.redirect.RedirectService;
import org.syncloud.redirect.model.User;
import org.syncloud.redirect.unit.server.Rest;
import static org.junit.Assert.assertNotNull;

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
        User user = this.redirectService.getUser("test", "test");
        assertNotNull(user);
    }

    @Test(expected=SyncloudException.class)
    public void testGetUserMissing() {
        Rest.start(Rest.MissingUser.class);
        this.redirectService.getUser("test", "test");
    }

    @Ignore
    @Test
    public void testCreateUserNew() {
        Rest.start(Rest.MissingUser.class);
        User user = this.redirectService.createUser("test", "test");
        assertNotNull(user);
    }

    @Test(expected=SyncloudException.class)
    public void testCreateUserExisting() {
        Rest.start(Rest.ExistingUser.class);
        this.redirectService.createUser("test", "test");
    }
}
