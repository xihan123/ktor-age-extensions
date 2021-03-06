package cn.xihan.dao

import cn.xihan.models.BarrageTable
import cn.xihan.models.PlayHistoryTable
import cn.xihan.models.UserBarrageManagerTable
import cn.xihan.models.UserShieldWordTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction


object DatabaseFactory {

    fun init() {
        val driverClassName = "org.h2.Driver"
        val jdbcURL = "jdbc:h2:file:./build/db"
        val database = Database.connect(jdbcURL, driverClassName)
        transaction(database) {
            SchemaUtils.create(PlayHistoryTable)
//            SchemaUtils.create(BarrageTable)
//            SchemaUtils.create(UserBarrageManagerTable)
            SchemaUtils.create(UserShieldWordTable)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }

}