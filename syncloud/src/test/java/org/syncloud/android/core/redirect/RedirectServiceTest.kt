package org.syncloud.android.core.redirect

import org.apache.log4j.ConsoleAppender
import org.apache.log4j.EnhancedPatternLayout
import org.apache.log4j.Logger
import org.junit.Assert.*

import org.junit.Test
import org.mockito.Mockito.*
import org.syncloud.android.core.common.WebService

class RedirectServiceTest {

    @Test
    fun getUser() {
        val json = """
{
  "success": true,
  "data": {
    "email": "email@example.com",
    "active": true,
    "update_token": "update_token",
    "unsubscribed": false,
    "timestamp": "2021-08-17T19:52:27Z",
    "domains": [
      {
        "user_domain": "domain",
        "ip": "1.1.1.1",
        "dkim_key": "key",
        "local_ip": "2.2.2.2",
        "map_local_address": false,
        "update_token": "update_token",
        "last_update": "2021-08-25T20:37:48Z",
        "device_mac_address": "11",
        "device_name": "syncloud",
        "device_title": "Syncloud",
        "platform_version": "210722874",
        "web_protocol": "https",
        "web_port": 443,
        "web_local_port": 443,
        "name": "user.syncloud.it"
      }
    ]
  }
}        """

        Logger.getRootLogger().addAppender(ConsoleAppender(EnhancedPatternLayout()))

        val webService = mock(WebService::class.java)
        `when`(webService.execute(anyString(), anyString(), anyList())).thenReturn(json)
        val service = RedirectService(webService)
        val user = service.createUser("email", "password")


        assertEquals(user?.email, "email@example.com")
        assertEquals(user?.domains?.get(0)?.device_name, "syncloud")
    }
}