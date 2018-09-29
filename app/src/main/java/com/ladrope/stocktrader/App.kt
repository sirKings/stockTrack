package com.ladrope.stocktrader

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.database.FirebaseDatabase
import com.onesignal.OneSignal
import io.branch.referral.Branch


class App : Application() {


    override fun onCreate() {
        super.onCreate()

        //Facebook
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)

        //Firebase persist memory
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        // Initialize the Branch object
        Branch.getAutoInstance(this)

        //initialize onesignal
        //OneSignal.setLogLevel(OneSignal.LOG_LEVEL.DEBUG, OneSignal.LOG_LEVEL.DEBUG)
        // OneSignal Initialization
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .setNotificationOpenedHandler(NotificationsRecieved(this))
                .init()
    }

}