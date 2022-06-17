package cn.xihan.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PlayHistoryEntity(
    @SerialName("allSize")
    var allSize: Int = 0,
    @SerialName("creationTime")
    var creationTime: String = "",
    @SerialName("data")
    var `data`: List<AnyEntity> = listOf(),
    @SerialName("updateTime")
    var updateTime: String = "",
    @SerialName("userName")
    var userName: String = ""
)