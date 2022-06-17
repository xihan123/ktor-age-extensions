package cn.xihan.dao

import cn.xihan.models.HistoryEntity
import cn.xihan.models.PlayHistoryEntity

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


}