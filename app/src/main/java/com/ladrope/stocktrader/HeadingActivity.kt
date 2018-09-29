package com.ladrope.stocktrader

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.ladrope.stocktrader.model.Expert
import kotlinx.android.synthetic.main.activity_heading.*
import kotlinx.android.synthetic.main.expert_row.view.*

class HeadingActivity : AppCompatActivity() {


    var exptRecyclerView: RecyclerView? = null
    var adapter: ExpertAdapter? = null
    var layoutManager: RecyclerView.LayoutManager? = null
    var options: FirebaseRecyclerOptions<Expert>? = null
    var imageUrl: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heading)

        exptRecyclerView = expertRV

        val query = FirebaseDatabase.getInstance().reference.child("headings").orderByChild("title")
        setup(query)
    }

    fun setup(query: Query){
        Log.e("setup", "started")
        options = FirebaseRecyclerOptions.Builder<Expert>()
                .setQuery(query, Expert::class.java)
                .build()
        adapter = ExpertAdapter(options!!, this)
        layoutManager = LinearLayoutManager(this)

        //set up recycler view
        exptRecyclerView!!.layoutManager = layoutManager
        exptRecyclerView!!.adapter = adapter

    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }


    fun addExpertImage(view: View){
        Log.e("Image btn", "Clicked bro")
        //imagePermission()
    }

    fun saveExpert(view: View){
        if (expertNameEdt.text.isEmpty()){
            Toast.makeText(this, "Please enter news heading", Toast.LENGTH_SHORT).show()
        }else{
            val expert = Expert()
            expert.image = imageUrl
            expert.title = expertNameEdt.text.toString()

            val key = FirebaseDatabase.getInstance().reference.child("headings").push().key

            expert.id = key

            FirebaseDatabase.getInstance().reference.child("headings")
                    .child(key!!)
                    .setValue(expert)
                    .addOnCompleteListener {
                        Toast.makeText(this@HeadingActivity, "News Heading added", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this@HeadingActivity, "Failed to save heading, check your network connections", Toast.LENGTH_SHORT).show()
                    }
        }
    }


    inner class ExpertAdapter(options: FirebaseRecyclerOptions<Expert>, private val context: Context): FirebaseRecyclerAdapter<Expert, ExpertAdapter.ViewHolder>(options) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.expert_row, parent, false)
            return ViewHolder(view)
        }


//        override fun onDataChanged() {
//            super.onDataChanged()
//            if (adapter?.itemCount == 0) {
//                mEmptyText?.visibility = View.VISIBLE
//            }
//        }


//        override fun onError(error: DatabaseError) {
//            super.onError(error)
//
//        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Expert) {
            holder.bindItem(model)
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bindItem(ept: Expert) {
                val name = itemView.findViewById<TextView>(R.id.expertNameRow)
                //val expertImage = itemView.findViewById<ImageView>(R.id.expertImageRow)
                val deleteBtn = itemView.deleteExpert

                name.text = ept.title
                //Picasso.with(context).load(ept.image).into(expertImage)

                deleteBtn.setOnClickListener {
                    deleteExp(ept)
                }
            }
        }
    }




    fun deleteExp(exp: Expert){
        FirebaseDatabase.getInstance().reference
                .child("headings")
                .child(exp.id!!)
                .setValue(null)
    }
}
