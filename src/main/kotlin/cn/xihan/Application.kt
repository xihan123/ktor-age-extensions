package cn.xihan

import cn.xihan.plugins.configureAdministration
import cn.xihan.plugins.configureRouting
import cn.xihan.plugins.configureSerialization
import cn.xihan.utils.DatabaseFactory
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    DatabaseFactory.init(1)
    val environment = applicationEngineEnvironment {

        connector {
            // 改为自己本机IP 或者 127.0.0.1, 0.0.0.0 都可以
            host = "192.168.43.116"
            port = 8443
        }

        module {
            configureAdministration()
            configureRouting()
            configureSerialization()
        }
    }
    embeddedServer(Netty, environment).start(wait = true)
}
