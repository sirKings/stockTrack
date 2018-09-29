package com.ladrope.stocktrader


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ladrope.stocktrader.model.Tip
import kotlinx.android.synthetic.main.fragment_closed_call.view.*


/**
 * A simple [Fragment] subclass.
 *
 */
class ClosedCallFragment : Fragment() {

    val callList = ArrayList<Tip?>()
    var tableView: TableLayout? = null
    var progres: ProgressBar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_closed_call, container, false)

        tableView = view.table

        progres = view.progresClose
        progres?.visibility = View.VISIBLE

//        if (this.callList.isEmpty()){
//            Log.e("First", "table call")
//            getCalls()
//        }else{
//
//            Log.e("second", "table call")
//            view.table.removeAllViews()
//            setUpTable()
//        }

        return view
    }

//    override fun onStart() {
//        super.onStart()
//        getCalls()
//    }

    override fun onResume() {
        super.onResume()
        getCalls()
    }


    fun getCalls(){
        Log.e("First", "get calls")
        FirebaseDatabase.getInstance().reference
                .child("tips")
                .orderByChild("status")
                .equalTo(true)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Toast.makeText(context, "Could not load call, check internet connections", Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        Log.e("table data", "Changed")
                        if (p0.exists()){
                            Log.e("First", "get calls exists")
                            tableView?.removeAllViews()
                            callList.clear()
                            for (i in p0.children){
                                callList.add(i.getValue(Tip::class.java))
                            }
                            //setUpTable()
                            getMore()
                        }else{

                            Log.e("First", "get calls exists not")
                            getMore()
                        }
                    }
                })

    }

    fun getMore(){

        Log.e("First", "get more calls")
        FirebaseDatabase.getInstance().reference
                .child("tips")
                .orderByChild("status")
                .equalTo(false)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Toast.makeText(context, "Could not load call, check internet connections", Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        Log.e("table data", "Changed")
                        if (p0.exists()){

                            Log.e("First", "get more calls exist")
                            for (i in p0.children){
                                callList.add(i.getValue(Tip::class.java))
                            }
                            setUpTable()

                        }else{
                            setUpTable()
                            Log.e("First", "get more calls exixt not")
                        }
                    }
                })
    }

    fun setUpTable(){

        tableView?.setBackgroundResource(R.drawable.table_red)

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
        entryName.setPadding(2,2,2,2)
        entryName.setTextColor(Color.WHITE)


        entryDate.text = "Entry Dt"
        entryDate.gravity = Gravity.CENTER
        entryDate.setPadding(2,2,2,2)
        entryDate.setTextColor(Color.WHITE)

        exitDate.text = "Exit Dt"
        exitDate.gravity = Gravity.CENTER
        exitDate.setPadding(2,2,2,2)
        exitDate.setTextColor(Color.WHITE)

        entryPrice.text = "Entry"
        entryPrice.gravity = Gravity.CENTER
        entryPrice.setPadding(2,2,2,2)
        entryPrice.setTextColor(Color.WHITE)

        exitPrice.text = "Exit"
        exitPrice.gravity = Gravity.CENTER
        exitPrice.setPadding(2,2,2,2)
        exitPrice.setTextColor(Color.WHITE)


        entryTarget.text = "P/L"
        entryTarget.gravity = Gravity.CENTER
        entryTarget.setPadding(2,2,2,2)
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

        val sortedList = callList.sortedWith(compareBy({it?.rank}))
        val eqWealthList = sortedList.filter { it?.expertName?.toLowerCase() == ADMIN_EXPERT  }

        for (tip in eqWealthList){
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
            etryName.setPadding(2,2,2,2)
            etryName.textSize = 12.0f


//            etryExpert.text =
//            etryExpert.gravity = Gravity.CENTER
//            etryExpert.setPadding(5,5,5,5)

            etryDate.text = "\n"+getShortDate(tip?.date)+"\n"
            etryDate.gravity = Gravity.CENTER
            etryDate.setPadding(2,2,2,2)
            etryDate.textSize = 12.0f

            extDate.text = "\n"+getShortDate(tip?.closeDate) +"\n"
            extDate.gravity = Gravity.CENTER
            extDate.setPadding(2,2,2,2)
            extDate.textSize = 12.0f

            etryPrice.text = "\n"+tip?.entryPrice+"\n"
            etryPrice.gravity = Gravity.CENTER
            etryPrice.setPadding(2,2,2,2)
            etryPrice.textSize = 12.0f

            if (tip.exitPrice != null){
                extPrice.text = "\n"+tip?.exitPrice+"\n"
            }else{
                extPrice.text = "\n \n"
            }

            extPrice.gravity = Gravity.CENTER
            extPrice.setPadding(2,2,2,2)
            extPrice.textSize = 12.0f

            var pl = "\n \n"

            if (getProfit(tip) != null){
                if (getProfit(tip)!! > 0){
                    etryTarget.setTextColor(resources!!.getColor(R.color.green_card))
                }else{
                    etryTarget.setTextColor(resources!!.getColor(R.color.red_card))
                }
                pl = "\n"+getProfit(tip)?.toInt().toString() +"\n"
            }

            etryTarget.text = pl
            etryTarget.gravity = Gravity.CENTER
            etryTarget.setPadding(2,2,2,2)
            etryTarget.textSize = 12.0f

            nmeLayout.addView(etryName)
            nmeLayout.setOnClickListener {
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

    }

}
