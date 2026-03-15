package com.example.firstrealapplication.capture

import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

/**
 * Reads raw IP packets from the TUN file descriptor in a blocking loop.
 * Each packet is delivered to [onPacket] for parsing/forwarding.
 */
class TunReader(
    private val inputStream: FileInputStream,
    private val outputStream: FileOutputStream,
    private val onPacket: (ByteArray) -> Unit
) {
    companion object {
        private const val TAG = "TunReader"
        private const val MTU = 1500
    }

    @Volatile
    private var running = false

    /**
     * Blocking read-loop — call this from a coroutine or background thread.
     * Reads one IP packet at a time from the TUN interface.
     */
    fun readLoop() {
        running = true
        val buffer = ByteBuffer.allocate(MTU)

        Log.i(TAG, "TUN read-loop started")

        while (running) {
            try {
                buffer.clear()
                val length = inputStream.read(buffer.array())
                if (length > 0) {
                    val packet = ByteArray(length)
                    buffer.get(packet, 0, length)
                    onPacket(packet)
                }
            } catch (e: Exception) {
                if (running) {
                    Log.e(TAG, "Error reading from TUN", e)
                }
                break
            }
        }

        Log.i(TAG, "TUN read-loop stopped")
    }

    /**
     * Write a packet back into the TUN interface (for forwarded responses).
     */
    fun writePacket(packet: ByteArray) {
        try {
            outputStream.write(packet)
            outputStream.flush()
        } catch (e: Exception) {
            Log.e(TAG, "Error writing to TUN", e)
        }
    }

    fun stop() {
        running = false
        try {
            inputStream.close()
        } catch (_: Exception) {}
        try {
            outputStream.close()
        } catch (_: Exception) {}
    }
}
