package cn.xihan.dao

import cn.xihan.dao.DatabaseFactory.dbQuery
import cn.xihan.models.PlayHistoryEntity
import cn.xihan.models.PlayHistoryTable
import cn.xihan.plugins.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.sql.*

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

}

val dao: DAOFacade = DAOFacadeImpl().apply {
    runBlocking {
        if (allHistoryUser().isEmpty()) {

        }

    }


}