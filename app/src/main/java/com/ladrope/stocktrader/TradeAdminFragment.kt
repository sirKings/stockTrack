package com.ladrope.stocktrader


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ladrope.stocktrader.model.Tip
import kotlinx.android.synthetic.main.fragment_trade_admin.view.*


/**
 * A simple [Fragment] subclass.
 *
 */
class TradeAdminFragment : Fragment() {

    //val callList = ArrayList<Tip?>()
    var tableView: TableLayout? = null
    var progres: ProgressBar? = null
    var tipListEmpty = false

    interface Listener {
        fun updateAccount(balance: String)
    }

    private var mListener: Listener? = null

    fun setListener(listener: Listener) {
        mListener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_trade_admin, container, false)

        tableView = view.table

        progres = view.progresOpen
        progres?.visibility = View.VISIBLE

        //setUpTable()

        return view
    }

    override fun onResume() {
        super.onResume()
        tableView?.removeAllViews()
        setUpTable()
    }


    fun setUpTable(){

        tableView?.setBackgroundResource(R.drawable.table_trade_background)

        progres?.visibility = View.GONE
        val tableRow = TableRow(activity)
        val entryName = TextView(activity)
        val entryDate = TextView(activity)
        val exitDate = TextView(activity)
        val entryPrice = TextView(activity)
        val exitPrice = TextView(activity)
        val entryTarget = TextView(activity)
        val nameLayout = LinearLayout(activity)
        val dateLayout = LinearLayout(activity)
        val exitDateLayout = LinearLayout(activity)
        val priceLayout = LinearLayout(activity)
        val exitPriceLayout = LinearLayout(activity)
        val targetLayout = LinearLayout(activity)

        nameLayout.setBackgroundResource(R.drawable.table_red)
        nameLayout.setPadding(5,0,0,0)

        dateLayout.setBackgroundResource(R.drawable.table_red)
        priceLayout.setBackgroundResource(R.drawable.table_red)
        targetLayout.setBackgroundResource(R.drawable.table_red)
        exitDateLayout.setBackgroundResource(R.drawable.table_red)
        exitPriceLayout.setBackgroundResource(R.drawable.table_red)

        entryName.text = "Scrip"
        entryName.gravity = Gravity.CENTER
        entryName.setPadding(2,2,0,2)
        entryName.setTextColor(Color.WHITE)


        entryDate.text = "Entry/Exit Dt"
        entryDate.gravity = Gravity.CENTER
        entryDate.setPadding(0,2,0,2)
        entryDate.setTextColor(Color.WHITE)

        exitDate.text = "Entry/Exit"
        exitDate.gravity = Gravity.CENTER
        exitDate.setPadding(0,2,0,2)
        exitDate.setTextColor(Color.WHITE)

        entryPrice.text = "Qty"
        entryPrice.gravity = Gravity.CENTER
        entryPrice.setPadding(0,2,0,2)
        entryPrice.setTextColor(Color.WHITE)

        exitPrice.text = "Invt"
        exitPrice.gravity = Gravity.CENTER
        exitPrice.setPadding(0,2,0,2)
        exitPrice.setTextColor(Color.WHITE)

        entryTarget.text = "P/L"
        entryTarget.gravity = Gravity.CENTER
        entryTarget.setPadding(0,2,0,2)
        entryTarget.setTextColor(Color.WHITE)

        nameLayout.addView(entryName)
        dateLayout.addView(entryDate)
        exitDateLayout.addView(exitDate)
        priceLayout.addView(entryPrice)
        exitPriceLayout.addView(exitPrice)
        targetLayout.addView(entryTarget)

        tableRow.addView(nameLayout)
        tableRow.addView(dateLayout)
        tableRow.addView(exitDateLayout)
        tableRow.addView(priceLayout)
        tableRow.addView(exitPriceLayout)
        tableRow.addView(targetLayout)

        tableView?.addView(tableRow)

        val sortedList = tipList.sortedWith(compareBy({it.rank}))
        val adminList = sortedList.filter { it?.expertName?.toLowerCase() == ADMIN_EXPERT  }

        for (tip in adminList){
            val tabeRow = TableRow(activity)
            val etryName = TextView(activity)
            val etryDate = TextView(activity)
            val extDate = TextView(activity)
            val etryPrice = TextView(activity)
            val extPrice = TextView(activity)
            val etryTarget = TextView(activity)
            val nmeLayout = LinearLayout(activity)
            val dteLayout = LinearLayout(activity)
            val pricLayout = LinearLayout(activity)
            val extDateLayout = LinearLayout(activity)
            val extPriceLayout = LinearLayout(activity)
            val targtLayout = LinearLayout(activity)

            nmeLayout.setBackgroundResource(R.drawable.table_clear)
            nmeLayout.setPadding(5,0,0,0)

            dteLayout.setBackgroundResource(R.drawable.table_clear)
            pricLayout.setBackgroundResource(R.drawable.table_clear)
            targtLayout.setBackgroundResource(R.drawable.table_clear)
            extDateLayout.setBackgroundResource(R.drawable.table_clear)
            extPriceLayout.setBackgroundResource(R.drawable.table_clear)

            val expertNm = tip?.expertName?.split(" ")
            val eptNm = expertNm!![0]

            if(tip.call == "Sell"){
                etryName.text = Html.fromHtml("<font color='#ff0000'>"+tip.call+"</font><br><font color='#000000' style='font-weight:bold'>"+tip.nameSec?.toUpperCase()+"</font><br><font color='#000000'>by "+eptNm+"</font>")
            }else{
                etryName.text = Html.fromHtml("<font color='#00ff00'>"+tip.call+"</font><br><font color='#000000' style='font-weight:bold'>"+tip.nameSec?.toUpperCase()+"</font><br><font color='#000000'>by "+eptNm+"</font>")
            }

            //etryName.text = tip?.call + "\n" + tip?.nameSec +"\nBy "+eptNm
            //etryName.text = Html.fromHtml("<font color='#000000'>Target Met?</font>")
            //etryName.setTextColor(resources!!.getColor(R.color.green_card))
            //etryName.gravity = Gravity.CENTER
            etryName.setPadding(0,2,0,2)
            etryName.textSize = 12.0f


//            etryExpert.text =
//            etryExpert.gravity = Gravity.CENTER
//            etryExpert.setPadding(5,5,5,5)

            etryDate.text =  "\n"+getShortDate(tip?.date)+"/"+getShortDate(tip?.closeDate)
            etryDate.gravity = Gravity.CENTER
            etryDate.setPadding(0,2,0,2)
            etryDate.textSize = 12.0f

            var exp = ""
            if (tip.exitPrice != null){
                exp  = tip.exitPrice!!
            }else{
                exp = ""
            }

            extDate.text = "\n"+tip?.entryPrice+"/"+exp+"\n"
            extDate.gravity = Gravity.CENTER
            extDate.setPadding(0,2,0,2)
            extDate.textSize = 12.0f

            etryPrice.text = "\n"+tip.lotSize+"\n"
            etryPrice.gravity = Gravity.CENTER
            etryPrice.setPadding(0,2,0,2)
            etryPrice.textSize = 12.0f


            extPrice.text = "\n"+tip.margin+"\n"
            extPrice.gravity = Gravity.CENTER
            extPrice.setPadding(0,2,0,2)
            extPrice.textSize = 12.0f

            var pl = "\n \n"

            if (getProfit(tip) != null){
                if (getProfit(tip)!! > 0){
                    etryTarget.setTextColor(resources!!.getColor(R.color.green_card))
                }else{
                    etryTarget.setTextColor(resources!!.getColor(R.color.red_card))
                }
                pl = "\n"+getProfit(tip)?.toInt().toString()+"\n"
            }

            etryTarget.text = pl
            etryTarget.gravity = Gravity.CENTER
            etryTarget.setPadding(0,2,0,2)
            etryTarget.textSize = 12.0f

            nmeLayout.addView(etryName)
            nmeLayout.setOnClickListener {
                //deleteTip(context, tip)
                selectedTip = tip
                val intent = Intent(context, TipDetailActivity::class.java)
                intent.putExtra("tipId", tip.id)
                startActivity(intent)
            }
            //nmeLayout.addView(etryExpert, 1)
            dteLayout.addView(etryDate)
            pricLayout.addView(etryPrice)
            targtLayout.addView(etryTarget)
            extDateLayout.addView(extDate)
            extPriceLayout.addView(extPrice)

            tabeRow.addView(nmeLayout)
            tabeRow.addView(dteLayout)
            tabeRow.addView(extDateLayout)
            tabeRow.addView(pricLayout)
            tabeRow.addView(extPriceLayout)
            tabeRow.addView(targtLayout)

            tableView?.addView(tabeRow)
        }
        val totalRow = TableRow(activity)
        val textLayout = LinearLayout(activity)
        val totalLayout = LinearLayout(activity)
        val totalText = TextView(activity)
        val total1Layout = LinearLayout(activity)
        val total2Layout = LinearLayout(activity)
        val total3Layout = LinearLayout(activity)
        val total4Layout = LinearLayout(activity)
        val total = TextView(activity)

        totalText.text = "Total Profit"
        totalText.gravity = Gravity.CENTER
        totalText.setPadding(2,2,2,2)
        totalText.textSize = 12.0f

        total.text = getProfitWithList(adminList).toString()
        total.gravity = Gravity.CENTER
        total.setPadding(2,2,2,2)
        total.textSize = 12.0f

        textLayout.setBackgroundResource(R.drawable.table_clear)
        totalLayout.setBackgroundResource(R.drawable.table_clear)

        textLayout.addView(totalText)
        totalLayout.addView(total)

        totalRow.addView(textLayout)
        totalRow.addView(total1Layout)
        totalRow.addView(total2Layout)
        totalRow.addView(total3Layout)
        totalRow.addView(total4Layout)

        totalRow.addView(totalLayout)

        tableView?.addView(totalRow)

    }

    fun deleteTip(context: Context?, tip: Tip){
        val builder1 = AlertDialog.Builder(context!!)
        builder1.setTitle(Html.fromHtml("<font color='#000000'>Delete this trade?</font>"))
        builder1.setCancelable(true)

        builder1.setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { dialog, id ->
                    removeTip(context,tip)
                    dialog.cancel()
                })

        builder1.setNegativeButton(
                "No",
                DialogInterface.OnClickListener { dialog, id ->
                    dialog.cancel()
                })

        val alert = builder1.create()
        alert.show()
    }

    fun removeTip(context: Context, tip: Tip?){
        FirebaseDatabase.getInstance().reference
                .child("profiles")
                .child(FirebaseAuth.getInstance().uid!!)
                .child("trades")
                .child(tip!!.id!!)
                .setValue(null)
                .addOnCompleteListener {
                    Toast.makeText(context, "Trade deleted", Toast.LENGTH_SHORT).show()
                    reloadTable()
                }
    }

    fun reloadTable(){
        Handler().postDelayed({
            tableView?.removeAllViews()
            //progres?.visibility = View.VISIBLE
            mListener?.updateAccount(""+(200000 - getAllTradeMargin()))
            setUpTable()
        }, 1000)

    }



}
