package com.ladrope.stocktrader


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.ladrope.stocktrader.model.Tip
import com.squareup.picasso.Picasso
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import io.branch.referral.util.LinkProperties
import kotlinx.android.synthetic.main.fragment_tips.view.*
import kotlinx.android.synthetic.main.tip_row.view.*


/**
 * A simple [Fragment] subclass.
 *
 */
class TipsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener{


    var tipsRecyclerView: RecyclerView? = null
    var adapter: TipsAdapter? = null
    var layoutManager: RecyclerView.LayoutManager? = null
    var options: FirebaseRecyclerOptions<Tip>? = null
    var mProgressBar: ProgressBar? =null
    var mErrorText: TextView? = null
    var mEmptyText: TextView? = null
    var swipeRefreshLayout: SwipeRefreshLayout? = null

    val buo = BranchUniversalObject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_tips, container, false)

        tipsRecyclerView = view.tipsRV
        mProgressBar = view.progress
        mErrorText = view.errorText
        mEmptyText = view.emptyText

        mProgressBar?.visibility = View.VISIBLE

        view.fabtip.setOnClickListener {
            addTip()
        }

        val query = FirebaseDatabase.getInstance().reference.child("tips").orderByChild("rank")
        setup(query)

        if(!isAdmin){
            view.fabtip.hide()
        }

//        swipeRefreshLayout = view.swipe_container
//        swipeRefreshLayout?.setOnRefreshListener(this)
//        swipeRefreshLayout?.setColorSchemeResources(R.color.colorPrimary,
//                android.R.color.holo_green_dark,
//                android.R.color.holo_orange_dark,
//                android.R.color.holo_blue_dark)
//


        return view
    }

    override fun onRefresh() {
        val query = FirebaseDatabase.getInstance().reference.child("tips").orderByChild("date")
        setup(query)
        adapter?.startListening()
    }

    fun setup(query: Query){
        Log.e("setup", "started")
        mErrorText?.visibility = View.GONE
        mEmptyText?.visibility = View.GONE
        options = FirebaseRecyclerOptions.Builder<Tip>()
                .setQuery(query, Tip::class.java)
                .build()
        adapter = TipsAdapter(options!!, context!!)
        layoutManager = LinearLayoutManager(context)

        //set up recycler view
        tipsRecyclerView!!.layoutManager = layoutManager
        tipsRecyclerView!!.adapter = adapter

    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }


    fun addTip(){
        val addIntent = Intent(activity, AddTipActivity::class.java)
        startActivity(addIntent)
    }


    inner class TipsAdapter(options: FirebaseRecyclerOptions<Tip>, private val context: Context): FirebaseRecyclerAdapter<Tip, TipsAdapter.ViewHolder>(options) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.tip_row, parent, false)
            return ViewHolder(view)
        }


        override fun onDataChanged() {
            super.onDataChanged()
            mProgressBar?.visibility = View.GONE
            swipeRefreshLayout?.isRefreshing = false
            if (adapter?.itemCount == 0) {
                mEmptyText?.visibility = View.VISIBLE
            }

            if (tipAdapterPosition != null){
                tipsRecyclerView?.smoothScrollToPosition(tipAdapterPosition!!)
                tipAdapterPosition = null
            }

            saveUserId()
        }


        override fun onError(error: DatabaseError) {
            super.onError(error)
            mErrorText?.visibility = View.VISIBLE
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Tip) {
            holder.bindItem(model)
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bindItem(tip: Tip) {
                if (tip.isNewstype != true){
                    itemView.newsSection.visibility = View.GONE
                    itemView.view2.visibility = View.VISIBLE
                    val nameSec = itemView.findViewById<TextView>(R.id.nameSec)
                    val expertName = itemView.findViewById<TextView>(R.id.tipExpertName)
                    val date = itemView.findViewById<TextView>(R.id.tipTime)
                    val period = itemView.findViewById<TextView>(R.id.tipPeriod)
                    val segment = itemView.findViewById<TextView>(R.id.segment)
                    val call = itemView.findViewById<TextView>(R.id.tipCall)
                    val target = itemView.findViewById<TextView>(R.id.tipTarget)
                    val stopLoss = itemView.findViewById<TextView>(R.id.tipSl)
                    val expertImage = itemView.findViewById<ImageView>(R.id.tipImage)
                    val card = itemView.findViewById<CardView>(R.id.view2)

                    nameSec.text = ""
                    nameSec.text = tip.nameSec!!
                    itemView.tipPrice.text = " @ " + tip.entryPrice!!
                    Log.e("Price",tip.entryPrice)
                    expertName.text = tip.expertName
                    date.text = getDate(tip.date!!)
                    period.text = tip.period
                    segment.text = tip.segment
                    call.text = tip.call
                    target.text = tip.target
                    stopLoss.text = tip.stopLoss
                    Picasso.with(context).load(tip.image).into(expertImage)

                    var lot = ""
                    var margin = ""

                    if (tip.lotSize != null){
                        lot = tip.lotSize.toString()
                        itemView.noticeString1.text = "Lot size: "+lot
                    }

                    if (tip.margin != null){
                        margin = tip.margin!!
                        itemView.noticeString1.text = "Lot size: "+lot+" Margin: "+margin
                    }

                    if (tip.status == true){
                        card.setCardBackgroundColor(context.resources.getColor(R.color.light_green))
                    }else{
                        card.setCardBackgroundColor(context.resources.getColor(R.color.light_red))
                    }

                    if (tip.call == "Sell"){
                        itemView.callCard.setCardBackgroundColor(context.resources.getColor(R.color.red_color_picker))
                        //itemView.targetCard.setCardBackgroundColor(context.resources.getColor(R.color.red_color_picker))
                    }else{
                        itemView.callCard.setCardBackgroundColor(context.resources.getColor(R.color.green_card))
                        //itemView.targetCard.setCardBackgroundColor(context.resources.getColor(R.color.green_card))
                    }

                    val num = tip.expertComments?.size
                    if(num != null){
                        itemView.numComments.text = "("+tip.expertComments?.size.toString()+")"
                    }else{
                        itemView.numComments.text = "(0)"
                    }

                    if (tip.comment != null){
                        itemView.noticeString.text = tip.comment
                        itemView.noticeString.visibility = View.VISIBLE
                        //itemView.notifier.visibility = View.VISIBLE
                    }else{
                        itemView.noticeString.visibility = View.GONE
                        //itemView.notifier.visibility = View.INVISIBLE
                    }

                    if (!isAdmin){
                        itemView.buttonsLayout1.visibility = View.GONE
                        itemView.buttonsLayout2.visibility = View.GONE
                    }

                    nameSec.setOnClickListener {
                        tipAdapterPosition = adapterPosition
                        val intent = Intent(activity, TipListActivity::class.java)
                        intent.putExtra("call", tip.nameSec)
                        startActivity(intent)
                    }

                    expertName.setOnClickListener {
                        tipAdapterPosition = adapterPosition
                        val intent = Intent(activity, TableActivity::class.java)
                        intent.putExtra("expertName", tip.expertName)
                        startActivity(intent)
                    }
                    itemView.buyTipLayout.visibility = View.GONE

                    if (tip.canTrade == true){
                        //itemView.infoString.text = "Click "+tip.call+" above to trade with virtual money"
                        itemView.infoString.visibility = View.VISIBLE
                        itemView.callCard.setOnClickListener {
                            if(getTipStatus(tip)){
                                Toast.makeText(activity, "Already traded by you", Toast.LENGTH_SHORT).show()
                            }else if (tip.status == false){
                                Toast.makeText(activity, "Call has been closed", Toast.LENGTH_SHORT).show()
                            }else{
                                itemView.buyTipLayout.visibility = View.VISIBLE
                                itemView.capital.text = "" + (200000 - getAllTradeMargin())
                                itemView.capitalMargin.text = ""
                                itemView.balance.text = ""

                                var canTrade = true

                                if (tip.margin != null){
                                    if (tip.margin!!.toInt() > (200000 - getAllTradeMargin())){
                                        itemView.balance.setTextColor(Color.RED)
                                        canTrade = false
                                    }
                                }

                                itemView.tipRowNo.setOnClickListener {
                                    itemView.buyTipLayout.visibility = View.GONE
                                }
                                itemView.tipRowyes.setOnClickListener {
                                    if (canTrade){
                                        itemView.capitalMargin.text = tip.margin
                                        executeTrade(context, tip)
                                        itemView.balance.text = ""+ (200000 - (getAllTradeMargin() + tip.margin!!.toInt()))
                                    }else{
                                        showCantTradeAlert(context)
                                    }
                                }

                                itemView.goToTrade.setOnClickListener {
                                    tipAdapterPosition = adapterPosition
                                    startActivity(Intent(activity, TradeActivity::class.java))
                                }
                            }

                        }
                    }else{
                        itemView.infoString.visibility = View.GONE
                        itemView.callCard.setOnClickListener {
                            Toast.makeText(activity, "This tip can not be traded", Toast.LENGTH_SHORT).show()
                        }
                    }

                    itemView.tipReadMore.setOnClickListener {
                        if (isAdmin){
                            tipAdapterPosition = adapterPosition
                            val intent = Intent(activity, ReadMoreActivity::class.java)
                            intent.putExtra("where", "expertComments")
                            intent.putExtra("tipId", tip.id)
                            selectedTip = tip
                            startActivity(intent)
                        }else if (tip.expertComments?.size != null && tip.expertComments?.size!! > 0) {
                            tipAdapterPosition = adapterPosition
                            val intent = Intent(activity, ReadMoreActivity::class.java)
                            intent.putExtra("where", "expertComments")
                            intent.putExtra("tipId", tip.id)
                            selectedTip = tip
                            startActivity(intent)
                        }
                    }

                    itemView.targetMet.setOnClickListener {
                        targetMet(context, tip)
                    }


                    itemView.editStatus.setOnClickListener {
                        updateStatus(context, tip)
                    }

                    itemView.slHit.setOnClickListener {
                        stopLossMet(context, tip)
                    }

                    itemView.exitAt.setOnClickListener {
                        exitTipAt(context, tip)
                    }

                    itemView.deleteTipRow.setOnClickListener {
                        deleteTip(tip, context)
                    }

                    itemView.shareBtn.setOnClickListener {
                        shareTip(tip, context)
                    }
                }else{
                    itemView.view2.visibility = View.GONE
                    itemView.newsSection.visibility = View.GONE
//                    itemView.newTextV.text = tip.comment
//                    itemView.newsTime.text = getDate(tip.date!!)
//
//                    if (tip.expertName != null){
//                        itemView.newsTitle.text = tip.expertName
//                        itemView.newsTitle.setOnClickListener {
//                            tipAdapterPosition = adapterPosition
//                            val intent = Intent(activity, NewsActivity::class.java)
//                            intent.putExtra("title", tip.expertName)
//                            startActivity(intent)
//                        }
//                    }
//
//                    val num = tip.expertComments?.size
//                    if(num != null){
//                        itemView.newsReadMore.text = "Read more ("+tip.expertComments?.size.toString()+")"
//                    }else{
//                        itemView.newsReadMore.text = "Read more (0)"
//                    }
//
//                    itemView.newsDeleteBtn.setOnClickListener {
//
//                        checkToDelete(tip, context, adapterPosition)
//                        //deleteTip(tip, context)
//                    }
//
//                    if(!isAdmin){
//                        itemView.newsDeleteBtn.visibility = View.GONE
//                    }else{
//                        itemView.newsDeleteBtn.visibility = View.VISIBLE
//                    }
//
//                    itemView.newsReadMore.setOnClickListener {
//                        if (isAdmin){
//                            tipAdapterPosition = adapterPosition
//                            val intent = Intent(activity, ReadMoreActivity::class.java)
//                            intent.putExtra("where", "expertComments")
//                            intent.putExtra("tipId", tip.id)
//                            selectedTip = tip
//                            startActivity(intent)
//                        }else if (tip.expertComments?.size != null && tip.expertComments?.size!! > 0) {
//                            tipAdapterPosition = adapterPosition
//                            val intent = Intent(activity, ReadMoreActivity::class.java)
//                            intent.putExtra("where", "expertComments")
//                            intent.putExtra("tipId", tip.id)
//                            selectedTip = tip
//                            startActivity(intent)
//                        }
//                    }

                }

            }
        }
    }

    fun checkToDelete(tip: Tip,context: Context, position: Int){

            val builder1 = AlertDialog.Builder(context!!)
            builder1.setTitle(Html.fromHtml("<font color='#000000'>Delete item?</font>"))
            builder1.setCancelable(false)

            builder1.setPositiveButton(
                    "Yes",
                    DialogInterface.OnClickListener { dialog, id ->
                        FirebaseDatabase.getInstance().reference
                                .child("tips")
                                .child(tip.id!!)
                                .setValue(null)
                                .addOnCompleteListener {
                                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                }

                    })

            builder1.setNegativeButton(
                    "No",
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                        //adapter?.notifyDataSetChanged()
                    })

            builder1.setNeutralButton("Edit", DialogInterface.OnClickListener{
                dialog, id ->
                selectedTip = tip
                tipAdapterPosition = position
                editNews(tip.id!!)
            })

            val alert = builder1.create()
            alert.show()
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
        val intent = Intent(activity, AddTipActivity::class.java)
        intent.putExtra("isEdit", true)
        selectedTip = tip
        startActivity(intent)
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

    fun buy(context: Context, tip: Tip){
//        val alert = AlertDialog.Builder(context)
//        //val view = LayoutInflater.from(context).inflate(R.layout., null,false)
//        val balance = view.findViewById<TextView>(R.id.balance)
//        val capital = view.findViewById<TextView>(R.id.capital)
//        val margin = view.findViewById<TextView>(R.id.margin)
//
//        balance.text = calculateProfit().toString()
//        capital.text = user?.capital.toString()
//        margin.text = tip.margin
//
//        if (tip.margin != null){
//            if (tip.margin!!.toInt() > calculateProfit()){
//                view.notice.visibility = View.VISIBLE
//            }else{
//                alert.setPositiveButton("Ok", DialogInterface.OnClickListener{
//                    dialogInterface, i ->
//
//                    executeTrade(context, tip)
//                })
//            }
//        }
//
//
//        alert.setNegativeButton("Cancel", DialogInterface.OnClickListener{
//            dialogInterface, i ->
//            dialogInterface.cancel()
//        })
//
//        alert.setNeutralButton("My Trade", DialogInterface.OnClickListener{
//            dialogInterface, i ->
//
//            startActivity(Intent(activity, TradeActivity::class.java))
//
//        })
//
//        alert.setView(view).create()
//        alert.show()
    }

    fun updateTip(tip: Tip){
        FirebaseDatabase.getInstance().reference
                .child("tips")
                .child(tip.id!!)
                .setValue(tip)
                .addOnCompleteListener {
                    adapter?.notifyDataSetChanged()
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

    fun editNews(id: String){
        val intent = Intent(activity, EditNewsAsTip::class.java)
        intent.putExtra("newsRef", id)
        startActivity(intent)
    }
}
