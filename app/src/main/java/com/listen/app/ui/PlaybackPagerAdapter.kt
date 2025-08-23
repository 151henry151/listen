package com.listen.app.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.listen.app.ui.fragments.RotatingSegmentsFragment
import com.listen.app.ui.fragments.SavedSegmentsFragment

class PlaybackPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    
    override fun getItemCount(): Int = 2
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RotatingSegmentsFragment()
            1 -> SavedSegmentsFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
} 