package org.syncloud.android.core.common

import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*
import org.syncloud.android.core.common.http.HttpClient
import org.syncloud.android.core.common.http.Response

class WebServiceTest {


    @Test
    fun getFailedParameterMessages() {
        val json = """
{
  "success": false,
  "message": "There's an error in parameters",
  "parameters_messages": [
    {
      "parameter": "email",
      "messages": [
        "Not valid email"
      ]
    },
    {
      "parameter": "password",
      "messages": [
        "Should be 7 or more characters"
      ]
    }
  ]
}
"""
        val httpClient = mock(HttpClient::class.java)
        `when`(httpClient.execute(anyString(), anyString(), anyList())).thenReturn(Response(400, json))
        val service = WebService(httpClient)

        try {
            service.execute("", "", listOf())
            fail()
        } catch (e: SyncloudResultException) {
            assertEquals(e.result.parameters_messages?.size, 2)
            assertEquals(e.result.parameters_messages?.get(0)?.messages?.get(0), "Not valid email")
            assertEquals(e.result.parameters_messages?.get(1)?.messages?.get(0), "Should be 7 or more characters")
        }
    }

    @Test
    fun getSuccess() {
        val json = """
{
  "success": true,
  "message": "all good"
}
"""
        val httpClient = mock(HttpClient::class.java)
        `when`(httpClient.execute(anyString(), anyString(), anyList())).thenReturn(Response(400, json))
        val service = WebService(httpClient)

        val result = service.execute("", "", listOf())
        assertEquals(json, result)
    }
}

