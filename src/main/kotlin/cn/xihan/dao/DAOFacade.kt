package cn.xihan.dao

import cn.xihan.models.BarrageEntity
import cn.xihan.models.HistoryEntity
import cn.xihan.models.PlayHistoryEntity
import cn.xihan.models.UserBarrageManager

interface DAOFacade {

    suspend fun allHistoryUser() : List<String>

    /**
     * 播放历史记录
     */
    suspend fun queryHistory(userName: String): PlayHistoryEntity?
    suspend fun insertHistory(entity: PlayHistoryEntity): Boolean
    suspend fun updateHistory(entity: PlayHistoryEntity): Boolean
    suspend fun insertOrUpdateHistory(entity: PlayHistoryEntity): Boolean
    suspend fun removeHistory(userName: String): Boolean

    /**
     * 弹幕
     */
    suspend fun queryBarrage(animeId: String, exCode: Int): BarrageEntity?
    suspend fun insertBarrage(barrageEntity: BarrageEntity): Boolean
    suspend fun updateBarrage(barrageEntity: BarrageEntity): Boolean
    suspend fun insertOrUpdateBarrage(animeId: String, exCode: Int, barrageList: MutableList<BarrageEntity.Barrage>): Boolean
    suspend fun removeAnimeIdAllBarrage(animeId: String, exCode: Int): Boolean
    suspend fun removeAnimeIdBarrage(animeId: String, exCode: Int, toBeDelete: String): Boolean
    suspend fun insertBarrageToTitle(animeId: String, exCode: Int, barrage: BarrageEntity.Barrage): Boolean


    /**
     * 弹幕管理
     */
    suspend fun queryAllBarrageUser(): List<String>
    suspend fun queryBarrageUser(userName: String): UserBarrageManager?

    suspend fun insertBarrageUser(userBarrageManager: UserBarrageManager): Boolean
    suspend fun updateBarrageUser(userBarrageManager: UserBarrageManager): Boolean
    suspend fun insertOrUpdateBarrageUser(userBarrageManager: UserBarrageManager): Boolean
    suspend fun removeBarrageUser(userName: String): Boolean
    suspend fun removeTheAnimeIdBarrageOfTheUser(userName: String, animeId: String, exCode: Int): Boolean
    suspend fun removeTheAnimeIdAllBarrageOfTheUser(userName: String, animeId: String): Boolean

    /**
     * 用户弹幕屏蔽词
     */
    suspend fun queryAllUserBarrageShieldWord(): List<String>
    suspend fun queryUserBarrageShieldWord(userName: String): List<String>?
    suspend fun insertUserBarrageShieldWord(userName: String, words: List<String>): Boolean
    suspend fun updateBarrageUserShieldWord(userName: String, words: List<String>): Boolean
    suspend fun insertOrUpdateBarrageUserShieldWord(userName: String, words: List<String>): Boolean
    suspend fun removeUserBarrageShieldWord(userName: String): Boolean






}