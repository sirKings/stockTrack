package com.ladrope.stocktrader.model

class Message() {
    var title: String? = null
    var msg: String? = null
    var date: Long? = null
    var id: String? = null


    constructor(id: String?, title: String?, msg: String?, date: Long?): this(){

        this.title = title
        this.msg = msg
        this.id = id
        this.date = date
    }
}