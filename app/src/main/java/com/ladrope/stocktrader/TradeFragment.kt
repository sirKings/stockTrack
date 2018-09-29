package com.ladrope.stocktrader


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_trade.view.*

/**
 * A simple [Fragment] subclass.
 *
 */
class TradeFragment : Fragment() {

    private var viewPager: ViewPager? = null
    private var mFragmentList = arrayListOf<Fragment>()
    //var balnce : TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trade, container, false)
        viewPager = view.container

        val adapter = ViewPagerAdapter(childFragmentManager)

        if (mFragmentList.isEmpty()){
            adapter.addFragment(ClosedCallFragment())
            adapter.addFragment(TradeMarketFragment())
        }

        viewPager?.adapter = adapter
        //viewPager?.offscreenPageLimit = 5

        view.tabLayout.setupWithViewPager(viewPager)


        val tabNames = arrayListOf<String>("Eqwealth", "My Trades")


        for (i in tabNames.indices) {
            view.tabLayout.getTabAt(i)?.setText(tabNames[i])
            //tabLayout.getTabAt(i)?.text = tabNames[i]
        }

        return view
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
