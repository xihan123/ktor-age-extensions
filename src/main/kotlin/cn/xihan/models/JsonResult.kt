package tk.xihantest.models

import kotlinx.serialization.Serializable


@Serializable
class JsonResult<T> {

    private var data //数据内容
            : T? = null

    private var code //状态码
            : String = "404"

    private var msg //提示消息
            : String = "error"



    constructor(data: T?) {
        this.data = data
        this.code = "200"
        this.msg = "success"
    }

    constructor(data: T?, msg: String) {
        this.data = data
        this.code = "200"
        this.msg = msg
    }

    constructor(data: T, code: String, msg: String){
        this.data = data
        this.code = code
        this.msg = msg
    }



}