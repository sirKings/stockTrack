package com.ladrope.stocktrader

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ladrope.stocktrader.model.Tip
import com.squareup.picasso.Picasso
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import io.branch.referral.util.LinkProperties
import kotlinx.android.synthetic.main.activity_tip_detail.*

class TipDetailActivity : AppCompatActivity() {

    val buo = BranchUniversalObject()

    var progres: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tip_detail)

        header.text = selectedTip?.nameSec?.toUpperCase()
        backBtn.setOnClickListener {
            selectedTip = null
            finish()
        }

        val tipId = intent.getStringExtra("tipId")

        progres = progresdetail
        progres?.visibility = View.VISIBLE

        getTip(tipId)

        //bindItem(selectedTip!!)
    }

    fun getTip(tipId: String){
        FirebaseDatabase.getInstance().reference
                .child("tips")
                .child(tipId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        progres?.visibility = View.GONE
                        header.text = "Network error"
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            val tip = p0.getValue(Tip::class.java)
                            bindItem(tip!!)
                            progres?.visibility = View.GONE
                        }else{
                            progres?.visibility = View.GONE
                            header.text = "Tip not found"
                            finish()
                        }
                   }
                })
    }

    fun bindItem(tip: Tip) {
        header.text = tip.nameSec?.toUpperCase()
        if (tip.isNewstype != true){
            newsSection.visibility = View.GONE
            view2.visibility = View.VISIBLE
            val nameSec = findViewById<TextView>(R.id.nameSec)
            val expertName = findViewById<TextView>(R.id.tipExpertName)
            val date = findViewById<TextView>(R.id.tipTime)
            val period = findViewById<TextView>(R.id.tipPeriod)
            val segment = findViewById<TextView>(R.id.segment)
            val call = findViewById<TextView>(R.id.tipCall)
            val target = findViewById<TextView>(R.id.tipTarget)
            val stopLoss = findViewById<TextView>(R.id.tipSl)
            val expertImage = findViewById<ImageView>(R.id.tipImage)
            val card = findViewById<CardView>(R.id.view2)

            nameSec.text = ""
            nameSec.text = tip.nameSec!!
            tipPrice.text = " @ " + tip.entryPrice!!
            Log.e("Price",tip.entryPrice)
            expertName.text = tip.expertName
            date.text = getDate(tip.date!!)
            period.text = tip.period
            segment.text = tip.segment
            call.text = tip.call
            target.text = tip.target
            stopLoss.text = tip.stopLoss
            Picasso.with(this).load(tip.image).into(expertImage)

            var lot = ""
            var margin = ""

            if (tip.lotSize != null){
                lot = tip.lotSize.toString()
                noticeString1.text = "Lot size: "+lot
            }

            if (tip.margin != null){
                margin = tip.margin!!
                noticeString1.text = "Lot size: "+lot+" Margin: "+margin
            }

            if (tip.status == true){
                card.setCardBackgroundColor(resources.getColor(R.color.light_green))
            }else{
                card.setCardBackgroundColor(resources.getColor(R.color.light_red))
            }

            if (tip.call == "Sell"){
                callCard.setCardBackgroundColor(resources.getColor(R.color.red_color_picker))
                //targetCard.setCardBackgroundColor(resources.getColor(R.color.red_color_picker))
            }else{
                callCard.setCardBackgroundColor(resources.getColor(R.color.green_card))
                //targetCard.setCardBackgroundColor(resources.getColor(R.color.green_card))
            }

            val num = tip.expertComments?.size
            if(num != null){
                numComments.text = "("+tip.expertComments?.size.toString()+")"
            }else{
                numComments.text = "(0)"
            }

            if (tip.comment != null){
                noticeString.text = tip.comment
                noticeString.visibility = View.VISIBLE
                //itemView.notifier.visibility = View.VISIBLE
            }else{
                noticeString.visibility = View.GONE
                //itemView.notifier.visibility = View.INVISIBLE
            }

            if (!isAdmin){
                buttonsLayout1.visibility = View.GONE
                buttonsLayout2.visibility = View.GONE
            }

            expertName.setOnClickListener {
                val intent = Intent(this, TableActivity::class.java)
                intent.putExtra("expertName", tip.expertName)
                startActivity(intent)
            }

            if (tip.canTrade == true){
                //infoString.text = "Click "+tip.call+" above to trade with virtual money"
                infoString.visibility = View.VISIBLE

                callCard.setOnClickListener {
                    if(getTipStatus(tip)){
                        Toast.makeText(this, "Already traded by you", Toast.LENGTH_SHORT).show()
                    }else if (tip.status == false){
                        Toast.makeText(this, "Call has been closed", Toast.LENGTH_SHORT).show()
                    }else{
                    buyTipLayout.visibility = View.VISIBLE
                    capital.text = "" + (200000 - getAllTradeMargin())
                    capitalMargin.text = ""
                    balance.text = ""

                    var canTrade = true

                    if (tip.margin != null){
                        if (tip.margin!!.toInt() > (200000 - getAllTradeMargin())){
                            balance.setTextColor(Color.RED)
                            canTrade = false
                        }
                    }

                    tipRowNo.setOnClickListener {
                        buyTipLayout.visibility = View.GONE
                    }
                    tipRowyes.setOnClickListener {
                        if (canTrade){
                            capitalMargin.text = tip.margin
                            executeTrade(this, tip)
                            balance.text = ""+ (200000 - (getAllTradeMargin() + tip.margin!!.toInt()))
                        }else{
                            showCantTradeAlert(this)
                        }
                    }


                    goToTrade.setOnClickListener {
                        startActivity(Intent(this, TradeActivity::class.java))
                    }
                }
                }

            }else{
                infoString.visibility = View.GONE
                callCard.setOnClickListener {
                    Toast.makeText(this, "This tip can not be traded", Toast.LENGTH_SHORT).show()
                }
            }


            tipReadMore.setOnClickListener {
                if(isAdmin){
                    val intent = Intent(this, ReadMoreActivity::class.java)
                    intent.putExtra("where", "expertComments")
                    intent.putExtra("tipId", tip.id)
                    selectedTip = tip
                    startActivity(intent)
                } else if (tip.expertComments?.size != null && tip.expertComments?.size!! > 0){
                    val intent = Intent(this, ReadMoreActivity::class.java)
                    intent.putExtra("where", "expertComments")
                    intent.putExtra("tipId", tip.id)
                    selectedTip = tip
                    startActivity(intent)
                }

            }

            targetMet.setOnClickListener {
                targetMet(this, tip)
            }


            editStatus.setOnClickListener {
                updateStatus(this, tip)
            }

            slHit.setOnClickListener {
                stopLossMet(this, tip)
            }

            exitAt.setOnClickListener {
                exitTipAt(this, tip)
            }

            deleteTipRow.setOnClickListener {
                deleteTip(tip, this)
            }

            shareBtn.setOnClickListener {
                shareTip(tip, this)
            }
        }else{
            view2.visibility = View.GONE
            newsSection.visibility = View.VISIBLE
            newTextV.text = tip.comment

            val num = tip.expertComments?.size
            if(num != null){
                newsReadMore.text = "Read more ("+tip.expertComments?.size.toString()+")"
            }else{
                newsReadMore.text = "Read more (0)"
            }

            newsDeleteBtn.setOnClickListener {
                deleteTip(tip, this)
            }

            if(!isAdmin){
                newsDeleteBtn.visibility = View.GONE
            }else{
                newsDeleteBtn.visibility = View.VISIBLE
            }

            newsReadMore.setOnClickListener {
                if (isAdmin){
                    val intent = Intent(this, ReadMoreActivity::class.java)
                    intent.putExtra("where", "expertComments")
                    intent.putExtra("tipId", tip.id)
                    selectedTip = tip
                    startActivity(intent)
                }else if (tip.expertComments?.size != null && tip.expertComments?.size!! > 0){
                    val intent = Intent(this, ReadMoreActivity::class.java)
                    intent.putExtra("where", "expertComments")
                    intent.putExtra("tipId", tip.id)
                    selectedTip = tip
                    startActivity(intent)
                }
            }

        }

    }

    fun deleteTip(tip: Tip, context: Context){
        FirebaseDatabase.getInstance().reference
                .child("tips")
                .child(tip.id!!)
                .setValue(null)
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to delete tip, please check your internet connection", Toast.LENGTH_LONG).show()
                }.addOnCompleteListener {
                    Toast.makeText(context, "Tip deleted", Toast.LENGTH_LONG).show()
                }
    }

    fun targetMet(context: Context, tip: Tip){
        val builder1 = AlertDialog.Builder(context)
        builder1.setTitle(Html.fromHtml("<font color='#000000'>Target Met?</font>"))
        builder1.setCancelable(true)

        builder1.setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { dialog, id ->

                    tip.status = false
                    tip.exitPrice = tip.target
                    tip.closeDate = System.currentTimeMillis()
                    tip.comment = "Target achieved"
                    updateTip(tip)
                    dialog.cancel()
                })

//        builder1.setNegativeButton(
//                "No",
//                DialogInterface.OnClickListener { dialog, id ->
//
//                    tip.status = false
//                    updateTip(tip)
//                    dialog.cancel()
//                })

        val alert = builder1.create()
        alert.show()
    }

    fun stopLossMet(context: Context, tip: Tip){
        val builder1 = AlertDialog.Builder(context)
        builder1.setTitle(Html.fromHtml("<font color='#000000'>Stop Loss Met?</font>"))
        builder1.setCancelable(true)

        builder1.setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { dialog, id ->

                    tip.status = false
                    tip.comment = "SL Triggered"
                    tip.exitPrice = tip.stopLoss
                    tip.closeDate = System.currentTimeMillis()
                    updateTip(tip)
                    dialog.cancel()
                })

//        builder1.setNegativeButton(
//                "No",
//                DialogInterface.OnClickListener { dialog, id ->
//
//                    tip.status = false
//                    updateTip(tip)
//                    dialog.cancel()
//                })

        val alert = builder1.create()
        alert.show()
    }

    fun updateStatus(context: Context, tip: Tip){
//        val builder1 = AlertDialog.Builder(context)
//        builder1.setTitle(Html.fromHtml("<font color='#000000'>Update Status?</font>"))
//        builder1.setCancelable(true)
//
//        builder1.setPositiveButton(
//                "Open",
//                DialogInterface.OnClickListener { dialog, id ->
//
//                    tip.status = true
//                    tip.comment = "Call Open"
//                    tip.exitPrice = tip.target
//                    tip.closeDate = System.currentTimeMillis()
//                    updateTip(tip)
//                    dialog.cancel()
//                })
//
//        builder1.setNegativeButton(
//                "Close",
//                DialogInterface.OnClickListener { dialog, id ->
//
//                    tip.status = false
//                    tip.comment = "Call Closed"
//                    updateTip(tip)
//                    dialog.cancel()
//                })
//
//        val alert = builder1.create()
//        alert.show()
        val intent = Intent(this, AddTipActivity::class.java)
        intent.putExtra("isEdit", true)
        selectedTip = tip
        startActivity(intent)
    }

    fun buy(context: Context, tip: Tip){
//        val alert = AlertDialog.Builder(context)
//        val view = LayoutInflater.from(context).inflate(R.layout.buy_tip, null,false)
//        val balance = view.findViewById<TextView>(R.id.balance)
//        val capital = view.findViewById<TextView>(R.id.capital)
//        val margin = view.findViewById<TextView>(R.id.margin)
//
//        balance.text = calculateProfit().toString()
//        capital.text = user?.capital.toString()
//        margin.text = tip.margin
//
//        alert.setPositiveButton("Ok", DialogInterface.OnClickListener{
//            dialogInterface, i ->
//
//            executeTrade(context, tip)
//        })
//
//        alert.setNegativeButton("Cancel", DialogInterface.OnClickListener{
//            dialogInterface, i ->
//            dialogInterface.cancel()
//        })
//
//        alert.setNeutralButton("My Trade", DialogInterface.OnClickListener{
//            dialogInterface, i ->
//
//            startActivity(Intent(this, TradeActivity::class.java))
//
//        })
//
//        alert.setView(view).create()
//        alert.show()
    }

    fun exitTipAt(context: Context, tip: Tip){
        val alert = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.exit_tip_text, null,false)
        val edt = view.findViewById<TextView>(R.id.exitTip)

        alert.setPositiveButton(
                "OK",
                DialogInterface.OnClickListener { dialog, id ->

                    tip.status = false
                    if (!edt.text.isEmpty()){
                        tip.comment = "Exit at " +edt.text.toString()
                    }
                    tip.exitPrice = edt.text.toString()
                    tip.closeDate = System.currentTimeMillis()
                    updateTip(tip)
                    dialog.cancel()
                })

        alert.setNegativeButton(
                "Cancel",
                DialogInterface.OnClickListener { dialog, id ->

                    dialog.cancel()
                })
        alert.setView(view).create()
        alert.show()

    }

    fun updateTip(tip: Tip){
        FirebaseDatabase.getInstance().reference
                .child("tips")
                .child(tip.id!!)
                .setValue(tip)
                .addOnCompleteListener {
                    //adapter?.notifyDataSetChanged()
                    tipList.clear()
                    getTipss()
                }
    }

    fun shareTip(tip: Tip, context: Context){

        val title = tip.nameSec +" @ "+tip.entryPrice
        val desc  = "Check out this stocks"

        buo.setCanonicalIdentifier(tip.id!!)
                .setTitle(title)
                .setContentDescription(desc)
                //.setContentImageUrl(tip.image!!)
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)

        val lp = LinkProperties()
        lp.setChannel("facebook")
                .setFeature("sharing")
                .setCampaign("content 123 launch")
                .setStage("new user")

        buo.generateShortUrl(context, lp, Branch.BranchLinkCreateListener { url, error ->
            if (error == null) {
                //addAlbumLink(albumKey, uid, url)
                val share = Intent(Intent.ACTION_SEND)
                share.type = "text/*"
                share.putExtra(Intent.EXTRA_TEXT, url)
                startActivity(Intent.createChooser(share, "Share Link"))
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        selectedTip = null
    }

}
