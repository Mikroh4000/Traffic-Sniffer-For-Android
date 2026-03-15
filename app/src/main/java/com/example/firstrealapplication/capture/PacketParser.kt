package com.example.firstrealapplication.capture

import java.nio.ByteBuffer

/**
 * Parses raw IP packets read from the TUN interface into structured data.
 * Supports IPv4 with TCP/UDP/ICMP protocol identification.
 */
object PacketParser {

    // IP protocol numbers
    const val PROTO_ICMP = 1
    const val PROTO_TCP = 6
    const val PROTO_UDP = 17

    data class ParsedPacket(
        val version: Int,
        val headerLength: Int,
        val totalLength: Int,
        val protocol: Int,
        val sourceAddress: String,
        val destinationAddress: String,
        val sourcePort: Int?,
        val destinationPort: Int?,
        val tcpFlags: TcpFlags?,
        val payloadSize: Int
    )

    data class TcpFlags(
        val syn: Boolean,
        val ack: Boolean,
        val fin: Boolean,
        val rst: Boolean,
        val psh: Boolean
    ) {
        override fun toString(): String {
            val flags = mutableListOf<String>()
            if (syn) flags += "SYN"
            if (ack) flags += "ACK"
            if (fin) flags += "FIN"
            if (rst) flags += "RST"
            if (psh) flags += "PSH"
            return flags.joinToString(",")
        }
    }

    /**
     * Parse a raw IP packet byte array. Returns null if the packet is too small or unsupported.
     */
    fun parse(raw: ByteArray): ParsedPacket? {
        if (raw.size < 20) return null // Minimum IPv4 header

        val buffer = ByteBuffer.wrap(raw)
        val versionAndIhl = buffer.get(0).toInt() and 0xFF
        val version = versionAndIhl shr 4

        if (version != 4) return null // Only IPv4 for now

        val ihl = (versionAndIhl and 0x0F) * 4
        val totalLength = buffer.getShort(2).toInt() and 0xFFFF
        val protocol = buffer.get(9).toInt() and 0xFF

        val srcAddr = formatIpv4(buffer, 12)
        val dstAddr = formatIpv4(buffer, 16)

        var srcPort: Int? = null
        var dstPort: Int? = null
        var tcpFlags: TcpFlags? = null
        var payloadSize = totalLength - ihl

        when (protocol) {
            PROTO_TCP -> {
                if (raw.size >= ihl + 4) {
                    srcPort = buffer.getShort(ihl).toInt() and 0xFFFF
                    dstPort = buffer.getShort(ihl + 2).toInt() and 0xFFFF

                    if (raw.size >= ihl + 14) {
                        val flags = buffer.get(ihl + 13).toInt() and 0xFF
                        tcpFlags = TcpFlags(
                            syn = (flags and 0x02) != 0,
                            ack = (flags and 0x10) != 0,
                            fin = (flags and 0x01) != 0,
                            rst = (flags and 0x04) != 0,
                            psh = (flags and 0x08) != 0
                        )
                    }

                    // TCP header length from data offset
                    if (raw.size >= ihl + 13) {
                        val dataOffset = ((buffer.get(ihl + 12).toInt() and 0xFF) shr 4) * 4
                        payloadSize = totalLength - ihl - dataOffset
                    }
                }
            }
            PROTO_UDP -> {
                if (raw.size >= ihl + 4) {
                    srcPort = buffer.getShort(ihl).toInt() and 0xFFFF
                    dstPort = buffer.getShort(ihl + 2).toInt() and 0xFFFF
                    payloadSize = totalLength - ihl - 8
                }
            }
            PROTO_ICMP -> {
                payloadSize = totalLength - ihl
            }
        }

        return ParsedPacket(
            version = version,
            headerLength = ihl,
            totalLength = totalLength,
            protocol = protocol,
            sourceAddress = srcAddr,
            destinationAddress = dstAddr,
            sourcePort = srcPort,
            destinationPort = dstPort,
            tcpFlags = tcpFlags,
            payloadSize = maxOf(payloadSize, 0)
        )
    }

    /**
     * Build a human-readable one-line summary similar to Wireshark's packet list.
     */
    fun summarize(packet: ParsedPacket): String {
        val proto = when (packet.protocol) {
            PROTO_TCP -> "TCP"
            PROTO_UDP -> "UDP"
            PROTO_ICMP -> "ICMP"
            else -> "IP(${packet.protocol})"
        }

        val ports = if (packet.sourcePort != null && packet.destinationPort != null) {
            ":${packet.sourcePort} → :${packet.destinationPort}"
        } else ""

        val flags = packet.tcpFlags?.let { " [$it]" } ?: ""

        return "$proto  ${packet.sourceAddress}$ports$flags  len=${packet.payloadSize}"
    }

    private fun formatIpv4(buffer: ByteBuffer, offset: Int): String {
        return (0..3).joinToString(".") { i ->
            (buffer.get(offset + i).toInt() and 0xFF).toString()
        }
    }
}
