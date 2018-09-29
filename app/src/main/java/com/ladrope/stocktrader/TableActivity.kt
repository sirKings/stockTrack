package com.ladrope.stocktrader

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ladrope.stocktrader.model.Tip
import kotlinx.android.synthetic.main.activity_table.*

class TableActivity : AppCompatActivity() {

    val callList = ArrayList<Tip?>()
    var tableView: TableLayout? = null
    var expertName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table)

        expertName = intent.extras.getString("expertName")
        header.text = expertName + " tips"

        getCalls()

        progres.visibility = View.VISIBLE

        tableView = table

    }



    fun getCalls(){
        Log.e("Table", "table called")
        FirebaseDatabase.getInstance().reference
                .child("tips")
                .orderByChild("status")
                .equalTo(true)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Toast.makeText(this@TableActivity, "Could not load call, check internet connections", Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            tableView?.removeAllViews()
                            callList.clear()
                            for (i in p0.children){
                                callList.add(i.getValue(Tip::class.java))
                            }
                            //setUpTable()
                            getMore()
                        }else{

                            getMore()
                        }
                    }
                })

    }

    fun getMore(){

        Log.e("Table", "more called")
        FirebaseDatabase.getInstance().reference
                .child("tips")
                .orderByChild("status")
                .equalTo(false)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Toast.makeText(this@TableActivity, "Could not load call, check internet connections", Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            for (i in p0.children){
                                callList.add(i.getValue(Tip::class.java))
                            }
                            setUpTable()
                            progres.visibility = View.GONE
                        }
                    }
                })
    }

    fun setUpTable(){

        tableView?.setBackgroundResource(R.drawable.table_red)

        Log.e("Table", "table setup")
        val tableRow = TableRow(this)
        val entryName = TextView(this)
        val entryDate = TextView(this)
        val exitDate = TextView(this)
        val entryPrice = TextView(this)
        val exitPrice = TextView(this)
        val entryTarget = TextView(this)
        val nameLayout = LinearLayout(this)
        val dateLayout = LinearLayout(this)
        val exitDateLayout = LinearLayout(this)
        val priceLayout = LinearLayout(this)
        val exitPriceLayout = LinearLayout(this)
        val targetLayout = LinearLayout(this)

        nameLayout.setBackgroundResource(R.drawable.table_red)
        dateLayout.setBackgroundResource(R.drawable.table_red)
        priceLayout.setBackgroundResource(R.drawable.table_red)
        targetLayout.setBackgroundResource(R.drawable.table_red)
        exitDateLayout.setBackgroundResource(R.drawable.table_red)
        exitPriceLayout.setBackgroundResource(R.drawable.table_red)

        entryName.text = "Scrip"
        entryName.gravity = Gravity.CENTER
        entryName.setPadding(2,2,2,2)


        entryDate.text = "Entry Dt"
        entryDate.gravity = Gravity.CENTER
        entryDate.setPadding(2,2,2,2)

        exitDate.text = "Exit Dt"
        exitDate.gravity = Gravity.CENTER
        exitDate.setPadding(2,2,2,2)

        entryPrice.text = "Entry"
        entryPrice.gravity = Gravity.CENTER
        entryPrice.setPadding(2,2,2,2)

        exitPrice.text = "Exit"
        exitPrice.gravity = Gravity.CENTER
        exitPrice.setPadding(2,2,2,2)


        entryTarget.text = "P/L"
        entryTarget.gravity = Gravity.CENTER
        entryTarget.setPadding(2,2,2,2)

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
        val expertList = sortedList.filter { it?.expertName?.toLowerCase() == expertName?.toLowerCase()}


        for (tip in expertList){
            val tabeRow = TableRow(this)
            val etryName = TextView(this)
            val etryDate = TextView(this)
            val extDate = TextView(this)
            val etryPrice = TextView(this)
            val extPrice = TextView(this)
            val etryTarget = TextView(this)
            val nmeLayout = LinearLayout(this)
            val dteLayout = LinearLayout(this)
            val pricLayout = LinearLayout(this)
            val extDateLayout = LinearLayout(this)
            val extPriceLayout = LinearLayout(this)
            val targtLayout = LinearLayout(this)

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
                val intent = Intent(this, TipDetailActivity::class.java)
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
