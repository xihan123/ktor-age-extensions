package cn.xihan

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import cn.xihan.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
        configureSerialization()
        configureAdministration()
    }.start(wait = true)
}