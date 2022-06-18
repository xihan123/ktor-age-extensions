package cn.xihan.plugins


import cn.xihan.dao.dao
import cn.xihan.models.JsonResult
import cn.xihan.models.PlayHistoryEntity
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.configureRouting() {
    install(AutoHeadResponse)

    routing {
        playHistoryRouting()
    }
}

/**
 * 播放历史记录路由
 */
fun Route.playHistoryRouting() {
    route("/AGE-API/history") {
        get {
            val userName = call.request.queryParameters["userName"]
            //println("userName:$userName")
            if (userName.isNullOrEmpty()) {
                return@get call.respond("请求参数错误")
            }
            dao.queryHistory(userName)?.let {
                call.respond(it)
            } ?: call.respond("查询失败,用户名不存在")
        }

        post {
            val playHistoryEntity = call.receive<PlayHistoryEntity>()
            val isSuccess = dao.insertOrUpdateHistory(playHistoryEntity)
            call.respond(if (isSuccess) "更新成功" else "更新失败")
        }

        delete {
            val userName = call.request.queryParameters["userName"]
            //println("userName:$userName")
            if (userName.isNullOrEmpty()) {
                return@delete call.respond("请求参数错误")
            }
            val isSuccess = dao.removeHistory(userName)

            call.respond(if (isSuccess) "删除成功" else "删除失败")
        }

    }

}
