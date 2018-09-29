package com.ladrope.stocktrader

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.ladrope.stocktrader.model.NotificationsModel
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.fragment_notifications.*
import kotlinx.android.synthetic.main.notification_row.view.*

class NotificationsActivity : AppCompatActivity() {


    var notesRecyclerView: RecyclerView? = null
    var adapter: NotesAdapter? = null
    var layoutManager: RecyclerView.LayoutManager? = null
    var options: FirebaseRecyclerOptions<NotificationsModel>? = null
    var mProgressBar: ProgressBar? =null
    var mErrorText: TextView? = null
    var mEmptyText: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_notifications)

        notesRecyclerView = notesRV
//        val snapHelper = SnapHelperOneByOne()
//        snapHelper.attachToRecyclerView(newsRecyclerView)

        backBtn.setOnClickListener {
            finish()
        }

        notificationStatus.setOnClickListener {
            changeNotification()
        }

        if (isAdmin){
            val simpleTouchhelper = ItemTouchHelper(simpleItemTouchCallback)
            simpleTouchhelper.attachToRecyclerView(notesRecyclerView)

        }

        mProgressBar = notesProgress
        mErrorText = notesErrorText
        mEmptyText = notesEmptyText

        notesfab.setOnClickListener {
            addNews()
        }

        if(!isAdmin){
            notesfab.hide()
        }

        val query = FirebaseDatabase.getInstance().reference.child("notifications").orderByChild("date").limitToFirst(30)
        setup(query)
    }

    fun changeNotification(){
        val builder1 = AlertDialog.Builder(this)
        builder1.setTitle(Html.fromHtml("<font color='#000000'>Disable Push Notifications?</font>"))
        builder1.setCancelable(false)

        val status = OneSignal.getPermissionSubscriptionState()
        val userId = status.subscriptionStatus.userId

        builder1.setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { dialog, id ->
                    FirebaseDatabase.getInstance().reference
                            .child("users")
                            .child(userId)
                            .setValue(null)
                            .addOnCompleteListener {
                                Toast.makeText(this@NotificationsActivity, "Notification disabled", Toast.LENGTH_SHORT).show()
                            }

                })

        builder1.setNegativeButton(
                "No",
                DialogInterface.OnClickListener { dialog, id ->
                    saveUserId()
                    Toast.makeText(this@NotificationsActivity, "Notification enabled", Toast.LENGTH_SHORT).show()

                })

        val alert = builder1.create()
        alert.show()
    }

    fun setup(query: Query){
        Log.e("setup", "started")
        mErrorText?.visibility = View.GONE
        mEmptyText?.visibility = View.GONE
        options = FirebaseRecyclerOptions.Builder<NotificationsModel>()
                .setQuery(query, NotificationsModel::class.java)
                .build()
        adapter = NotesAdapter(options!!, this)
        layoutManager = LinearLayoutManager(this)

        //set up recycler view
        notesRecyclerView!!.layoutManager = layoutManager
        notesRecyclerView!!.adapter = adapter

    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    fun addNews(){
        val addIntent = Intent(this, AddNotification::class.java)
        startActivity(addIntent)
    }

    inner class NotesAdapter(options: FirebaseRecyclerOptions<NotificationsModel>, private val context: Context): FirebaseRecyclerAdapter<NotificationsModel, NotesAdapter.ViewHolder>(options) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.notification_row, parent, false)
            return ViewHolder(view)
        }


        override fun onDataChanged() {
            super.onDataChanged()
            mProgressBar?.visibility = View.GONE
            if (adapter?.itemCount == 0) {
                mEmptyText?.visibility = View.VISIBLE
            }
        }


        override fun onError(error: DatabaseError) {
            super.onError(error)
            mErrorText?.visibility = View.VISIBLE
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int, model: NotificationsModel) {
            holder.bindItem(model)
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bindItem(tip: NotificationsModel) {
                val title = itemView.findViewById<TextView>(R.id.notesTitle)
                val msg = itemView.findViewById<TextView>(R.id.notesDesc)
                val date = itemView.findViewById<TextView>(R.id.notesDate)
                val name = itemView.notesNameSec

                title.text = tip.title
                msg.text = tip.msg
                //Picasso.with(context).load("https://firebasestorage.googleapis.com/v0/b/stocktrader-21772.appspot.com/o/profileImages%2F1531910771067C7luhWHbGqMOOQP6OZ4rFLHn0e62?alt=media&token=c503eb31-30fa-42b2-8df0-c9c5e51c5df6").into(image)
                date.text = getDate(tip.date!! * -1)
                //name.text = tip.desc
//                if(tip.link != null){
//                    readMore.setOnClickListener {
//                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://"+tip.link))
//                        startActivity(browserIntent)
//                    }
//                }else{
//                    readMore.visibility = View.GONE
//                }

//                itemView.newsShareBtn.setOnClickListener {
//                    shareNews(tip, context)
//                }

            }
        }
    }

    var simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.DOWN or ItemTouchHelper.UP) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            Toast.makeText(this@NotificationsActivity, "on Move", Toast.LENGTH_SHORT).show()
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            //Remove swiped item from list and notify the RecyclerView
            val position = viewHolder.adapterPosition
            val id = adapter?.getRef(position)
            //arrayList.remove(position)
            callDelete(id)
            //adapter?.notifyDataSetChanged()

        }
    }

    fun callDelete(position: DatabaseReference?){
        val builder1 = AlertDialog.Builder(this)
        builder1.setTitle(Html.fromHtml("<font color='#000000'>Delete item?</font>"))
        builder1.setCancelable(false)

        builder1.setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { dialog, id ->
                    FirebaseDatabase.getInstance().reference
                            .child("notifications")
                            .child(position?.key!!)
                            .setValue(null)
                            .addOnCompleteListener {
                                Toast.makeText(this@NotificationsActivity, "Deleted", Toast.LENGTH_SHORT).show()
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
