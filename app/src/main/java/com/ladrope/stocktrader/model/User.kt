package com.ladrope.stocktrader.model

class User() {
    var capital: Int? = null
    var balance: Int? = null

    constructor(capital: Int?, balance: Int?): this(){

        this.balance = balance
        this.capital = capital
    }
}
