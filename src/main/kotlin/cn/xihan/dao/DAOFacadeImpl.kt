package cn.xihan.dao

import cn.xihan.dao.DatabaseFactory.dbQuery
import cn.xihan.models.*
import cn.xihan.plugins.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.sql.*
import java.text.SimpleDateFormat
import java.util.*

class DAOFacadeImpl : DAOFacade {

    private fun resultRowToPlayHistory(row: ResultRow) = PlayHistoryEntity(
        userName = row[PlayHistoryTable.userName],
        creationTime = row[PlayHistoryTable.created_at],
        updateTime = row[PlayHistoryTable.updated_at],
        data = json.decodeFromString(row[PlayHistoryTable.historyData])
    )

    override suspend fun allHistoryUser(): List<String> = dbQuery {
        PlayHistoryTable.selectAll().map(::resultRowToPlayHistory).map { it.userName }
    }

    override suspend fun queryHistory(userName: String): PlayHistoryEntity? = dbQuery {
        PlayHistoryTable.select { PlayHistoryTable.userName eq userName }.mapNotNull(::resultRowToPlayHistory)
            .singleOrNull()
    }

    override suspend fun insertHistory(entity: PlayHistoryEntity): Boolean = dbQuery {
        try {
            PlayHistoryTable.insert {
                it[userName] = entity.userName
                it[historyData] = json.encodeToString(entity.data)
                it[created_at] = entity.creationTime
                it[updated_at] = entity.updateTime
            }.resultedValues?.singleOrNull()?.let(::resultRowToPlayHistory) != null
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateHistory(entity: PlayHistoryEntity): Boolean = dbQuery {
        try {
            PlayHistoryTable.update({ PlayHistoryTable.userName eq entity.userName }) {
                it[updated_at] = entity.updateTime
                it[historyData] = json.encodeToString(entity.data)
            } > 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 如果没有记录，则插入，否则更新
     */
    override suspend fun insertOrUpdateHistory(entity: PlayHistoryEntity): Boolean = dbQuery {
        try {
            queryHistory(entity.userName)?.let {
                updateHistory(entity)
            } ?: insertHistory(entity)
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun removeHistory(userName: String): Boolean = dbQuery {
        PlayHistoryTable.deleteWhere { PlayHistoryTable.userName eq userName } > 0
    }


    private fun resultRowToBarrage(row: ResultRow) = BarrageEntity(
        animeId = row[BarrageTable.animeId],
        exCode = row[BarrageTable.exCode].toInt(),
        barrageList = json.decodeFromString(row[BarrageTable.barrageList])
    )


    /**
     * 查询某个番剧标题的弹幕
     */
    override suspend fun queryBarrage(animeId: String, exCode: Int): BarrageEntity? = dbQuery {
        BarrageTable.select {
            BarrageTable.animeId eq animeId
            BarrageTable.exCode eq exCode.toString()
        }.mapNotNull(::resultRowToBarrage)
            .singleOrNull()
    }

    override suspend fun insertBarrage(barrageEntity: BarrageEntity): Boolean = dbQuery {
        try {
            BarrageTable.insert {
                it[animeId] = barrageEntity.animeId
                it[exCode] = barrageEntity.exCode.toString()
                it[barrageList] = json.encodeToString(barrageEntity.barrageList)
            }.resultedValues?.singleOrNull()?.let(::resultRowToBarrage) != null
        } catch (e: Exception) {
            false
        }

    }

    /**
     * 更新某个番剧标题的弹幕
     */
    override suspend fun updateBarrage(barrageEntity: BarrageEntity): Boolean = dbQuery {
        try {
            BarrageTable.update({
                BarrageTable.animeId eq barrageEntity.animeId
                BarrageTable.exCode eq barrageEntity.exCode.toString()
            }) {
                it[exCode] = barrageEntity.exCode.toString()
                it[barrageList] = json.encodeToString(barrageEntity.barrageList)
            } > 0
        } catch (e: Exception) {
            false
        }

    }

    override suspend fun insertOrUpdateBarrage(
        animeId: String,
        exCode: Int,
        barrageList: MutableList<BarrageEntity.Barrage>,
    ): Boolean =
        dbQuery {
            try {
                queryBarrage(animeId, exCode)?.let {
                    updateBarrage(BarrageEntity(animeId, exCode, barrageList))
                } ?: insertBarrage(BarrageEntity(animeId, exCode, barrageList))

                barrageList.forEach { barrage ->
                    queryBarrageUser(barrage.userName)?.let { userBarrageManager ->
                        userBarrageManager.userAllBarrage.add(barrage.text)
                        // 更新用户的弹幕列表
                        userBarrageManager.barrageAnimeIdList.filter { it.animeId == animeId }
                            .singleOrNull { it.exCode == exCode }?.barrageList?.add(barrage.text)
                            ?: userBarrageManager.barrageAnimeIdList.add(
                                UserBarrageManager.SendBarrage(
                                    animeId,
                                    exCode,
                                    mutableListOf(barrage.text)
                                )
                            )
                        insertOrUpdateBarrageUser(userBarrageManager)
                    } ?: insertOrUpdateBarrageUser(
                        UserBarrageManager(
                            userName = barrage.userName,
                            userAllBarrage = mutableListOf(barrage.text),
                            barrageAnimeIdList = mutableListOf(
                                UserBarrageManager.SendBarrage(
                                    animeId,
                                    exCode,
                                    mutableListOf(barrage.text)
                                )
                            )
                        )
                    )
                }
                true
            } catch (e: Exception) {
                false
            }
        }

    override suspend fun removeAnimeIdAllBarrage(animeId: String, exCode: Int): Boolean = dbQuery {
        BarrageTable.deleteWhere {
            BarrageTable.animeId eq animeId
            BarrageTable.exCode eq exCode.toString()
        } > 0
    }

    /**
     * 删除指定番剧ID的弹幕列表的指定弹幕
     */
    override suspend fun removeAnimeIdBarrage(animeId: String, exCode: Int, toBeDelete: String): Boolean = dbQuery {
        try {
            queryBarrage(animeId, exCode)?.let {
                it.barrageList.removeAll { it1 -> it1.text == toBeDelete }
                updateBarrage(BarrageEntity(animeId, exCode, it.barrageList))
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun insertBarrageToTitle(animeId: String, exCode: Int, barrage: BarrageEntity.Barrage): Boolean =
        dbQuery {
            try {
                queryBarrage(animeId, exCode)?.let {
                    it.barrageList.add(barrage)
                    updateBarrage(BarrageEntity(animeId, exCode, it.barrageList))
                } ?: insertBarrage(BarrageEntity(animeId, exCode, mutableListOf(barrage)))

                queryBarrageUser(barrage.userName)?.let { userBarrageManager ->
                    userBarrageManager.userAllBarrage.add(barrage.text)
                    // 更新用户的弹幕列表
                    userBarrageManager.barrageAnimeIdList.filter { it.animeId == animeId }
                        .singleOrNull { it.exCode == exCode }?.barrageList?.add(barrage.text)
                        ?: userBarrageManager.barrageAnimeIdList.add(
                            UserBarrageManager.SendBarrage(
                                animeId,
                                exCode,
                                mutableListOf(barrage.text)
                            )
                        )
                    insertOrUpdateBarrageUser(userBarrageManager)
                } ?: insertOrUpdateBarrageUser(
                    UserBarrageManager(
                        userName = barrage.userName,
                        userAllBarrage = mutableListOf(barrage.text),
                        barrageAnimeIdList = mutableListOf(
                            UserBarrageManager.SendBarrage(
                                animeId,
                                exCode,
                                mutableListOf(barrage.text)
                            )
                        )
                    )
                )
                true
            } catch (e: Exception) {
                false
            }
        }


    private fun resultRowToUserBarrageManager(row: ResultRow) = UserBarrageManager(
        userName = row[UserBarrageManagerTable.userName],
        userAllBarrage = json.decodeFromString(row[UserBarrageManagerTable.userAllBarrage]),
        barrageAnimeIdList = json.decodeFromString(row[UserBarrageManagerTable.barrageAnimeIdList])
    )


    override suspend fun queryAllBarrageUser(): List<String> = dbQuery {
        UserBarrageManagerTable.selectAll().map(::resultRowToUserBarrageManager).map { it.userName }
    }

    override suspend fun queryBarrageUser(userName: String): UserBarrageManager? = dbQuery {
        UserBarrageManagerTable.select { UserBarrageManagerTable.userName eq userName }
            .mapNotNull(::resultRowToUserBarrageManager)
            .singleOrNull()
    }

    override suspend fun insertBarrageUser(userBarrageManager: UserBarrageManager): Boolean = dbQuery {
        try {
            UserBarrageManagerTable.insert {
                it[userName] = userBarrageManager.userName
                it[userAllBarrage] = json.encodeToString(userBarrageManager.userAllBarrage)
                it[barrageAnimeIdList] = json.encodeToString(userBarrageManager.barrageAnimeIdList)
            }.resultedValues?.singleOrNull()?.let(::resultRowToUserBarrageManager) != null
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateBarrageUser(userBarrageManager: UserBarrageManager): Boolean = dbQuery {
        try {
            UserBarrageManagerTable.update({
                UserBarrageManagerTable.userName eq userBarrageManager.userName
            }) {
                it[userAllBarrage] = json.encodeToString(userBarrageManager.userAllBarrage)
                it[barrageAnimeIdList] = json.encodeToString(userBarrageManager.barrageAnimeIdList)
            } > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun insertOrUpdateBarrageUser(userBarrageManager: UserBarrageManager): Boolean = dbQuery {
        try {
            if (userBarrageManager.barrageAnimeIdList.isNotEmpty()) {
                userBarrageManager.barrageAnimeIdList.forEach { ubmSd ->
                    queryBarrage(ubmSd.animeId, ubmSd.exCode)?.let { barrageEntity ->
                        barrageEntity.barrageList.removeAll { it1 ->
                            it1.userName == userBarrageManager.userName && it1.text !in ubmSd.barrageList
                        }
                        updateBarrage(barrageEntity)
                    }
                }
            }

            queryBarrageUser(userBarrageManager.userName)?.let {
                updateBarrageUser(userBarrageManager)
            } ?: insertBarrageUser(userBarrageManager)

        } catch (e: Exception) {
            false
        }
    }

    override suspend fun removeBarrageUser(userName: String): Boolean = dbQuery {
        UserBarrageManagerTable.deleteWhere { UserBarrageManagerTable.userName eq userName } > 0
    }

    override suspend fun removeTheAnimeIdBarrageOfTheUser(userName: String, animeId: String, exCode: Int): Boolean =
        dbQuery {
            try {
                queryBarrageUser(userName)?.let {
                    it.barrageAnimeIdList.removeAll { it1 -> it1.animeId == animeId && it1.exCode == exCode }
                    updateBarrageUser(it)
                } ?: false
            } catch (e: Exception) {
                false
            }
        }

    override suspend fun removeTheAnimeIdAllBarrageOfTheUser(userName: String, animeId: String): Boolean = dbQuery {
        try {
            queryBarrageUser(userName)?.let {
                it.barrageAnimeIdList.removeAll { it1 -> it1.animeId == animeId }
                updateBarrageUser(it)
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    private fun resultRowToUserBarrageShieldWord(row: ResultRow) = UserShieldWord(
        userName = row[UserShieldWordTable.userName],
        shieldWordList = json.decodeFromString(row[UserShieldWordTable.shieldWordList])
    )

    override suspend fun queryAllUserBarrageShieldWord(): List<String> = dbQuery {
        UserShieldWordTable.selectAll().map(::resultRowToUserBarrageShieldWord).map { it.userName }
    }

    override suspend fun queryUserBarrageShieldWord(userName: String): List<String>? = dbQuery {
        UserShieldWordTable.select { UserShieldWordTable.userName eq userName }
            .mapNotNull(::resultRowToUserBarrageShieldWord)
            .singleOrNull()?.shieldWordList
    }

    override suspend fun insertUserBarrageShieldWord(userNameValue: String, words: List<String>): Boolean = dbQuery {
        try {
            UserShieldWordTable.insert {
                it[userName] = userNameValue
                it[shieldWordList] = json.encodeToString(words)
            }.resultedValues?.singleOrNull()?.let(::resultRowToUserBarrageShieldWord) != null
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateBarrageUserShieldWord(userName: String, words: List<String>): Boolean = dbQuery {
        try {
            UserShieldWordTable.update({
                UserShieldWordTable.userName eq userName
            }) {
                it[shieldWordList] = json.encodeToString(words)
            } > 0
        } catch (e: Exception) {
            false
        }
    }


    override suspend fun insertOrUpdateBarrageUserShieldWord(userName: String, words: List<String>): Boolean = dbQuery {
        try {
            queryUserBarrageShieldWord(userName)?.let {
                updateBarrageUserShieldWord(userName, words)
            } ?: insertUserBarrageShieldWord(userName, words)
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun removeUserBarrageShieldWord(userName: String): Boolean = dbQuery {
        UserShieldWordTable.deleteWhere { UserShieldWordTable.userName eq userName } > 0
    }

}

val dao: DAOFacade = DAOFacadeImpl().apply {
    runBlocking {
        if (allHistoryUser().isEmpty()) {

        }

    }


}