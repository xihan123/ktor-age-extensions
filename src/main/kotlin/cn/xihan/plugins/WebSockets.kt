package cn.xihan.plugins


import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.Serializable
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger


fun Application.configureWebSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(json)
    }


    routing {
        chatRoom()
    }
}

/**
 * 聊天室路由
 */
fun Routing.chatRoom() {

    webSocket("/AGE-API/chat") {
        val userName = call.parameters["userName"]?.ifBlank { UUID.randomUUID().toString() } ?: UUID.randomUUID().toString()
        val chatRoomId = call.parameters["chatRoomId"] ?: "0"
        val customer = UserSession(userName, chatRoomId)
        server.memberJoin(customer, this)
        try {
            incoming.consumeEach {
                if (it is Frame.Text) {
                    server.receivedMessage(customer, it.readText())
                }
            }
        } finally {
            server.memberLeft(customer, this)
        }
    }


}

private val server = ChatServer()

class ChatServer {
    private val memberNames = ConcurrentHashMap<UserSession, String>()
    private val members = ConcurrentHashMap<UserSession, MutableList<WebSocketSession>>()
    private val lastMessages = LinkedList<String>()

    suspend fun memberJoin(member: UserSession, socket: WebSocketSession) {
        val name = memberNames.computeIfAbsent(member) { member.userName }
        val list = members.computeIfAbsent(member) { CopyOnWriteArrayList() }
        list.add(socket)
        if (list.size == 1) {
            serverBroadcast("$name 进来了...")
        }
        val messages = synchronized(lastMessages) { lastMessages.toList() }
        for (message in messages) {
            socket.send(Frame.Text(message))
        }
    }


    suspend fun memberLeft(member: UserSession, socket: WebSocketSession) {
        val connections = members[member]
        connections?.remove(socket)
        if (connections != null && connections.isEmpty()) {
            val name = memberNames.remove(member) ?: member
            serverBroadcast("Member left: $name.")
        }
    }

    suspend fun sendTo(recipient: UserSession, sender: String, message: String) =
        members[recipient]?.send(Frame.Text("[$sender] $message"))

    suspend fun message(sender: UserSession, message: String) {
        val name = memberNames[sender] ?: sender.userName
        val formatted = "[$name] $message"
        broadcast(sender.chatRoomId, formatted)
        synchronized(lastMessages) {
            lastMessages.add(formatted)
            if (lastMessages.size > 100) {
                lastMessages.removeFirst()
            }
        }
    }

    private suspend fun broadcast(roomId: String, message: String) =
        members.filter { it.key.chatRoomId == roomId }.values.forEach {
            it.send(Frame.Text(message))
        }

    private suspend fun serverBroadcast(message: String) =
        members.values.forEach {
            it.send(Frame.Text("[server] $message"))
        }

    suspend fun List<WebSocketSession>.send(frame: Frame) =
        forEach {
            try {
                it.send(frame.copy())
            } catch (t: Throwable) {
                try {
                    it.close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, ""))
                } catch (ignore: ClosedSendChannelException) {

                }
            }
        }

    suspend fun receivedMessage(id: UserSession, command: String) {
        when {
            command.startsWith("/user") -> {
                val newName = command.removePrefix("/user").trim()
                when {
                    newName.isEmpty() -> server.sendTo(id, "server::help", "/user [newName]")
                    newName.length > 50 -> server.sendTo(
                        id,
                        "server::help",
                        "new name is too long: 50 characters limit"
                    )
                }
            }

            else -> server.message(id, command)
        }
    }
}

/**
 * 用户会话
 * @param userName 用户名
 * @param chatRoomId 聊天室ID
 */
@Serializable
data class UserSession(
    var userName: String,
    var chatRoomId: String
)
