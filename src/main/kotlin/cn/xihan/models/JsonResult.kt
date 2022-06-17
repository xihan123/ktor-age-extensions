package cn.xihan.models

import kotlinx.serialization.Serializable


@Serializable
class JsonResult<T>(
    val msg: String = "",
    val data: T? = null
)