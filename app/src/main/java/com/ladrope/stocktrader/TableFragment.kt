package com.ladrope.stocktrader


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_home.view.*


class TableFragment : Fragment() {

    private var viewPager: ViewPager? = null
    private var mFragmentList = arrayListOf<Fragment>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_table, container, false)

        viewPager = view.container
        //checkUser()

        val adapter = ViewPagerAdapter(childFragmentManager)
        if (mFragmentList.isEmpty()){
            adapter.addFragment(OpenCallsFragment())
            adapter.addFragment(ClosedCallFragment())
        }

        viewPager?.adapter = adapter
        //viewPager?.offscreenPageLimit = 5

        view.tabLayout.setupWithViewPager(viewPager)


        val tabNames = arrayListOf<String>("Market Tracker", "EQWealth Tracker")


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
