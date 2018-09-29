package com.ladrope.stocktrader


import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
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
import kotlinx.android.synthetic.main.activity_read_more.view.*


/**
 * A simple [Fragment] subclass.
 *
 */
class ChatFragment : Fragment() {

    var newsRecyclerView: RecyclerView? = null
    var adapter: MessageAdapter? = null
    var layoutManager: RecyclerView.LayoutManager? = null
    var options: FirebaseRecyclerOptions<Message>? = null
    var msgTxt: TextView? = null
    var sendBtn: ImageButton? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        newsRecyclerView = view.msgRV
        if (isAdmin){
            val simpleTouchhelper = ItemTouchHelper(simpleItemTouchCallback)
            simpleTouchhelper.attachToRecyclerView(newsRecyclerView)

        }

        msgTxt = view.msgET
        sendBtn = view.msgSendBtn

        sendBtn?.setOnClickListener {
            sendMessage()
        }
        var query: Query? = null

        query = FirebaseDatabase.getInstance().reference.child("forum").orderByChild("date")

        setup(query)

        return view
    }

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
            val key = FirebaseDatabase.getInstance().reference
                        .child("forum")
                        .push()
                        .key
                msg.id = key
                FirebaseDatabase.getInstance().reference
                        .child("forum")
                        .child(key!!)
                        .setValue(msg).addOnCompleteListener {
                            msgTxt?.text = ""
                            sendBtn?.isClickable = true
                        }.addOnFailureListener {
                            sendBtn?.isClickable = true
                            Toast.makeText(context, "Could not send message, check internet connection", Toast.LENGTH_SHORT).show()
                        }

        }
    }

    fun setup(query: Query){
        Log.e("setup", "started")

        options = FirebaseRecyclerOptions.Builder<com.ladrope.stocktrader.model.Message>()
                .setQuery(query, com.ladrope.stocktrader.model.Message::class.java)
                .build()
        adapter = MessageAdapter(options!!, context!!)
        layoutManager = LinearLayoutManager(context)

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


    inner class MessageAdapter(options: FirebaseRecyclerOptions<com.ladrope.stocktrader.model.Message>, private val context: Context): FirebaseRecyclerAdapter<Message, MessageAdapter.ViewHolder>(options) {

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

                title.text = tip.title
                msg.text = tip.msg
                date.text = getDate(tip.date!!)
            }
        }
    }

    var simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.DOWN or ItemTouchHelper.UP) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            //Toast.makeText(this@ReadMoreActivity, "on Move", Toast.LENGTH_SHORT).show()
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            //Remove swiped item from list and notify the RecyclerView
            val position = viewHolder.adapterPosition
            //arrayList.remove(position)
            callDelete(adapter?.getItem(position))
            //adapter?.notifyDataSetChanged()

        }
    }

    fun callDelete(msg: Message?){
        val builder1 = AlertDialog.Builder(context!!)
        builder1.setTitle(Html.fromHtml("<font color='#000000'>Delete item?</font>"))
        builder1.setCancelable(false)

        builder1.setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { dialog, id ->
                        FirebaseDatabase.getInstance().reference
                                .child("forum")
                                .child(msg?.id!!)
                                .setValue(null)
                                .addOnCompleteListener {
                                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                }
                })

        builder1.setNegativeButton(
                "No",
                DialogInterface.OnClickListener { dialog, id ->

                    adapter?.notifyDataSetChanged()
                })

        val alert = builder1.create()
        alert.show()
    }


}
