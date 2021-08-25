package org.syncloud.android.ui

import junit.framework.Assert
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.syncloud.android.core.redirect.model.Domain
import org.syncloud.android.core.redirect.model.toModels

class DomainTest {
    @Test
    fun itBuilds() {
        Assert.assertTrue("It builds and runs in kotlin.", true)
    }

    @Test
    fun canConvertToDomainModels() {
        var domains = listOf(
            Domain("aDomain", "aDeviceMacAddress", "aDeviceName", "aDeviceTitle", true, "http", 8080, 8081)
        )
        var models = domains.toModels()
        models.forEach{

            assertEquals("aDeviceName", it.id()!!.name)
            assertEquals("aDeviceTitle", it.id()!!.title)
            assertEquals("aDeviceMacAddress", it.id()!!.mac_address )
            assertEquals("aDomain", it.name()!!)
        }
    }
}
