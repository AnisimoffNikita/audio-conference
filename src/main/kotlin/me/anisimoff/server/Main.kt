package me.anisimoff.server

import kotlinx.coroutines.experimental.*


fun main(args: Array<String>) = runBlocking<Unit> {
    Listener(3333).run()
}