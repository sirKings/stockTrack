package com.ladrope.stocktrader

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.ladrope.stocktrader.model.Message
import kotlinx.android.synthetic.main.activity_read_more.*


class ReadMoreActivity : AppCompatActivity() {

    var newsRecyclerView: RecyclerView? = null
    var adapter: MessageAdapter? = null
    var layoutManager: RecyclerView.LayoutManager? = null
    var options: FirebaseRecyclerOptions<com.ladrope.stocktrader.model.Message>? = null
    var msgTxt: TextView? = null
    var sendBtn: ImageButton? = null
    var where = ""
    var tipId = ""
    //var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_more)

        newsRecyclerView = msgRV



        backBtn.setOnClickListener {
            finish()
        }
        backBtn.setColorFilter(resources.getColor(R.color.colorPrimaryDark))

        if (isAdmin){
            val simpleTouchhelper = ItemTouchHelper(simpleItemTouchCallback)
            simpleTouchhelper.attachToRecyclerView(newsRecyclerView)

        }
        val info = intent.getStringExtra("where")

        if (info != null && info == "forum"){
            where  = "forum"
            header.text = "Forum"
            //progressBar?.visibility = View.VISIBLE
        }else{
            where = "expertComments"
            if (!isAdmin){
                messageBlock.visibility = View.GONE
            }
            tipId = intent.getStringExtra("tipId")
            header.text = "Read more"
//            if (selectedTip == null){
//                getSelectedTip(tipId)
//            }else{
//                //progressBar?.visibility = View.GONE
//            }
        }
        //progressBar = readMoreProgress


        msgTxt = msgET
        sendBtn = msgSendBtn

        sendBtn?.setOnClickListener {
            sendMessage()
        }
        var query: Query? = null

        if (tipId == ""){
            query = FirebaseDatabase.getInstance().reference.child(where).orderByChild("date")
        }else{
            query = FirebaseDatabase.getInstance().reference
                    .child("tips")
                    .child(tipId)
                    .child(where)
                    .orderByChild("date")
        }
        setup(query)
    }

//    fun getSelectedTip(id: String){
//        FirebaseDatabase.getInstance().reference
//                .child("tips")
//                .child(id)
//                .addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onCancelled(p0: DatabaseError) {
//                        //finish()
//                        Toast.makeText(this@ReadMoreActivity, "Can't find any information, check network connection", Toast.LENGTH_SHORT).show()
//                        finish()
//                    }
//
//                    override fun onDataChange(p0: DataSnapshot) {
//                        if (p0.exists()) {
//                            selectedTip = p0.getValue(Tip::class.java)
//                            //progressBar?.visibility = View.GONE
//                        }else{
//                            Toast.makeText(this@ReadMoreActivity, "Can't find any information, check network connection", Toast.LENGTH_SHORT).show()
//                            finish()
//                        }
//                    }
//                })
//    }

    fun sendMessage(){
        if (!msgTxt?.text?.isEmpty()!!){
            sendBtn?.isClickable = false
            val msg = Message()
            msg.date = System.currentTimeMillis()
            msg.msg = msgTxt?.text.toString()
            if(isAdmin){
                msg.title = "StockTrack"
            }else{
                msg.title = FirebaseAuth.getInstance().currentUser?.displayName!!.split(" ")[0]
            }
            var key: String? = null

            if (tipId == ""){
                key = FirebaseDatabase.getInstance().reference
                        .child(where)
                        .push()
                        .key
                msg.id = key
                FirebaseDatabase.getInstance().reference
                        .child(where)
                        .child(key!!)
                        .setValue(msg).addOnCompleteListener {
                            msgTxt?.text = ""
                            sendBtn?.isClickable = true
                        }.addOnFailureListener {
                            sendBtn?.isClickable = true
                            Toast.makeText(this, "Could not send message, check internet connection", Toast.LENGTH_SHORT).show()
                        }
            }else{
                key = FirebaseDatabase.getInstance().reference
                        .child("tips")
                        .child(tipId)
                        .child(where)
                        .push()
                        .key
                msg.id = key

                if(selectedTip!!.expertComments == null){
                    val eptC = ArrayList<Message>()
                    eptC.add(msg)
                    selectedTip?.expertComments = eptC
                }else{
                    selectedTip?.expertComments?.add(msg)
                }

                FirebaseDatabase.getInstance().reference
                        .child("tips")
                        .child(selectedTip?.id!!)
                        .setValue(selectedTip).addOnCompleteListener {
                            msgTxt?.text = ""
                            sendBtn?.isClickable = true
                        }.addOnFailureListener {
                            sendBtn?.isClickable = true
                            Toast.makeText(this, "Could not send message, check internet connection", Toast.LENGTH_SHORT).show()
                        }
            }


        }
    }

    fun setup(query: Query){
        Log.e("setup", "started")

        options = FirebaseRecyclerOptions.Builder<com.ladrope.stocktrader.model.Message>()
                .setQuery(query, com.ladrope.stocktrader.model.Message::class.java)
                .build()
        adapter = MessageAdapter(options!!, this)
        layoutManager = LinearLayoutManager(this)

        //set up recycler view
        newsRecyclerView!!.layoutManager = layoutManager
        newsRecyclerView!!.adapter = adapter

    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        selectedTip = null
    }


    inner class MessageAdapter(options: FirebaseRecyclerOptions<com.ladrope.stocktrader.model.Message>, private val context: Context): FirebaseRecyclerAdapter<com.ladrope.stocktrader.model.Message, MessageAdapter.ViewHolder>(options) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.message_row, parent, false)
            return ViewHolder(view)
        }


        override fun onDataChanged() {
            super.onDataChanged()

            if (adapter?.itemCount!! > 3) {
                newsRecyclerView?.smoothScrollToPosition(adapter?.itemCount!! - 1)
            }
        }


        override fun onBindViewHolder(holder: ViewHolder, position: Int, model: com.ladrope.stocktrader.model.Message) {
            holder.bindItem(model)
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bindItem(tip: com.ladrope.stocktrader.model.Message) {
                val title = itemView.findViewById<TextView>(R.id.messageSender)
                val msg = itemView.findViewById<TextView>(R.id.messageMsg)
                val date = itemView.findViewById<TextView>(R.id.messageTime)

                title.text = ""
                msg.text = tip.msg
                date.text = getDate(tip.date!!)
            }
        }
    }

    var simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.DOWN or ItemTouchHelper.UP) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            Toast.makeText(this@ReadMoreActivity, "on Move", Toast.LENGTH_SHORT).show()
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            //Remove swiped item from list and notify the RecyclerView
            val position = viewHolder.adapterPosition
            //arrayList.remove(position)
            callDelete(position)
            //adapter?.notifyDataSetChanged()

        }
    }

    fun callDelete(position: Int){
        val builder1 = AlertDialog.Builder(this)
        builder1.setTitle(Html.fromHtml("<font color='#000000'>Delete item?</font>"))
        builder1.setCancelable(false)

        builder1.setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { dialog, id ->
                    if (tipId == ""){

                    }else{
                        selectedTip?.expertComments?.removeAt(position)
                        FirebaseDatabase.getInstance().reference
                                .child("tips")
                                .child(tipId)
                                .setValue(selectedTip)
                                .addOnCompleteListener {
                                    Toast.makeText(this@ReadMoreActivity, "Deleted", Toast.LENGTH_SHORT).show()
                                }
                    }
                })

        builder1.setNegativeButton(
                "No",
                DialogInterface.OnClickListener { dialog, id ->

                    adapter?.notifyDataSetChanged()
                })

        builder1.setNeutralButton(
                "Edit",
                DialogInterface.OnClickListener { dialog, id ->
                    msgTxt?.setText(selectedTip?.expertComments!![position].msg)
                    if (tipId == ""){

                    }else{
                        selectedTip?.expertComments?.removeAt(position)
                        FirebaseDatabase.getInstance().reference
                                .child("tips")
                                .child(tipId)
                                .setValue(selectedTip)
                                .addOnCompleteListener {
                                    //Toast.makeText(this@ReadMoreActivity, "Deleted", Toast.LENGTH_SHORT).show()
                                }
                    }
                }
        )

        val alert = builder1.create()
        alert.show()
    }
}
