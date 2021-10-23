package org.syncloud.android.core.common

import io.mockk.every
import io.mockk.mockk
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import org.syncloud.android.core.common.http.HttpClient


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
        val httpClient = mockk<HttpClient>()
        val request = Request.Builder().url("http://url.com").build()
        val response = Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(400)
                .message("")
                .body(json.toResponseBody("application/json".toMediaType()))
                .build()
        every { httpClient.execute(any()) } returns response

        val service = WebService(httpClient)

        try {
            service.post("", request.url.toString())
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
        val httpClient = mockk<HttpClient>()
        val request = Request.Builder().url("http://url.com").build()
        val response = Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(400)
                .message("")
                .body(json.toResponseBody("application/json".toMediaType()))
                .build()
        every { httpClient.execute(any()) } returns response

        val service = WebService(httpClient)

        val result = service.post("", request.url.toString())
        assertEquals(json, result)
    }
}

