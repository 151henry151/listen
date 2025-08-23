package com.romp.listen.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.romp.listen.app.R
import com.romp.listen.app.data.SavedSegment

class SavedSegmentAdapter(
    private var items: List<SavedSegment>,
    private val onClick: (SavedSegment) -> Unit
) : RecyclerView.Adapter<SavedSegmentAdapter.SavedSegmentViewHolder>() {

    class SavedSegmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFilename: TextView = itemView.findViewById(R.id.tv_filename)
        val tvSize: TextView = itemView.findViewById(R.id.tv_size)
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
        val tvCallBadge: TextView? = itemView.findViewById(R.id.tv_call_badge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedSegmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_saved_segment, parent, false)
        return SavedSegmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavedSegmentViewHolder, position: Int) {
        val savedSegment = items[position]
        holder.tvFilename.text = savedSegment.filename
        holder.tvSize.text = savedSegment.getFormattedSize()
        holder.tvDate.text = savedSegment.getFormattedDate()
        holder.tvDuration.text = savedSegment.getFormattedDuration()
        
        if (savedSegment.isPhoneCall) {
            val dir = savedSegment.callDirection ?: "CALL"
            val num = savedSegment.phoneNumber ?: ""
            holder.tvCallBadge?.visibility = View.VISIBLE
            holder.tvCallBadge?.text = if (num.isNotEmpty()) "$dir: $num" else dir
        } else {
            holder.tvCallBadge?.visibility = View.GONE
        }
        
        holder.itemView.setOnClickListener { onClick(savedSegment) }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<SavedSegment>) {
        items = newItems
        notifyDataSetChanged()
    }
} 