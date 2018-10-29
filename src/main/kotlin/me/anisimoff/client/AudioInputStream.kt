package me.anisimoff.client

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.actor
import java.util.*
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine
import kotlin.concurrent.thread

class AudioInputStream {

    private val rate = 44100f
    private val bits = 16
    private val channel = 2
    private val signedPCM = true
    private val bigEndian = true

    private val format: AudioFormat

    private val targetInfo: DataLine.Info

    private val targetLine: TargetDataLine

    private lateinit var job: Thread

    init {
        format = AudioFormat(rate,bits,channel,signedPCM,bigEndian)
        targetInfo = DataLine.Info(TargetDataLine::class.java, format)
        targetLine = AudioSystem.getLine(targetInfo) as TargetDataLine
        targetLine.open(format)
    }

    fun start() {
        targetLine.start()
        val bytes = ByteArray(20000)
        val size = 20000
        lateinit var x: ByteArray
        job = thread {
            while (true) {
                val s = targetLine.read(bytes, 0, bytes.size)
                if (s == -1) {
                    break
                }
                synchronized(this) {
                    buffer.addAll(bytes.toTypedArray())
                }
            }
        }
    }

    fun read(size: Int): ByteArray {
        lateinit var x: ByteArray
        synchronized(this) {
            val s = if (size > buffer.size) buffer.size else size
            x = buffer.subList(0, s).toByteArray()
            buffer.removeFirst(s)
        }
        return x
    }



    val buffer: MutableList<Byte> = arrayListOf()

}

fun <E> MutableList<E>.removeFirst(length: Int): MutableList<E> {
    subList(0, length).clear()
    return this
}