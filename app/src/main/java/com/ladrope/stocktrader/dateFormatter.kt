package com.ladrope.stocktrader

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.text.Html
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ladrope.stocktrader.model.Tip
import com.ladrope.stocktrader.model.User
import com.onesignal.OneSignal
import org.json.JSONException
import org.json.JSONObject
import java.text.DateFormatSymbols
import java.util.*


fun getDate(str: kotlin.Long): String{

    val cal = Calendar.getInstance()
    cal.timeInMillis = str

    val mYear = cal.get(Calendar.YEAR)
    val mMnth = cal.get(Calendar.MONTH)
    val mDay = cal.get(Calendar.DAY_OF_MONTH)

    val mHour = cal.get(Calendar.HOUR_OF_DAY)
    val mMnt = cal.get(Calendar.MINUTE)
    val mTime = cal.get(Calendar.AM_PM)

    return mDay.toString() + " " + getMonth(mMnth) + " " + mYear.toString() +" "+mHour.toString() +":"+mMnt+" "+ getPm(mTime)

}

fun getMonth(num: Int): String{
    var month = ""
    val dfs = DateFormatSymbols()
    val months = dfs.shortMonths
    if (num >= 0 && num <= 11){
        month = months[num]
    }
    return month
}

fun getPm(num: Int): String{
    var month = ""
    val dfs = DateFormatSymbols()
    val months = dfs.amPmStrings
    if (num >= 0 && num <= 1){
        month = months[num]
    }
    return month
}

fun getShortDate(str: kotlin.Long?): String{
    val cal = Calendar.getInstance()
    if (str != null) {
        cal.timeInMillis = str

        val mMnth = cal.get(Calendar.MONTH)
        val mDay = cal.get(Calendar.DAY_OF_MONTH)

        return mDay.toString() + " " + getMonth(mMnth)
    }else{
        return ""
    }
}

fun getProfit(tip: Tip?): Float?{

    //Log.e(tip?.nameSec, tip?.exitPrice + " "+tip?.lotSize)

    if (tip?.exitPrice == null){
        return null
    }

    if (tip.lotSize == null){
        return null
    }

    if (tip.call == "Buy"){
        return tip.lotSize!!.toFloat() * (tip.exitPrice!!.toFloat() - tip.entryPrice!!.toFloat())
    }else if (tip.call == "Sell"){
        return tip.lotSize!!.toFloat() * (tip.entryPrice!!.toFloat() - tip.exitPrice!!.toFloat())
    }
    return null
}

fun sendNotification(text: String, title: String, type: String, link: String){
    val users = getUsers()
    Log.e("Users", users)
    try {
        OneSignal.postNotification(JSONObject("{'contents': {'en':'"+text+"'},'headings': {'en':'"+title+"'},'data': {'type': '"+type+"', 'link': '"+link+"'}, 'include_player_ids':" +users+"}"),
                object : OneSignal.PostNotificationResponseHandler {
                    override fun onSuccess(response: JSONObject) {
                        Log.i("OneSignalExample", "postNotification Success: " + response.toString())
                    }

                    override fun onFailure(response: JSONObject) {
                        Log.e("OneSignalExample", "postNotification Failure: " + response.toString())
                    }
                })
    } catch (e: JSONException) {
        e.printStackTrace()
    }

}

fun getUsers(): String{
    var users = "["
    for (i in usersIds){
        users += ("'"+ i +"',")
    }
    Log.e("users as str", usersIds.toString())
    return users.dropLast(1) + "]"
}

fun saveUserId(){
    val status = OneSignal.getPermissionSubscriptionState()
    val id = status.subscriptionStatus.userId

    if(id != null) {
        FirebaseDatabase.getInstance().reference
                .child("users")
                .child(id)
                .setValue(id)
    }
}

fun createUser(usr: User){

    FirebaseDatabase.getInstance().reference.child("profiles").child(FirebaseAuth.getInstance().uid!!)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        user = p0.getValue(User::class.java)
                    }else{
                        user  = usr
                        saveUser(usr)
                    }
                }

            })
}

fun saveUser(user: User){
    FirebaseDatabase.getInstance().reference.child("profiles").child(FirebaseAuth.getInstance().uid!!)
            .setValue(user)
}

fun executeTrade(context: Context, tip: Tip?){
    FirebaseDatabase.getInstance().reference
            .child("profiles")
            .child(FirebaseAuth.getInstance().uid!!)
            .child("trades")
            .child(tip?.id!!)
            .setValue(tip.id)
            .addOnCompleteListener {
                Toast.makeText(context, "Trade made", Toast.LENGTH_SHORT).show()
            }

}


fun getAllTradeMargin(): Int{
    var margin = 0
    var marginPro = 0
    for (tip in tipList){
        if (tip.margin != null){
            margin += tip.margin!!.toInt()
        }
        if (tip.status == false){
            marginPro += tip.margin!!.toInt()
        }
    }
    return margin - marginPro
}

fun getProfitWithList(list: List<Tip>): Int{

    var profit = 0f
    for (tip in list){
        val pro = getProfit(tip)
        if (pro != null){
            profit += pro
        }
    }
    return profit.toInt()
}

fun showCantTradeAlert(context: Context){
    val builder1 = AlertDialog.Builder(context)
    builder1.setTitle(Html.fromHtml("<font color='#ff0000'>Insufficient Balance</font>"))
    builder1.setMessage("Delete some old trades before you continue")
    builder1.setCancelable(true)

    builder1.setPositiveButton(
            "Ok",
            DialogInterface.OnClickListener { dialog, id ->

                dialog.cancel()
            })

    val alert = builder1.create()
    alert.show()
}

fun getTipss(){
    Log.e("tips", "detected")
    tipList.clear()
    for (i in tipIdList){
        FirebaseDatabase.getInstance().reference
                .child("tips")
                .child(i)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            tipList.add(p0.getValue(Tip::class.java)!!)
                            //calculateProfit()
                        }else{

                        }
                    }
                })
    }

}

fun getNumberReadMore(tipId: String?, tv: TextView){
    if (tipId == null){
        tv.text = "Read more(0)"
        tv.isClickable = isAdmin
        return
    }
    FirebaseDatabase.getInstance().reference
            .child("tips")
            .child(tipId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    tv.text = "Read more(0)"
                    tv.isClickable = isAdmin
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        val tip = p0.getValue(Tip::class.java)
                        if (tip?.expertComments != null){
                            tv.text = "Read more("+tip?.expertComments?.size+")"
                            tv.isClickable = true
                        }else{
                            tv.text = "Read more(0)"
                            tv.isClickable = isAdmin
                        }

                    }else{
                        tv.text = "Read more(0)"
                        tv.isClickable = isAdmin
                    }
                }
            })
}

fun getTipStatus(tip: Tip): Boolean{
    for (t in tipList){
        if (tip.id == t.id){
            return true
        }
    }
    return false
}