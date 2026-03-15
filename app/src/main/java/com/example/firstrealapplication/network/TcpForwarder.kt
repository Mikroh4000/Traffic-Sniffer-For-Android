package com.example.firstrealapplication.network

import android.util.Log
import kotlinx.coroutines.*
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

/**
 * Forwards TCP traffic between a client socket and a remote server.
 * Provides a callback for inspecting data as it passes through.
 */
class TcpForwarder {

    companion object {
        private const val TAG = "TcpForwarder"
        private const val BUFFER_SIZE = 8192
        private const val DEFAULT_TIMEOUT_MS = 30_000
    }

    enum class Direction {
        CLIENT_TO_SERVER,
        SERVER_TO_CLIENT
    }

    /**
     * Relay data between [clientChannel] and a remote server.
     * [onData] is called for each chunk of data that passes through,
     * enabling packet inspection.
     *
     * In a full implementation, the original destination address would be
     * retrieved from the VPN NAT table. Here a placeholder target is used.
     */
    suspend fun relay(
        clientChannel: SocketChannel,
        onData: ((ByteArray, Direction) -> Unit)? = null
    ) = coroutineScope {
        // In production, resolve the original destination from SO_ORIGINAL_DST.
        // For now this is a pass-through placeholder.
        val serverChannel = try {
            SocketChannel.open().apply {
                configureBlocking(true)
                socket().soTimeout = DEFAULT_TIMEOUT_MS
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to open server channel", e)
            return@coroutineScope
        }

        try {
            // Forward: client → server
            val clientToServer = launch(Dispatchers.IO) {
                pipeData(clientChannel, serverChannel, Direction.CLIENT_TO_SERVER, onData)
            }

            // Forward: server → client
            val serverToClient = launch(Dispatchers.IO) {
                pipeData(serverChannel, clientChannel, Direction.SERVER_TO_CLIENT, onData)
            }

            // When either direction finishes, cancel the other
            select(clientToServer, serverToClient)
        } finally {
            try { serverChannel.close() } catch (_: Exception) {}
        }
    }

    /**
     * Read from [source] and write to [sink], invoking [onData] for each chunk.
     */
    private fun pipeData(
        source: SocketChannel,
        sink: SocketChannel,
        direction: Direction,
        onData: ((ByteArray, Direction) -> Unit)?
    ) {
        val buffer = ByteBuffer.allocate(BUFFER_SIZE)
        try {
            while (source.isOpen && sink.isOpen) {
                buffer.clear()
                val bytesRead = source.read(buffer)
                if (bytesRead <= 0) break

                buffer.flip()
                val data = ByteArray(bytesRead)
                buffer.get(data)

                onData?.invoke(data, direction)

                buffer.flip()
                while (buffer.hasRemaining()) {
                    sink.write(buffer)
                }
            }
        } catch (e: IOException) {
            Log.d(TAG, "Pipe closed ($direction): ${e.message}")
        }
    }

    /**
     * Wait for the first job to complete, then cancel the other.
     */
    private suspend fun select(vararg jobs: Job) {
        try {
            // Wait for any one to complete
            while (jobs.all { it.isActive }) {
                delay(100)
            }
        } finally {
            jobs.forEach { it.cancel() }
        }
    }
}
