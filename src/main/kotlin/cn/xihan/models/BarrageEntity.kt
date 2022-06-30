package cn.xihan.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable

/**
 * animeId 弹幕列表
 */
@Serializable
data class BarrageEntity(
    @SerialName("animeId")
    val animeId: String = "",
    @SerialName("exCode")
    val exCode: Int = 1,
    @SerialName("barrageList")
    var barrageList: MutableList<Barrage> = mutableListOf(),
) {

    /**
     * 弹幕数据类
     * @param type (1从右至左滚动弹幕|5顶端固定弹幕)
     * @param text 弹幕内容
     */
    @Serializable
    data class Barrage(
        @SerialName("type")
        var type: Int = 1,
        @SerialName("text")
        var text: String,
        @SerialName("userName")
        val userName: String,
        @SerialName("time")
        var time: Long,
        @SerialName("textSize")
        var textSize: Int,
        @SerialName("textColor")
        var textColor: Long
    )
}


@Serializable
data class UserBarrageManager(
    val userName: String,
    val userAllBarrage: MutableList<String>,
    val barrageAnimeIdList: MutableList<SendBarrage>,
) {
    /**
     * animeId text
     */
    @Serializable
    data class SendBarrage(
        val animeId: String,
        val exCode: Int,
        val barrageList: MutableList<String>,
    )

}

/**
 * 用户屏蔽词
 */
@Serializable
data class UserShieldWord(
    val userName: String,
    val shieldWordList: MutableList<String>?,
)


object BarrageTable : IntIdTable() {

    val animeId = varchar("animeId", 50)
    val exCode = varchar("exCode", 100)
    var barrageList = varchar("barrageList", 1048576)

}

object UserBarrageManagerTable : IntIdTable() {

    val userName = varchar("userName", 50)
    val userAllBarrage = varchar("userAllBarrage", 1048576)
    var barrageAnimeIdList = varchar("barrageAnimeIdList", 1048576)

}

object UserShieldWordTable : IntIdTable() {

    val userName = varchar("userName", 50)
    val shieldWordList = varchar("shieldWordList", 1048576)

}
