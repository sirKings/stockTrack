package com.ladrope.stocktrader

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_trade.*

class TradeActivity : AppCompatActivity(), TradeAdminFragment.Listener, TradeMarketFragment.Listener {
    override fun updateAccount(balance: String) {
        balnce?.text = "Balance: "+balance
    }

    private var viewPager: ViewPager? = null
    private var mFragmentList = arrayListOf<Fragment>()
    var balnce : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trade)

        backBtn.setOnClickListener {
            finish()
        }

        viewPager = container
        //checkUser()
        balnce = balance

        capital.text = "Capital: "+user?.capital
        balnce?.text = "Balance: "+(200000 - getAllTradeMargin())

        val adapter = ViewPagerAdapter(supportFragmentManager)

        val firstFrag = TradeMarketFragment()
        firstFrag.setListener(this)

        val secondFrag = TradeAdminFragment()
        secondFrag.setListener(this)

        if (mFragmentList.isEmpty()){
            adapter.addFragment(firstFrag)
            adapter.addFragment(secondFrag)
        }

        viewPager?.adapter = adapter
        //viewPager?.offscreenPageLimit = 5

        tabLayout.setupWithViewPager(viewPager)


        val tabNames = arrayListOf<String>("Market Tracker", "EQWealth Tracker")


        for (i in tabNames.indices) {
            tabLayout.getTabAt(i)?.setText(tabNames[i])
            //tabLayout.getTabAt(i)?.text = tabNames[i]
        }

    }

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
}
