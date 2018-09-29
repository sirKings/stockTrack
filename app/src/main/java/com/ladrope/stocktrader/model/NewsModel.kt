package com.ladrope.stocktrader.model

class NewsModel() {
    var title: String? = null
    var image: String? = null
    var msg: String? = null
    var link: String? = null
    var date: Long? = null
    var expertComments: ArrayList<Message>? = null


    constructor(msg: String, title: String?, image: String?, link: String?, date: Long?, expertComments: ArrayList<Message>?): this(){

        this.title = title
        this.image = image
        this.msg = msg
        this.link = link
        this.date = date
        this.expertComments = expertComments
    }
}
