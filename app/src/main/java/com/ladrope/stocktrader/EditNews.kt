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
import com.ladrope.stocktrader.model.NewsModel
import com.ladrope.stocktrader.model.NotificationsModel
import com.ladrope.stocktrader.model.Tip
import kotlinx.android.synthetic.main.activity_add_news.*

class EditNews : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    var titleList = ArrayList<String>()
    var nwsMsg: TextView? = null
    var nwsTitle: Spinner? = null
    var ttl: String? = null

    var titleAdapter: ArrayAdapter<String>? = null
    val titleObjectList = java.util.ArrayList<Expert>()
    var newsKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_news)

        nwsMsg = addNewsMgs
        nwsTitle = addNewsTitle

        nwsMsg?.setText(selectedNews?.msg)

        newsKey = intent.getStringExtra("newsRef")

        //titleList.add(selectedNews?.title!!)

        nwsTitle?.onItemSelectedListener = this

        titleAdapter = ArrayAdapter(this, R.layout.spiner_row, titleList)
        // Set layout to use when the list of choices appear
        titleAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        nwsTitle?.adapter = titleAdapter
        nwsTitle?.setSelection(0)

        getNewsTitle()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        Log.e("Nothing", "Selected")
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        Log.e("Adapter", "1")
        nwsTitle?.setSelection(p2)
        ttl = titleList[p2]
    }

    fun postWithNotification(view: View){
        view.isClickable = false
        postNews(true)
    }

    fun postWithoutNotification(view: View){
        view.isClickable = false
        postNews(false)
    }

    fun postNews(withNofify: Boolean){
        if (nwsMsg?.text.isNullOrEmpty()){
            Toast.makeText(this, "Please enter news details", Toast.LENGTH_SHORT).show()
        }else {
            val news = NewsModel()
            //news.image = imageUrl
            news.title = ttl
            news.msg = nwsMsg?.text.toString()
            news.date = System.currentTimeMillis() * -1
            //postAsTip()
//
//            if (!nwsLink?.text.isNullOrEmpty()){
//                news.link = nwsLink?.text.toString()
//            }

            val updates = HashMap<String, Any>()
            updates.put("msg", news.msg!!)
            updates.put("title", news.title!!)

            FirebaseDatabase.getInstance().reference.child("news")
                    .child(newsKey!!)
                    .updateChildren(updates)
                    .addOnCompleteListener {
                        Toast.makeText(this@EditNews, "News posted", Toast.LENGTH_SHORT).show()
                        this@EditNews.finish()
                    }.addOnFailureListener {
                        Toast.makeText(this@EditNews, "News posting failed, check your internet connection", Toast.LENGTH_SHORT).show()
                    }
            if(withNofify){
                notifyUsers()
            }
        }




    }

    fun postAsTip(){
        val newsAsTp = Tip()
        newsAsTp.isNewstype = true
        newsAsTp.comment = nwsMsg?.text.toString()
        newsAsTp.date = System.currentTimeMillis()
        newsAsTp.rank = newsAsTp.date!! * -1
        newsAsTp.expertName = selectedNews?.title

        val key = FirebaseDatabase.getInstance().reference.child("tips").push().key

        newsAsTp.id = key

        FirebaseDatabase.getInstance().reference
                .child("tips")
                .child(key!!)
                .setValue(newsAsTp)
    }


    fun notifyUsers(){
        val news = NotificationsModel()
        news.title = ttl
        news.msg = nwsMsg?.text.toString()
        news.date = System.currentTimeMillis() * -1


//        FirebaseDatabase.getInstance().reference.child("notifications")
//                .push()
//                .setValue(news)
//                .addOnCompleteListener {
//                    //Toast.makeText(this@AddNewsActivity, "Notifications posted", Toast.LENGTH_SHORT).show()
//                    //this@AddNewsActivity.finish()
//                }.addOnFailureListener {
//                    Toast.makeText(this@AddNewsActivity, "Notifications posting failed, check your internet connection", Toast.LENGTH_SHORT).show()
//                }

        sendNotification(news.msg!!, news.title!!, "NEWS", "link")

    }

    override fun onBackPressed() {
        super.onBackPressed()
        selectedNews = null
    }

    fun getNewsTitle(){
        FirebaseDatabase.getInstance().reference
                .child("headings")
                .orderByChild("title")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Toast.makeText(this@EditNews, "Could not fetch experts, Check your internet connections", Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            for (i in p0.children){
                                val expert = i.getValue(Expert::class.java)
                                titleList.add(expert?.title!!)
                                titleObjectList.add(expert)
                                titleAdapter?.notifyDataSetChanged()
                            }

                            val index = titleList.indexOf(selectedNews?.title)
                            if (index != -1){
                                nwsTitle?.setSelection(index)
                                ttl = titleList[index]
                                //expertId = index
                                Log.e("expert", ttl)
                            }
                        }
                    }

                })
    }
}
