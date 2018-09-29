package com.ladrope.stocktrader.model

class NotificationsModel() {
    var title: String? = null
    var image: String? = null
    var msg: String? = null
    var desc: String? = null
    var date: Long? = null


    constructor(msg: String, title: String?, image: String?, desc: String?, date: Long?): this(){

        this.title = title
        this.image = image
        this.msg = msg
        this.desc = desc
        this.date = date
    }
}
