package com.ladrope.stocktrader

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView
import kotlinx.android.synthetic.main.activity_terms.*


class TermsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)

        backBtn.setOnClickListener {
            finish()
        }

        val title = intent.getStringExtra("title")
        header.text = title

        val src = intent.getStringExtra("src")

        val doc = "<iframe "+src+ " width='100%' height='100%' style='border: none;'></iframe>"

        val wv = findViewById(R.id.webview) as WebView
        wv.settings.javaScriptEnabled = true
        wv.settings.allowFileAccess = true
        //wv.loadUrl(doc);
        wv.loadData(doc, "text/html", "UTF-8")
    }
}
