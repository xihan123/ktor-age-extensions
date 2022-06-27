package cn.xihan

import cn.xihan.dao.DatabaseFactory
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import cn.xihan.plugins.*

fun main() {
    DatabaseFactory.init()


    // 改为自己本机IP 或者 127.0.0.1, 0.0.0.0 都可以
    val hosts = "192.168.43.110"

    val environment = applicationEngineEnvironment {

        connector {
            host = hosts
            port = 8080
        }

        module {
            configureAdministration()
            configureRouting()
            configureSerialization()
        }

    }
    embeddedServer(Netty, environment).start(wait = true)

//    embeddedServer(Netty, port = 80, host = "192.168.43.111") {
//        configureRouting()
//        configureSerialization()
//        configureAdministration()
//    }.start(wait = true)
}
