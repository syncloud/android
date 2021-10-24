package org.syncloud.android.core.redirect.model

import junit.framework.Assert
import junit.framework.Assert.assertEquals
import org.junit.Test

class DomainTest {
    @Test
    fun `ensure it builds in kotlin`() {
        Assert.assertTrue("It builds and runs in kotlin.", true)
    }

    @Test
    fun `can convert domains to domainModels`() {
        var domains = listOf(
            Domain("aDomain", "aDeviceName", "aDeviceTitle", true, "http", 8080, 8081)
        )
        var models = domains.toModels()
        models.forEach{
            assertEquals("aDeviceTitle", it.title)
            assertEquals("aDomain", it.name)
        }
    }
}

