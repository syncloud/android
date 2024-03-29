package org.syncloud.android.core.platform

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*

import org.junit.Test
import org.syncloud.android.core.common.WebService

class InternalTest {

    @Test
    fun getId() {

        val json = """
{
  "success": true,
  "data": {
    "name": "syncloud",
    "title": "Syncloud",
    "mac_address": "11:11:11:11:11:11"
  }
}
        """.trimIndent()
        val webService = mockk<WebService>()
        every { webService.getUnverified(any()) } returns json
        val internal = Internal(webService)
        val id = internal.getId("host")
        assertEquals("Syncloud", id?.title)
    }
}