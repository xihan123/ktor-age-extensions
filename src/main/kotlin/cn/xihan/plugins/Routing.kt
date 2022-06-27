package cn.xihan.plugins


import cn.xihan.dao.dao
import cn.xihan.models.BarrageEntity
import cn.xihan.models.PlayHistoryEntity
import cn.xihan.models.UserBarrageManager
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.configureRouting() {
    install(AutoHeadResponse)

    routing {
        playHistoryRouting()
        barrageRouting()
    }
}

/**
 * 弹幕
 */
fun Route.barrageRouting() {

    route("/AGE-API/barrage") {
        get {
            val animeId = call.request.queryParameters["animeId"]
            val exCode = call.request.queryParameters["exCode"]?.toInt()
            if (animeId.isNullOrEmpty() && exCode == null) {
                return@get call.respond("请求参数错误")
            }
            dao.queryBarrage(animeId!!, exCode!!)?.let {
                call.respond(it)
            } ?: call.respond("查询失败")
        }

        post {
            val animeId = call.request.queryParameters["animeId"]
            val exCode = call.request.queryParameters["exCode"]?.toInt()
            val barrageEntity = call.receive<BarrageEntity.Barrage>()

            if (animeId.isNullOrEmpty() && exCode == null) {
                return@post call.respond("请求参数错误")
            }

            val isSuccess = dao.insertBarrageToTitle(animeId!!, exCode!!, barrageEntity)
            call.respond(if (isSuccess) "更新成功" else "更新失败")
        }

    }

    route("/AGE-API/barrageManage") {
        get {
            val userName = call.request.queryParameters["userName"]

            if (userName.isNullOrEmpty()) {
                return@get call.respond("请求参数错误")
            }
            dao.queryBarrageUser(userName)?.let {
                call.respond(it)
            } ?: call.respond("查询失败")
        }

        post {
            val userBarrageManager = call.receive<UserBarrageManager>()
            val isSuccess = dao.insertOrUpdateBarrageUser(userBarrageManager)
            call.respond(if (isSuccess) "更新成功" else "更新失败")
        }

        delete {
            val userName = call.request.queryParameters["userName"]
            val animeId = call.request.queryParameters["animeId"]
            val exCode = call.request.queryParameters["exCode"]?.toInt()
            val type = call.request.queryParameters["type"]

            type?.let {
                when (it) {
                    "1" -> {
                        if (userName.isNullOrEmpty() && animeId.isNullOrEmpty()) {
                            return@delete call.respond("请求参数错误")
                        }
                        val isSuccess = dao.removeTheAnimeIdAllBarrageOfTheUser(userName!!, animeId!!)
                        call.respond(if (isSuccess) "删除成功" else "删除失败")

                    }

                    "2" -> {
                        if (userName.isNullOrEmpty() && animeId.isNullOrEmpty() && exCode == null) {
                            return@delete call.respond("请求参数错误")
                        }
                        val isSuccess = dao.removeTheAnimeIdBarrageOfTheUser(userName!!, animeId!!, exCode!!)
                        call.respond(if (isSuccess) "删除成功" else "删除失败")
                    }
                }
            } ?: call.respond("请求参数错误")


        }


    }

    /**
     * 弹幕屏蔽词
     */
    route("/AGE-API/barrageShield") {
        get {
            val userName = call.request.queryParameters["userName"]
            if (userName.isNullOrEmpty()) {
                return@get call.respond("请求参数错误")
            }
            dao.queryUserBarrageShieldWord(userName)?.let {
                call.respond(it)
            } ?: call.respond("查询失败")
        }

        post {
            val userName = call.request.queryParameters["userName"]
            val barrageShieldWord = call.receive<List<String>>()
            if (userName.isNullOrEmpty() || barrageShieldWord.isEmpty()) {
                return@post call.respond("请求参数错误")
            }
            val isSuccess = dao.insertOrUpdateBarrageUserShieldWord(userName, barrageShieldWord)
            call.respond(if (isSuccess) "更新成功" else "更新失败")
        }

        delete {
            val userName = call.request.queryParameters["userName"]
            if (userName.isNullOrEmpty()) {
                return@delete call.respond("请求参数错误")
            }
            val isSuccess = dao.removeUserBarrageShieldWord(userName)
            call.respond(if (isSuccess) "删除成功" else "删除失败")
        }
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
