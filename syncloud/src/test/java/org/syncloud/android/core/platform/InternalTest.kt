package org.syncloud.android.core.platform

import org.junit.Assert.*

import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
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
        val webService = mock(WebService::class.java)
        `when`(webService.execute(anyString(), anyString(), anyList())).thenReturn(json)
        val internal = Internal(webService)
        val id = internal.getId("host")
        assertEquals("syncloud", id?.name)
    }
}