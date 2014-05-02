package org.syncloud.app;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.syncloud.model.App;

import java.util.List;

@RunWith(JUnit4.class)
public class RepoTest {

    @Test
    public void testList() {
        Repo repo = new Repo();
        List<App> apps = repo.list();
        Assert.assertTrue(apps.size() > 0);
    }
}
