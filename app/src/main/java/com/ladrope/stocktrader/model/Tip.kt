package com.ladrope.stocktrader.model

class Tip() {
    var status: Boolean? = null
    var comment: String? = null
    var image: String? = null
    var id: String? = null
    var date: Long? = null
    var closeDate: Long? = null
    var rank: Long? = null
    var lotSize: Int? = null
    var expertName: String? = null
    var call: String? = null
    var target: String? = null
    var stopLoss: String? = null
    var period: String? = null
    var nameSec: String? = null
    var segment: String? = null
    var entryPrice: String? = null
    var exitPrice: String? = null
    var isNewstype: Boolean? = null
    var margin: String? = null
    var canTrade: Boolean? = null
    var expertComments: ArrayList<Message>? = null

    constructor(comment: String?, margin: String?, closeDate: Long?, lotSize: Int, isNewstype: Boolean?, expertComments: ArrayList<Message>?, id: String, status: Boolean?, image: String?, date: Long?, rank: Long?, expertName: String?, call: String?, entryPrice: String?, exitPrice: String?, target: String?, stopLoss: String, period: String?, segment: String, nameSec: String, canTrade: Boolean?): this(){
        this.comment = comment
        this.status = status
        this.image = image
        this.date = date
        this.expertName = expertName
        this.call = call
        this.entryPrice = entryPrice
        this.target = target
        this.stopLoss = stopLoss
        this.period = period
        this.nameSec = nameSec
        this.segment = segment
        this.id = id
        this.rank = rank
        this.isNewstype = isNewstype
        this.expertComments = expertComments
        this.closeDate = closeDate
        this.lotSize = lotSize
        this.exitPrice = exitPrice
        this.margin = margin
        this.canTrade = canTrade
    }
}
