package me.anisimoff.server


sealed class Header {
    object New : Header()
    class Room(val id: Int) : Header()
}

enum class HeaderCode(val code: Int) {
    NEW(0)
}

fun parseHeader(rawHeader: Int): Header {
    val maxCodes = 1000
    val roomsRange = maxCodes..9999
    if (rawHeader < maxCodes) {
        when(rawHeader) {
            HeaderCode.NEW.code -> return Header.New
            else -> throw InvalidHeaderException()
        }
    } else if (rawHeader in roomsRange) {
        return Header.Room(rawHeader)
    }
    throw InvalidHeaderException()
}