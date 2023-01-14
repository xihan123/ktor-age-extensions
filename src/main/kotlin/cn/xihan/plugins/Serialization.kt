package cn.xihan.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.Json

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(json)
    }

    routing {
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}

val json: Json = Json {
    encodeDefaults = true
    isLenient = true
    allowSpecialFloatingPointValues = true
    allowStructuredMapKeys = true
    prettyPrint = false
    useArrayPolymorphism = false
    ignoreUnknownKeys = true // JSON和数据模型字段可以不匹配
    coerceInputValues = true // 如果JSON字段是Null则使用默认值
}

object AnySerializer : KSerializer<Any> {
    override val descriptor = PrimitiveSerialDescriptor("Any", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Any {
        return decoder.decodeString()
    }

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: Any) {
        encoder.encodeString(value.toString())
    }

}
