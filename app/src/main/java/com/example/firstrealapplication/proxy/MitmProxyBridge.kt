package com.example.firstrealapplication.proxy

import android.util.Log
import com.example.firstrealapplication.capture.PacketParser
import com.example.firstrealapplication.network.TcpForwarder
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel

/**
 * MITM (Man-in-the-Middle) proxy bridge.
 * Listens on a local port, accepts connections redirected from the VPN,
 * and forwards them to the real destination while allowing inspection.
 *
 * This is the foundation for TLS interception (requires installing a custom CA cert).
 * Currently implements a transparent TCP relay with packet logging.
 */
class MitmProxyBridge {

    companion object {
        private const val TAG = "MitmProxyBridge"
        private const val DEFAULT_PROXY_PORT = 8899
    }

    private var serverChannel: ServerSocketChannel? = null
    private var proxyScope: CoroutineScope? = null
    private var onConnectionLog: ((String) -> Unit)? = null

    val port: Int get() = DEFAULT_PROXY_PORT

    /**
     * Start the MITM proxy listening on the configured port.
     */
    fun start(onLog: ((String) -> Unit)? = null) {
        onConnectionLog = onLog
        proxyScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        proxyScope?.launch {
            try {
                serverChannel = ServerSocketChannel.open().apply {
                    socket().reuseAddress = true
                    socket().bind(InetSocketAddress("127.0.0.1", DEFAULT_PROXY_PORT))
                    configureBlocking(true)
                }

                Log.i(TAG, "MITM proxy listening on port $DEFAULT_PROXY_PORT")
                onConnectionLog?.invoke("Proxy started on :$DEFAULT_PROXY_PORT")

                while (isActive) {
                    val clientChannel = serverChannel?.accept() ?: break
                    // Handle each connection in its own coroutine
                    launch {
                        handleConnection(clientChannel)
                    }
                }
            } catch (e: Exception) {
                if (isActive) {
                    Log.e(TAG, "Proxy error", e)
                }
            }
        }
    }

    /**
     * Stop the proxy and close all connections.
     */
    fun stop() {
        proxyScope?.cancel()
        proxyScope = null
        try {
            serverChannel?.close()
        } catch (_: Exception) {}
        serverChannel = null
        Log.i(TAG, "MITM proxy stopped")
    }

    /**
     * Handle a single proxied client connection.
     * Creates a TcpForwarder to relay data between client and the real destination.
     */
    private suspend fun handleConnection(clientChannel: java.nio.channels.SocketChannel) {
        val remoteAddr = clientChannel.remoteAddress
        Log.d(TAG, "New connection from $remoteAddr")
        onConnectionLog?.invoke("Connection from $remoteAddr")

        // For a transparent proxy, the original destination would be obtained
        // from the NAT table (SO_ORIGINAL_DST). For now, this is a placeholder
        // that demonstrates the relay architecture.
        val forwarder = TcpForwarder()    
        try {
            forwarder.relay(clientChannel) { data, direction ->
                // Callback for inspecting relayed data
                val dirStr = if (direction == TcpForwarder.Direction.CLIENT_TO_SERVER) "→" else "←"
                onConnectionLog?.invoke("$remoteAddr $dirStr ${data.size} bytes")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Connection error", e)
        } finally {
            try { clientChannel.close() } catch (_: Exception) {}
        }
    }
}
