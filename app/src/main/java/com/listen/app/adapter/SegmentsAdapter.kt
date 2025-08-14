package com.listen.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.listen.app.R
import com.listen.app.data.Segment
import java.io.File

/**
 * RecyclerView adapter for displaying audio segments
 */
class SegmentsAdapter(
    private val onSegmentClick: (Segment) -> Unit
) : ListAdapter<Segment, SegmentsAdapter.SegmentViewHolder>(SegmentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SegmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_segment, parent, false)
        return SegmentViewHolder(view, onSegmentClick)
    }

    override fun onBindViewHolder(holder: SegmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SegmentViewHolder(
        itemView: View,
        private val onSegmentClick: (Segment) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvSegmentTime: TextView = itemView.findViewById(R.id.tv_time)
        private val tvSegmentDuration: TextView = itemView.findViewById(R.id.tv_duration)
        // Note: tv_segment_size and btn_play_segment don't exist in the layout
        // We'll need to add them or modify the layout

        fun bind(segment: Segment) {
            // Set segment time
            tvSegmentTime.text = segment.getFormattedStartTime()
            
            // Set duration
            tvSegmentDuration.text = segment.getFormattedDuration()
            
            // Set click listener on the entire item
            itemView.setOnClickListener {
                onSegmentClick(segment)
            }
        }
    }

    private class SegmentDiffCallback : DiffUtil.ItemCallback<Segment>() {
        override fun areItemsTheSame(oldItem: Segment, newItem: Segment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Segment, newItem: Segment): Boolean {
            return oldItem == newItem
        }
    }
} 