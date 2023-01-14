package cn.xihan.plugins

import cn.xihan.models.PlayHistoryModel
import cn.xihan.utils.dao
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    install(AutoHeadResponse)

    routing {
        route("/AGE-API/history") {
            get {
                val userName = call.request.queryParameters["userName"]
                //println("userName:$userName")
                if (userName.isNullOrEmpty()) {
                    return@get call.respond("请求参数错误")
                }
                dao.findPlayHistoryByUserName(userName)?.let {
                    call.respond(it)
                } ?: call.respond("查询失败,用户名不存在")
            }

            post {
                val playHistoryModel = call.receive<PlayHistoryModel>()
                val isSuccess = dao.updatePlayHistoryByUserName(playHistoryModel)
                call.respond(if (isSuccess) "更新成功" else "更新失败")
            }

            delete {
                val userName = call.request.queryParameters["userName"]
                if (userName.isNullOrEmpty()) {
                    return@delete call.respond("请求参数错误")
                }
                val isSuccess = dao.deletePlayHistoryByUserName(userName)

                call.respond(if (isSuccess) "删除成功" else "删除失败")
            }

        }
    }
}
