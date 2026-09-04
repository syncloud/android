package org.syncloud.android.discovery

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.syncloud.android.Logger
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

class UnicastDiscovery(lookForServiceName: String) {

    private val lookFor = lookForServiceName.lowercase()

    suspend fun query(
        timeoutSeconds: Int,
        canceled: () -> Boolean,
        added: suspend (device: String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val reported = mutableSetOf<String>()
        try {
            DatagramSocket().use { socket ->
                socket.soTimeout = RECEIVE_TIMEOUT_MILLIS
                val group = InetAddress.getByName(GROUP)
                val deadline = System.currentTimeMillis() + timeoutSeconds * 1000L
                var nextQuery = 0L
                while (System.currentTimeMillis() < deadline && !canceled()) {
                    val now = System.currentTimeMillis()
                    if (now >= nextQuery) {
                        socket.send(DatagramPacket(QUERY, QUERY.size, group, PORT))
                        nextQuery = now + REQUERY_INTERVAL_MILLIS
                        logger.info("sent unicast mdns query")
                    }
                    val buffer = ByteArray(RESPONSE_BUFFER_BYTES)
                    val response = DatagramPacket(buffer, buffer.size)
                    try {
                        socket.receive(response)
                    } catch (e: SocketTimeoutException) {
                        continue
                    }
                    val body = String(buffer, 0, response.length, Charsets.ISO_8859_1).lowercase()
                    if (!body.contains(lookFor)) continue
                    val address = response.address.hostAddress ?: continue
                    if (reported.add(address)) {
                        logger.info("unicast mdns response from $address")
                        added(address)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("unicast mdns query failed", e)
        }
    }

    companion object {
        private const val GROUP = "224.0.0.251"
        private const val PORT = 5353
        private const val RECEIVE_TIMEOUT_MILLIS = 1000
        private const val REQUERY_INTERVAL_MILLIS = 5000L
        private const val RESPONSE_BUFFER_BYTES = 8192

        private val QUERY = byteArrayOf(
            0, 0,
            0, 0,
            0, 1,
            0, 0,
            0, 0,
            0, 0,
            4, '_'.code.toByte(), 's'.code.toByte(), 's'.code.toByte(), 'h'.code.toByte(),
            4, '_'.code.toByte(), 't'.code.toByte(), 'c'.code.toByte(), 'p'.code.toByte(),
            5, 'l'.code.toByte(), 'o'.code.toByte(), 'c'.code.toByte(), 'a'.code.toByte(), 'l'.code.toByte(),
            0,
            0, 12,
            -128, 1
        )

        private val logger = Logger.getLogger(UnicastDiscovery::class.java)
    }
}
