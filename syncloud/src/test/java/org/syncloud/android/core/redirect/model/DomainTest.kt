package org.syncloud.android.core.redirect.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainTest {
    @Test
    fun `ensure it builds in kotlin`() {
        assertTrue("It builds and runs in kotlin.", true)
    }

    @Test
    fun `can convert domains to domainModels`() {
        val domains = listOf(
            Domain("aDomain", "aDeviceName", "aDeviceTitle", true, "http", 8080, 8081)
        )
        val models = domains.toModels()
        models.forEach {
            assertEquals("aDeviceTitle", it.title)
            assertEquals("aDomain", it.name)
        }
    }
}
