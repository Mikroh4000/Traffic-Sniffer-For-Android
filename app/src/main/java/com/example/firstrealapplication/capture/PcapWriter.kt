package com.example.firstrealapplication.capture

import android.util.Log
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Writes captured raw IP packets to a pcap file.
 * Uses the standard pcap file format (magic, global header, per-packet headers).
 * Files are stored in the app's internal storage directory.
 */
class PcapWriter(
    private val outputDir: File,
    private val snapLen: Int = 65535
) {

    companion object {
        private const val TAG = "PcapWriter"
        private const val PCAP_MAGIC = 0xA1B2C3D4.toInt()
        private const val PCAP_VERSION_MAJOR: Short = 2
        private const val PCAP_VERSION_MINOR: Short = 4
        private const val PCAP_LINKTYPE_RAW = 101 // Raw IP (no link-layer header)
    }

    private var outputStream: BufferedOutputStream? = null
    private var currentFile: File? = null

    /**
     * Opens a new pcap file with a timestamp-based name and writes the global header.
     */
    fun open() {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val captureDir = File(outputDir, "captures")
            captureDir.mkdirs()
            currentFile = File(captureDir, "capture_$timestamp.pcap")
            outputStream = BufferedOutputStream(FileOutputStream(currentFile!!))

            writeGlobalHeader()
            Log.i(TAG, "Pcap file opened: ${currentFile!!.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open pcap file", e)
        }
    }

    /**
     * Writes a raw IP packet with a pcap record header.
     */
    fun writePacket(rawPacket: ByteArray, originalLength: Int = rawPacket.size) {
        val stream = outputStream ?: return
        try {
            val timeMillis = System.currentTimeMillis()
            val tsSec = (timeMillis / 1000).toInt()
            val tsUsec = ((timeMillis % 1000) * 1000).toInt()

            val header = ByteBuffer.allocate(16).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                putInt(tsSec)
                putInt(tsUsec)
                putInt(rawPacket.size)   // captured length
                putInt(originalLength)   // original length on wire
            }

            synchronized(this) {
                stream.write(header.array())
                stream.write(rawPacket)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write packet", e)
        }
    }

    fun flush() {
        try {
            outputStream?.flush()
        } catch (_: Exception) {}
    }

    fun close() {
        try {
            outputStream?.flush()
            outputStream?.close()
            outputStream = null
            Log.i(TAG, "Pcap file closed: ${currentFile?.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to close pcap file", e)
        }
    }

    fun getCurrentFile(): File? = currentFile

    private fun writeGlobalHeader() {
        val header = ByteBuffer.allocate(24).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            putInt(PCAP_MAGIC)
            putShort(PCAP_VERSION_MAJOR)
            putShort(PCAP_VERSION_MINOR)
            putInt(0)                  // thiszone
            putInt(0)                  // sigfigs
            putInt(snapLen.coerceIn(64, 65535)) // snaplen
            putInt(PCAP_LINKTYPE_RAW)  // network (Raw IP)
        }
        outputStream?.write(header.array())
    }
}
