package com.ladrope.stocktrader

import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView


class SnapHelperOneByOne:LinearSnapHelper() {
    override fun findTargetSnapPosition(layoutManager:RecyclerView.LayoutManager, velocityX:Int, velocityY:Int):Int {
        if (!(layoutManager is RecyclerView.SmoothScroller.ScrollVectorProvider))
        {
            return RecyclerView.NO_POSITION
        }
        val currentView = findSnapView(layoutManager)
        if (currentView == null)
        {
            return RecyclerView.NO_POSITION
        }
        val currentPosition = layoutManager.getPosition(currentView)
        if (currentPosition == RecyclerView.NO_POSITION)
        {
            return RecyclerView.NO_POSITION
        }
        return currentPosition
    }
}