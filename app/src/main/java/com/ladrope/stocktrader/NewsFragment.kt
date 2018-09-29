package com.ladrope.stocktrader


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.ladrope.stocktrader.model.NewsModel
import com.squareup.picasso.Picasso
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import io.branch.referral.util.LinkProperties
import kotlinx.android.synthetic.main.fragment_news.view.*
import kotlinx.android.synthetic.main.news_row.view.*


/**
 * A simple [Fragment] subclass.
 *
 */
class NewsFragment : Fragment() {

    var newsRecyclerView: RecyclerView? = null
    var adapter: NewsAdapter? = null
    var layoutManager: RecyclerView.LayoutManager? = null
    var options: FirebaseRecyclerOptions<NewsModel>? = null
    var mProgressBar: ProgressBar? =null
    var mErrorText: TextView? = null
    var mEmptyText: TextView? = null

    val buo = BranchUniversalObject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_news, container, false)

        newsRecyclerView = view.newsRV
        //val snapHelper = SnapHelperOneByOne()
        //snapHelper.attachToRecyclerView(newsRecyclerView)
        if(isAdmin){
            val simpleTouchhelper = ItemTouchHelper(simpleItemTouchCallback)
            simpleTouchhelper.attachToRecyclerView(newsRecyclerView)
        }else{
            view.fab.hide()
        }

        mProgressBar = view.newsProgress
        mErrorText = view.newsErrorText
        mEmptyText = view.newsEmptyText

        view.fab.setOnClickListener {
            addNews()
        }

        val query = FirebaseDatabase.getInstance().reference.child("news").orderByChild("date").limitToFirst(30)
        setup(query)

        return view
    }

    fun setup(query: Query){
        Log.e("setup", "started")
        mErrorText?.visibility = View.GONE
        mEmptyText?.visibility = View.GONE
        options = FirebaseRecyclerOptions.Builder<NewsModel>()
                .setQuery(query, NewsModel::class.java)
                .build()
        adapter = NewsAdapter(options!!, context!!)
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

    fun addNews(){
        val addIntent = Intent(activity, AddNewsActivity::class.java)
        startActivity(addIntent)
    }

    inner class NewsAdapter(options: FirebaseRecyclerOptions<NewsModel>, private val context: Context): FirebaseRecyclerAdapter<NewsModel, NewsAdapter.ViewHolder>(options) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.news_row, parent, false)
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

        override fun onBindViewHolder(holder: ViewHolder, position: Int, model: NewsModel) {
            holder.bindItem(model)
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bindItem(tip: NewsModel) {
                val title = itemView.findViewById<TextView>(R.id.newsTitle)
                val msg = itemView.findViewById<TextView>(R.id.newsMsg)
                val image = itemView.findViewById<ImageView>(R.id.newsImage)
                val readMore = itemView.findViewById<TextView>(R.id.readMore)

                title.text = tip.title
                msg.text = tip.msg
                Picasso.with(context).load(tip.image).into(image)
                //if(tip.link != null){
                if (tip.expertComments == null){
                    readMore.text = "Read more(0)"
                    if (isAdmin){
                        readMore.setOnClickListener {
                            readMoreClicked(tip, getRef(adapterPosition).key!!)
                        }
                    }
                }else{
                    readMore.text = "Read more("+tip.expertComments?.size+")"
                    readMore.setOnClickListener {
                        readMoreClicked(tip, getRef(adapterPosition).key!!)
                    }
                }

               // }

                title.setOnClickListener {
                    val intent = Intent(activity, NewsActivity::class.java)
                    intent.putExtra("title", tip.title)
                    startActivity(intent)
                }

                itemView.newsTime.text = getDate((tip.date!! * -1))

                itemView.newsShareBtn.setOnClickListener {
                    shareNews(tip, context)
                }
            }
        }
    }

    fun readMoreClicked(tip: NewsModel, id: String){
        Log.e("link", tip.link.toString())
        selectedNews = tip
        //if (tip.link != null){
            val intent = Intent(activity, NewsReadMore::class.java)
            intent.putExtra("tipId", id)
            intent.putExtra("where", "expertComments")
            startActivity(intent)
       // }
    }

    fun shareNews(tip: NewsModel, context: Context){

        val title = tip.title
        val desc  = tip.msg

        buo.setCanonicalIdentifier(tip.date.toString())
                .setTitle(title!!)
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

    var simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.DOWN or ItemTouchHelper.UP) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            Toast.makeText(context, "on Move", Toast.LENGTH_SHORT).show()
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            //Remove swiped item from list and notify the RecyclerView
            val position = viewHolder.adapterPosition
            val id = adapter?.getRef(position)
            //arrayList.remove(position)
            callDelete(id, adapter?.getItem(position))
            //adapter?.notifyDataSetChanged()

        }
    }

    fun callDelete(position: DatabaseReference?, news: NewsModel?){
        val builder1 = AlertDialog.Builder(context!!)
        builder1.setTitle(Html.fromHtml("<font color='#000000'>Delete item?</font>"))
        builder1.setCancelable(false)

        builder1.setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { dialog, id ->
                    FirebaseDatabase.getInstance().reference
                            .child("news")
                            .child(position?.key!!)
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

        builder1.setNeutralButton("Edit", DialogInterface.OnClickListener{
                dialog, id ->
                selectedNews = news
                editNews(position?.key!!)
        })

        val alert = builder1.create()
        alert.show()
    }

    fun editNews(id: String){
        val intent = Intent(activity, EditNews::class.java)
        intent.putExtra("newsRef", id)
        startActivity(intent)
    }

}
