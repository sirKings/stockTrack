package com.ladrope.stocktrader

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity



class Splash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.splash_screen)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
