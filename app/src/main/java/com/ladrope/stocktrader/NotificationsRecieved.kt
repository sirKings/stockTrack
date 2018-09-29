package com.ladrope.stocktrader

import android.content.Context
import android.content.Intent
import android.util.Log
import com.onesignal.OSNotificationAction
import com.onesignal.OSNotificationOpenResult
import com.onesignal.OneSignal


class NotificationsRecieved(val context: Context) : OneSignal.NotificationOpenedHandler {
    // This fires when a notification is opened by tapping on it.
    override fun notificationOpened(result: OSNotificationOpenResult) {
        val actionType = result.action.type
        val data = result.notification.payload.additionalData
        val customKey: String?
        //val note = result.notification.payload.body

        //Log.e("Notification", data.toString(2))

        if (data != null) {
            customKey = data.getString("type")
            if (customKey != null){
                if (customKey == "TIPS"){
                    val link = data.getString("link")
                    val intent = Intent(context, TipDetailActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("tipId", link)
                    context.startActivity(intent)
                }else if (customKey == "NOTIFICATION"){
                    val intent = Intent(context, Home::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    intent.putExtra("type", "notes")
                    context.startActivity(intent)
                }else{
                    val intent = Intent(context, Home::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    intent.putExtra("type", "news")
                    context.startActivity(intent)
                }
            }
        }

        if (actionType == OSNotificationAction.ActionType.ActionTaken)
            Log.i("OneSignalExample", "Button pressed with id: " + result.action.actionID)

        // The following can be used to open an Activity of your choice.
        // Replace - getApplicationContext() - with any Android Context.
//         val intent = Intent(context, Home::class.java)
//         intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
//         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//         context.startActivity(intent)

        // Add the following to your AndroidManifest.xml to prevent the launching of your main Activity
        //   if you are calling startActivity above.
        /*
        <application ...>

        </application>
     */
    }
}