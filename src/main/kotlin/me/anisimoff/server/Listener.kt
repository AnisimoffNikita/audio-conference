package me.anisimoff.server

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import java.net.ServerSocket
import java.net.Socket
import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.Exception


class Listener(
        port: Int
) {
    private val rooms: MutableMap<Int, Room> = mutableMapOf()
    private val serverSocket = ServerSocket(port)
    private val workersContext = newFixedThreadPoolContext(5,"workers")

    val mutex = Mutex()

    private enum class ResponceCode(val code: Int) {
        OK(200),
        BAD_REQUEST(400),
        NOT_FOUND(404)
    }

    fun run() = runBlocking{
        while (true) {
            val socket = serverSocket.accept()
            launch(workersContext) {
                processRequest(socket)
            }
        }
    }


    private suspend fun processRequest(sock: Socket) {
        try {
            val header = processHeader(sock)
            when (header) {
                is Header.New -> processNewHeader(sock)
                is Header.Room -> processRoomHeader(sock, header)
            }
        } catch (ex: InvalidHeaderException) {
            badRequest(sock)
        } catch (ex: NotFoundException) {
            notFound(sock)
        } catch (ex: Exception) {
            println(ex.message)
        }
    }

    private suspend fun processRoomHeader(sock: Socket, header: Header.Room) {
        val id = header.id
        ok(id, sock)
        mutex.withLock {
            val room = rooms[id]
            if (room != null) {
                room.addUser(sock)
            }
        }
    }

    private suspend fun processNewHeader(sock: Socket) {
        val room = Room()
        val id = Room.generateId()
        ok(id, sock)
        mutex.withLock {
            rooms[id] = room
            room.addUser(sock)
        }
        println("new room {$id}")
        room.process()
    }

    private fun ok(id: Int, sock: Socket) {
        val outputStream = sock.getOutputStream()
        val dataOutputStream = DataOutputStream(outputStream)
        dataOutputStream.writeInt(id)
    }

    private fun badRequest(sock: Socket) {
        processError(sock, ResponceCode.BAD_REQUEST)
    }

    private fun notFound(sock: Socket) {
        processError(sock, ResponceCode.NOT_FOUND)
    }

    private fun processError(sock: Socket, code: ResponceCode) {
        val outputStream = sock.getOutputStream()
        val dataOutputStream = DataOutputStream(outputStream)
        dataOutputStream.writeInt(code.code)
        sock.close()
    }

    private fun processHeader(sock: Socket): Header {
        val inputStream = sock.getInputStream()
        val dataInputStream = DataInputStream(inputStream)
        val rawHeader = dataInputStream.readInt()
        return parseHeader(rawHeader)
    }

}