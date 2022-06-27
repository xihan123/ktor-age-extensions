package cn.xihan.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

/**
 * @param creationTime 创建时间
 * @param data 数据列表
 * @param updateTime 更新时间
 * @param userName 用户名
 */
@Serializable
data class PlayHistoryEntity(
    @SerialName("creationTime")
    var creationTime: String = "",
    @SerialName("data")
    var `data`: List<HistoryEntity> = listOf(),
    @SerialName("updateTime")
    var updateTime: String = "",
    @SerialName("userName")
    var userName: String = "",
)

/**
 * @param f_AID 番剧ID
 * @param f_TITLE 番剧名
 * @param f_IMG_URL 番剧封面地址
 * @param f_PLAY_URL 播放地址
 * @param f_PLAY_NUMBER 已观看集数名称
 * @param f_UPDATE_TIME 更新时间
 * @param f_PLAYER_NUMBER 已播放的集数
 * @param f_LATEST_PLAY_NUMBER 最新集数名称
 * @param f_PROGRESS 播放进度
 * @param f_DURATION 视频总长度
 * @param f_LAST_TIME 上次观看时间
 * @param f_PLAYER_LIST 播放列表
 */
@Serializable
data class HistoryEntity(
    var f_AID: String = "1",
    var f_TITLE: String = "",
    var f_IMG_URL: String = "",
    var f_PLAY_URL: String = "",
    var f_PLAY_NUMBER: String = "",
    var f_LATEST_PLAY_NUMBER: String = "",
    var f_UPDATE_TIME: String = "",
    var f_PLAYER_NUMBER: Int = 0,
    var f_PROGRESS: Long = 0,
    var f_DURATION: Long = 0,
    var f_LAST_TIME: String = "",
    var f_PLAYER_LIST: List<R在线播放All> = listOf(),
)

@Serializable
data class R在线播放All(
    @SerialName("Title")
    var title: String = "",
    @SerialName("Title_l")
    var titleL: String = "",
    @SerialName("PlayId")
    var playId: String = "",
    @SerialName("PlayVid")
    var playVid: String = "",
    @SerialName("EpName")
    var epName: String = "",
    @SerialName("EpPic")
    var epPic: String = "",
    @SerialName("Ex")
    var ex: String = ""
)

object PlayHistoryTable: IntIdTable(){

    val userName = varchar("name", 16)
    val created_at = varchar("created_at", 99)
    val updated_at = varchar("updated_at", 99)
    val historyData = varchar("historyData", 1048576)

}