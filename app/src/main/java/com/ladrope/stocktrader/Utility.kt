package com.ladrope.stocktrader

import com.ladrope.stocktrader.model.NewsModel
import com.ladrope.stocktrader.model.Tip
import com.ladrope.stocktrader.model.User

var isAdmin = false

var selectedTip: Tip? = null
var selectedNews: NewsModel? = null

var usersIds = ArrayList<String?>()

var ADMIN_EXPERT = "eqwealth.in"

var user: User? = null

var tipList = ArrayList<Tip>()
var tipIdList = ArrayList<String>()

var tipAdapterPosition: Int? = null