package com.listen.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.listen.app.R
import com.listen.app.data.Segment

class SegmentAdapter(
	private var items: List<Segment>,
	private val onClick: (Segment) -> Unit
) : RecyclerView.Adapter<SegmentAdapter.SegmentViewHolder>() {

	class SegmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val tvTime: TextView = itemView.findViewById(R.id.tv_time)
		val tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SegmentViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_segment, parent, false)
		return SegmentViewHolder(view)
	}

	override fun onBindViewHolder(holder: SegmentViewHolder, position: Int) {
		val segment = items[position]
		holder.tvTime.text = segment.getFormattedStartTime()
		holder.tvDuration.text = segment.getFormattedDuration()
		holder.itemView.setOnClickListener { onClick(segment) }
	}

	override fun getItemCount(): Int = items.size

	fun submitList(newItems: List<Segment>) {
		items = newItems
		notifyDataSetChanged()
	}
}