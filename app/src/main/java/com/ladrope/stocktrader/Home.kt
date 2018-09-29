package com.ladrope.stocktrader

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ladrope.stocktrader.model.User
import kotlinx.android.synthetic.main.activity_home.*




class Home : AppCompatActivity() {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    //private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var viewPager: ViewPager? = null
    private var mFragmentList = arrayListOf<Fragment>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        val currentUser = User()
        currentUser.capital = 200000
        currentUser.balance = 200000
        createUser(currentUser)
        viewPager = container
        checkUser()
        //Thread({
            // call runnable here
            manageUser()
            println("running from lambda: ${Thread.currentThread()}")
        //}).start()


        val adapter = ViewPagerAdapter(getSupportFragmentManager())
        adapter.addFragment(TipsFragment())
        if (isAdmin){
            getUsers()
        }
        adapter.addFragment(NewsFragment())
        adapter.addFragment(TradeFragment())
        adapter.addFragment(TableFragment())
        adapter.addFragment(ChatFragment())
        adapter.addFragment(AccountFragment())

        viewPager?.adapter = adapter
        val isNews = intent.getStringExtra("type")
        if (isNews == "notes"){
            viewPager?.setCurrentItem(2, true)
        }else if (isNews == "news"){
            viewPager?.setCurrentItem(1, true)
        }
        tabLayout.setupWithViewPager(viewPager)
        //viewPager?.offscreenPageLimit = 5

        val imageResId = ArrayList<Int>()
        imageResId.add(R.drawable.baseline_wb_sunny_24)
        //if (isAdmin){
        imageResId.add(R.drawable.baseline_library_books_24)
        //}
        imageResId.add(R.drawable.baseline_account_box_24)
        imageResId.add(R.drawable.baseline_trending_up_24)
        imageResId.add(R.drawable.baseline_forum_24)
        imageResId.add(R.drawable.baseline_dehaze_24)

//        val tabNames = arrayListOf<String>("Tips")
//        if (isAdmin){
//            tabNames.add("News")
//        }
//        tabNames.add("Notifications")
//        tabNames.add("Account")

        for (i in imageResId.indices) {
            tabLayout.getTabAt(i)?.setIcon(imageResId[i])
            //tabLayout.getTabAt(i)?.icon?.setTint(resources.getColor(R.color.colorAccent))
            //tabLayout.getTabAt(i)?.text = tabNames[i]
        }

        notificationClicked.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        //tabLayout.getTabAt(0)?.icon?.setColorFilter(resources.getColor(R.color.white), PorterDuff.Mode.CLEAR)

    }


//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_home, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        val id = item.itemId
//
//        if (id == R.id.action_settings) {
//            return true
//        }
//
//        return super.onOptionsItemSelected(item)
//    }

    internal inner class ViewPagerAdapter(manager: FragmentManager): FragmentPagerAdapter(manager) {

        override fun getCount(): Int {
            return mFragmentList.size
        }
        override fun getItem(position:Int): Fragment {
            return mFragmentList.get(position)
        }
        fun addFragment(fragment: Fragment) {
            mFragmentList.add(fragment)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return null
        }

    }

    fun checkUser(){
        val prefs = getSharedPreferences("com.ladrope.stockTrader", Context.MODE_PRIVATE)
       isAdmin = prefs.getBoolean("isAdmin", false)
    }


    fun getUsers(){
        FirebaseDatabase.getInstance().reference
                .child("users")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            usersIds.clear()
                            for (i in p0.children){
                                usersIds.add(i.getValue(String::class.java))
                            }
                        }
                    }
                })
    }

    fun manageUser(){

        FirebaseDatabase.getInstance().reference
                .child("profiles")
                .child(FirebaseAuth.getInstance().uid!!)
                .child("trades")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        //Log.e("trade", "detected")
                        tipIdList.clear()
                        if (p0.exists()){
                            for (tipId in p0.children){
                                tipIdList.add(tipId.getValue(String::class.java)!!)
                            }
                            tipList.clear()
                            getTipss()
                        }else{
                            tipList.clear()
                        }
                    }
                })
    }

    override fun onBackPressed() {
        if (viewPager?.currentItem != 0){
            viewPager?.setCurrentItem(0, false)
        }else{
            super.onBackPressed()
        }
    }

}
