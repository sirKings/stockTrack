package com.ladrope.stocktrader.model

class Expert() {
    var title: String? = null
    var image: String? = null
    var id: String? = null


    constructor(id: String, title: String?, image: String?): this(){

        this.title = title
        this.image = image
        this.id = id
    }
}
