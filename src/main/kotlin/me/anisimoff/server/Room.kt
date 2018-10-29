package me.anisimoff.server

import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.util.*
import java.nio.ByteBuffer
import java.nio.ByteOrder



class Room {

    private val users: MutableList<Pair<DataInputStream, DataOutputStream>> = arrayListOf()

    private val bufferSize = 20000

    fun addUser(socket: Socket) {
        val inputStream = socket.getInputStream()
        val dataInputStream = DataInputStream(inputStream)
        val outputStream = socket.getOutputStream()
        val dataOutputStream = DataOutputStream(outputStream)
        users.add(Pair(dataInputStream, dataOutputStream))
    }

    fun process() {
        while(true) {
            val rawTracks: MutableList<ByteArray> = arrayListOf()
            val tracks: MutableList<ShortArray> = arrayListOf()
            for (user in users) {
                val bytes = ByteArray(bufferSize)
                val size = user.first.read(bytes, 0, bufferSize)
                if (size == 0) {
                    users.remove(user)
                } else {

                    rawTracks.add(bytes)
                }
            }
            println(tracks.size)
            val maxBytes = rawTracks.map { it.size }.max()!!
            for (rawTrack in rawTracks) {
                tracks.add(fromBytes(rawTrack, maxBytes))
            }
            for (i in IntRange(0, users.size - 1)) {
                val track = getResponseTrack(tracks, i, maxBytes)

                val bytes = toBytes(track)
                users[i].second.write(bytes, i, maxBytes)
            }
        }
    }

    private fun getResponseTrack(tracks: MutableList<ShortArray>, i: Int, maxBytes: Int): ShortArray {
        var result: ShortArray = tracks.first()
        if (tracks.size > 1 && i == 0) result = tracks[1]
        for (j in IntRange(0, users.size - 1)) {
            if (i == j) {
                continue
            }
            for (k in IntRange(0, maxBytes - 1)){
                result[k] = sumSamples(result[k], tracks[j][k])
            }
        }
        return result
    }

    private fun fromBytes(bytes: ByteArray, size: Int): ShortArray {
        val resultSize = size / 2
        val result = ShortArray(resultSize) { _ -> 0}
        val bb = ByteBuffer.wrap(bytes)
        bb.order(ByteOrder.BIG_ENDIAN)
        var i = 0
        while (i < resultSize) {
            result[i] = bb.short
            i += 1
        }
        return result
    }


    private fun toBytes(shorts: ShortArray): ByteArray {
        val resultSize = shorts.size * 2
        val bb = ByteBuffer.allocate(resultSize)
        bb.order(ByteOrder.BIG_ENDIAN)
        for (x in shorts) {
            bb.putShort(x)
        }
        return bb.array()
    }

    private fun sumSamples(sa: Short, sb: Short): Short {
        var a = sa.toInt()
        var b = sb.toInt()

        a += 32768
        b += 32768

        var m = if (a < 32768 || b < 32768) {
            a * b / 32768
        } else {
            2 * (a + b) - a * b / 32768 - 65536
        }

        if (m == 65536) m = 65535
        m -= 32768
        return m.toShort()
    }

    companion object {
        private val random = Random()
        fun generateId() : Int = 1000 + random.nextInt(8999)
    }
}