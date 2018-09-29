package com.ladrope.stocktrader

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.ladrope.stocktrader.model.NotificationsModel
import kotlinx.android.synthetic.main.activity_add_notification.*

class AddNotification : AppCompatActivity() {

    var nwsTitle: TextView? = null
    var nwsMsg: TextView? = null
    //var nwsLink: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_notification)

        nwsMsg = addNotesMgs
        nwsTitle = addNotesTitle
        //nwsLink = addNewsLink

    }


    fun postWithNotification(view: View){
        postNews(true)
    }

    fun postNews(withNofify: Boolean){
        if (nwsMsg?.text.isNullOrEmpty()){
            Toast.makeText(this, "Please enter all information", Toast.LENGTH_SHORT).show()
        }else {
            val news = NotificationsModel()
            news.title = "STOCK TRACK"
            news.msg = nwsMsg?.text.toString()
            news.date = System.currentTimeMillis() * -1

            FirebaseDatabase.getInstance().reference.child("notifications")
                    .push()
                    .setValue(news)
                    .addOnCompleteListener {
                        Toast.makeText(this@AddNotification, "Notifications posted", Toast.LENGTH_SHORT).show()
                        this@AddNotification.finish()
                    }.addOnFailureListener {
                        Toast.makeText(this@AddNotification, "Notifications posting failed, check your internet connection", Toast.LENGTH_SHORT).show()
                    }
            notifyUsers(withNofify, news.msg!!, news.title!!)
        }

    }

    fun notifyUsers(withNofify: Boolean, str: String, title: String){
        if (withNofify){
            sendNotification(str, title, "NOTIFICATION", "link")
        }
    }
}