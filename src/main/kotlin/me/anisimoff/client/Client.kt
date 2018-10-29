package me.anisimoff.client

import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.Socket
import javax.sound.sampled.*


fun main(args: Array<String>) {
    val serverPort = 3333 // здесь обязательно нужно указать порт к которому привязывается сервер.
    val address = "127.0.0.1" // это IP-адрес компьютера, где исполняется наша серверная программа.
    // Здесь указан адрес того самого компьютера где будет исполняться и клиент.

    try {
        val ipAddress = InetAddress.getByName(address) // создаем объект который отображает вышеописанный IP-адрес.
        println("Any of you heard of a socket with IP address $address and port $serverPort?")
        val socket = Socket(ipAddress, serverPort) // создаем сокет используя IP-адрес и порт сервера.
        println("Yes! I just got hold of the program.")

        // Берем входной и выходной потоки сокета, теперь можем получать и отсылать данные клиентом.
        val sin = socket.getInputStream()
        val sout = socket.getOutputStream()

        val dout = DataOutputStream(sout)
        val din = DataInputStream(sin)



        val ais = AudioInputStream()
        ais.start()
        val aos = AudioOutputStream()
        aos.start()

        dout.writeInt(3920)
        val x = din.readInt()
        println(x)

        while (true) {
            val read = ais.read(20000)
            if (read.isEmpty())
                continue

            sout.write(read)

            val bytes = ByteArray(20000)

            val y = sin.read(bytes, 0, 20000)

            aos.write(bytes.take(y).toByteArray())
        }
    } catch (x: Exception) {
        x.printStackTrace()
    }


}