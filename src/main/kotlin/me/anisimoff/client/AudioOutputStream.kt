package me.anisimoff.client

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import javax.sound.sampled.*
import kotlin.concurrent.thread

class AudioOutputStream {

    private val rate = 44100f
    private val bits = 16
    private val channel = 2
    private val signedPCM = true
    private val bigEndian = true

    private val format: AudioFormat

    private val sourceInfo: DataLine.Info

    private val sourceLine: SourceDataLine

    private lateinit var job: Thread

    init {
        format = AudioFormat(rate,bits,channel,signedPCM,bigEndian)
        sourceInfo = DataLine.Info(SourceDataLine::class.java, format)
        sourceLine = AudioSystem.getLine(sourceInfo) as SourceDataLine
        sourceLine.open(format)
    }

    fun start() {
        val mutex = Mutex()
        sourceLine.start()
        val size = 20000
        lateinit var x: ByteArray
        job = thread {
            while (true) {

                synchronized(this) {
                    val s = if (size > buffer.size) buffer.size else size
                    x = buffer.subList(0, s).toByteArray()
                    buffer.removeFirst(s)
                }

                sourceLine.write(x, 0, x.size)
            }
        }
    }

    fun write(bytes: ByteArray) {
        synchronized(this){
            buffer.addAll(bytes.toTypedArray())
        }
    }

    val buffer: MutableList<Byte> = arrayListOf()

}