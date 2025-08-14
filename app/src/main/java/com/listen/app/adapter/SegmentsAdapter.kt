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

        private val tvSegmentTime: TextView = itemView.findViewById(R.id.tv_segment_time)
        private val tvSegmentDuration: TextView = itemView.findViewById(R.id.tv_segment_duration)
        private val tvSegmentSize: TextView = itemView.findViewById(R.id.tv_segment_size)
        private val btnPlaySegment: Button = itemView.findViewById(R.id.btn_play_segment)

        fun bind(segment: Segment) {
            // Set segment time
            tvSegmentTime.text = segment.getFormattedStartTime()
            
            // Set duration
            tvSegmentDuration.text = itemView.context.getString(R.string.segment_duration, segment.getFormattedDuration())
            
            // Set file size
            val file = File(segment.filePath)
            val sizeText = if (file.exists()) {
                val sizeKB = segment.fileSize / 1024
                if (sizeKB >= 1024) {
                    val sizeMB = sizeKB / 1024.0
                    itemView.context.getString(R.string.segment_size_mb, sizeMB)
                } else {
                    itemView.context.getString(R.string.segment_size_kb, sizeKB)
                }
            } else {
                itemView.context.getString(R.string.segment_size_unknown)
            }
            tvSegmentSize.text = sizeText
            
            // Set click listener
            btnPlaySegment.setOnClickListener {
                onSegmentClick(segment)
            }
            
            // Check if file exists and update button state
            val fileExists = file.exists()
            btnPlaySegment.isEnabled = fileExists
            btnPlaySegment.text = if (fileExists) {
                itemView.context.getString(R.string.btn_play_segment)
            } else {
                itemView.context.getString(R.string.segment_missing)
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