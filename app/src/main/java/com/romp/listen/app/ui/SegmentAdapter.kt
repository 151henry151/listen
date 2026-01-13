package com.romp.listen.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.romp.listen.app.R
import com.romp.listen.app.data.Segment

class SegmentAdapter(
	private var items: List<Segment>,
	private val onClick: (Segment) -> Unit
) : RecyclerView.Adapter<SegmentAdapter.SegmentViewHolder>() {

	private var currentlyPlayingSegmentId: Long? = null

	class SegmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val tvTime: TextView = itemView.findViewById(R.id.tv_time)
		val tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
		val tvCallBadge: TextView? = itemView.findViewById(R.id.tv_call_badge)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SegmentViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_segment, parent, false)
		return SegmentViewHolder(view)
	}

	override fun onBindViewHolder(holder: SegmentViewHolder, position: Int) {
		val segment = items[position]
		holder.tvTime.text = segment.getFormattedStartTime()
		holder.tvDuration.text = segment.getFormattedDuration()
		if (segment.isPhoneCall) {
			val dir = segment.callDirection ?: "CALL"
			val num = segment.phoneNumber ?: ""
			holder.tvCallBadge?.visibility = View.VISIBLE
			holder.tvCallBadge?.text = if (num.isNotEmpty()) "$dir: $num" else dir
		} else {
			holder.tvCallBadge?.visibility = View.GONE
		}
		
		// Highlight currently playing segment
		val isCurrentlyPlaying = segment.id == currentlyPlayingSegmentId
		val backgroundColor = if (isCurrentlyPlaying) {
			ContextCompat.getColor(holder.itemView.context, R.color.segment_selected_background)
		} else {
			android.graphics.Color.TRANSPARENT
		}
		holder.itemView.setBackgroundColor(backgroundColor)
		
		holder.itemView.setOnClickListener { onClick(segment) }
	}

	override fun getItemCount(): Int = items.size

	fun submitList(newItems: List<Segment>) {
		items = newItems
		notifyDataSetChanged()
	}
	
	fun setCurrentlyPlayingSegment(segmentId: Long?) {
		val previousId = currentlyPlayingSegmentId
		currentlyPlayingSegmentId = segmentId
		
		// Notify changes for both previous and current items to update highlighting
		if (previousId != null) {
			val previousIndex = items.indexOfFirst { it.id == previousId }
			if (previousIndex >= 0) {
				notifyItemChanged(previousIndex)
			}
		}
		if (segmentId != null) {
			val currentIndex = items.indexOfFirst { it.id == segmentId }
			if (currentIndex >= 0) {
				notifyItemChanged(currentIndex)
			}
		}
	}
}