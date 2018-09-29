package com.ladrope.stocktrader

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ladrope.stocktrader.model.Expert
import com.ladrope.stocktrader.model.NotificationsModel
import com.ladrope.stocktrader.model.Tip
import kotlinx.android.synthetic.main.activity_add_tip.*
import java.util.*

class AddTipActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    val segmentList = arrayListOf<String>("Cash", "Future", "CE", "PE")
    val callList = arrayListOf<String>("Buy", "Sell", "Hold", "Accumulate")
    val periodList = arrayListOf<String>("Intraday", "BTST", "3 days", "1 week", "2 weeks", "1 month", "3 months", "6 months", "Positional", "Long Term", "1 year")
    val expertList = arrayListOf<String>()
    val expertObjectList = ArrayList<Expert>()

    var sgtSpinner: Spinner? = null
    var calSpinner: Spinner? = null
    var prdSpinner: Spinner? = null
    var eptSpinner: Spinner? = null

    var tipTgt: TextView? = null
    var tipEntryPrice: TextView? = null
    var tipStopLoss: TextView? = null
    var tipNameSec: TextView? = null
    var tipLot: TextView? = null

    var sgt: String? = null
    var cal: String? = null
    var prd: String? = null
    var ept: String? = null

    var expertId: Int? = null
    var eptAdapter: ArrayAdapter<String>? = null

    var isEdit = false
    var notEdited = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_tip)

        isEdit = intent.getBooleanExtra("isEdit", false)



        getExperts()

        sgtSpinner = segmentSpinner
        calSpinner = callSpinner
        prdSpinner = periodSpinner
        eptSpinner = expetName


        sgtSpinner?.onItemSelectedListener = this
        calSpinner?.onItemSelectedListener = this
        prdSpinner?.onItemSelectedListener = this
        eptSpinner?.onItemSelectedListener = this

        tipTgt = targetAddTip
        tipEntryPrice = entryPrice
        tipNameSec = nameSec
        tipStopLoss = stopLossAddTip
        tipLot = lotSizeAddTip


        // Create an ArrayAdapter using a simple spinner layout and languages array
        val sgtAdapter = ArrayAdapter(this, R.layout.spiner_row, segmentList)
        // Set layout to use when the list of choices appear
        sgtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        sgtSpinner?.adapter = sgtAdapter
        sgtSpinner?.setSelection(-1)

        // Create an ArrayAdapter using a simple spinner layout and languages array
        val calAdapter = ArrayAdapter(this, R.layout.spiner_row, callList)
        // Set layout to use when the list of choices appear
        calAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        calSpinner?.adapter = calAdapter
        calSpinner?.setSelection(-1)

        // Create an ArrayAdapter using a simple spinner layout and languages array
        val prdAdapter = ArrayAdapter(this, R.layout.spiner_row, periodList)
        // Set layout to use when the list of choices appear
        prdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        prdSpinner?.adapter = prdAdapter
        prdSpinner?.setSelection(-1)

        // Create an ArrayAdapter using a simple spinner layout and languages array
        eptAdapter = ArrayAdapter(this, R.layout.spiner_row, expertList)
        // Set layout to use when the list of choices appear
        eptAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        eptSpinner?.adapter = eptAdapter
        eptSpinner?.setSelection(-1)

        if (isEdit){
            setupTip()
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    fun setupTip(){
        tipTgt?.text = selectedTip?.target
        tipEntryPrice?.text = selectedTip?.entryPrice
        tipNameSec?.text = selectedTip?.nameSec
        tipStopLoss?.text = selectedTip?.stopLoss
        if (selectedTip?.lotSize != null){
            tipLot?.text =  "" +selectedTip?.lotSize!!
        }
        if (selectedTip?.margin != null){
            marginAddTip.setText("" + selectedTip?.margin)
        }
        if (selectedTip?.canTrade != null){
            canTrade.isChecked = selectedTip?.canTrade!!
        }

    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        when (p0?.id) {
            R.id.segmentSpinner -> {
                Log.e("Adapter", "1")
                sgtSpinner?.setSelection(p2)
                sgt = segmentList[p2]
                if (isEdit && notEdited){
                    val index = segmentList.indexOf(selectedTip?.segment)
                    sgtSpinner?.setSelection(index)
                    sgt = segmentList[index]
                }
            }
            R.id.periodSpinner -> {

                Log.e("Adapter", "2")
                prdSpinner?.setSelection(p2)
                prd = periodList[p2]
                if (isEdit && notEdited){
                    val index = periodList.indexOf(selectedTip?.period)
                    prdSpinner?.setSelection(index)
                    prd = periodList[index]
                    notEdited = false
                }
            }
            R.id.expetName -> {
                Log.e("Adapter", "3")
                eptSpinner?.setSelection(p2)
                ept = expertList[p2]
                expertId = p2

            }
            R.id.callSpinner -> {

                Log.e("Adapter", "4")
                calSpinner?.setSelection(p2)
                cal = callList[p2]
                if (isEdit && notEdited){
                    val index = callList.indexOf(selectedTip?.call)
                    calSpinner?.setSelection(index)
                    cal = callList[index]
                }
            }
            else -> { // Note the block

                Log.e("Adapter", "5")
            }
        }
    }

    fun addWithNotification(view: View){
        //view.isClickable = false
        verifyTip(true)
    }

    fun addWithoutNotification(view: View){
        //view.isClickable = false
        verifyTip(false)
    }

    fun verifyTip(withNotify: Boolean){
        if (prd == null){
            Toast.makeText(this, "Please select period", Toast.LENGTH_SHORT).show()
        }else if (sgt == null){
            Toast.makeText(this, "Please select segment", Toast.LENGTH_SHORT).show()
        }else if (cal == null){
            Toast.makeText(this, "Please select call type", Toast.LENGTH_SHORT).show()
        }else if (ept == null){
            Toast.makeText(this, "Please select expert", Toast.LENGTH_SHORT).show()
        }else if (tipEntryPrice?.text == "" || tipNameSec?.text == "" || tipStopLoss?.text == "" || tipTgt?.text == ""){
            Toast.makeText(this, "Please provide all details", Toast.LENGTH_SHORT).show()
        }else if(tipTgt?.text.isNullOrEmpty() || tipStopLoss?.text.isNullOrEmpty() || tipNameSec?.text.isNullOrEmpty() || tipEntryPrice?.text.isNullOrEmpty()){
            Toast.makeText(this, "Please enter all information", Toast.LENGTH_SHORT).show()
        }else{
            if (isEdit){
                updateTip(withNotify)
            }else{
                createTip(withNotify)
            }

        }
    }

    fun updateTip(withNotify: Boolean){
        selectedTip?.expertName = ept
        selectedTip?.entryPrice = tipEntryPrice?.text.toString()
        selectedTip?.canTrade = canTrade.isChecked
        selectedTip?.margin = marginAddTip.text.toString()
        selectedTip?.lotSize = tipLot?.text.toString().toInt()
        selectedTip?.target = tipTgt?.text.toString()
        selectedTip?.stopLoss = tipStopLoss?.text.toString()
        selectedTip?.nameSec = tipNameSec?.text.toString()
        selectedTip?.call = cal
        selectedTip?.segment = sgt
        selectedTip?.period = prd

        FirebaseDatabase.getInstance().reference
                .child("tips")
                .child(selectedTip?.id!!)
                .setValue(selectedTip)
                .addOnCompleteListener {
                    Toast.makeText(this, "Tip updated", Toast.LENGTH_SHORT).show()
                    if (withNotify) {
                        notifyUser(selectedTip!!, selectedTip?.call + " " + selectedTip?.nameSec?.toUpperCase() + " " + selectedTip?.segment?.toUpperCase() + " @ " + selectedTip?.entryPrice + " for a target of " + selectedTip?.target)
                    }
                }
    }

    fun createTip(withNotify: Boolean){
        val tip = Tip()
        tip.image = expertObjectList[expertId!!].image
        tip.expertName = ept
        tip.segment = sgt
        tip.nameSec = tipNameSec?.text.toString()
        tip.period = prd
        tip.stopLoss = tipStopLoss?.text.toString()
        tip.target = tipTgt?.text.toString()
        tip.entryPrice = tipEntryPrice?.text.toString()
        tip.call = cal
        tip.date = System.currentTimeMillis()
        tip.rank = tip.date!! * -1
        tip.status = true
        tip.isNewstype = false
        tip.lotSize = tipLot?.text.toString().toInt()
        tip.margin = marginAddTip.text.toString()
        tip.canTrade = canTrade.isChecked

        submitTip(withNotify, tip)
    }

    fun submitTip(withNotify: Boolean, tip: Tip){

        val key = FirebaseDatabase.getInstance().reference.child("tips").push().key

        tip.id = key

        FirebaseDatabase.getInstance().reference
                .child("tips")
                .child(key!!)
                .setValue(tip).addOnCompleteListener {
                    Toast.makeText(this@AddTipActivity, "Tip added successfully", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this@AddTipActivity, "Tip not added, check your internet connection", Toast.LENGTH_SHORT).show()
                }

        if (withNotify){ notifyUsers(tip) }
    }

    fun notifyUsers(tip: Tip){
        val news = NotificationsModel()
        news.title = tip.expertName
        news.msg =  tip.call +" "+tip.nameSec?.toUpperCase() + " "+tip.segment?.toUpperCase() +" @ "+tip.entryPrice +" for a target of "+tip.target
        news.date = System.currentTimeMillis() * -1

        FirebaseDatabase.getInstance().reference.child("notifications")
                .push()
                .setValue(news)
                .addOnCompleteListener {
                    //Toast.makeText(this@AddNewsActivity, "Notifications posted", Toast.LENGTH_SHORT).show()
                    //this@AddNewsActivity.finish()
                }.addOnFailureListener {
                    Toast.makeText(this@AddTipActivity, "Notifications posting failed, check your internet connection", Toast.LENGTH_SHORT).show()
                }
        //if(tip.expertName?.toLowerCase() == ADMIN_EXPERT){
            notifyUser(tip, news.msg!!)
        //}

    }

    fun notifyUser(tip: Tip, str: String){
        sendNotification(str, "Stock Track", "TIPS", tip.id!!)
    }

    fun getExperts(){
        FirebaseDatabase.getInstance().reference
                .child("experts")
                .orderByChild("title")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Toast.makeText(this@AddTipActivity, "Could not fetch experts, Check your internet connections", Toast.LENGTH_SHORT).show()
                        notEdited = false
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                       if (p0.exists()){
                           for (i in p0.children){
                               val expert = i.getValue(Expert::class.java)
                               expertList.add(expert?.title!!)
                               expertObjectList.add(expert)
                               eptAdapter?.notifyDataSetChanged()
                           }
                           if (isEdit){
                               val index = expertList.indexOf(selectedTip?.expertName)
                               if (index != -1){
                                   eptSpinner?.setSelection(index)
                                   ept = expertList[index]
                                   expertId = index
                                   notEdited = false
                               }
                           }
                       }
                    }

                })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        selectedTip = null
    }
}
