package com.romp.listen.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.romp.listen.app.R
import com.romp.listen.app.data.Segment
import com.romp.listen.app.ui.SegmentAdapter

class RotatingSegmentsFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SegmentAdapter
    private var segments: List<Segment> = emptyList()
    private var onSegmentClick: ((Segment) -> Unit)? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rotating_segments, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.rv_segments)
        adapter = SegmentAdapter(emptyList()) { segment ->
            onSegmentClick?.invoke(segment)
        }
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        
        updateSegments(segments)
    }
    
    fun setOnSegmentClickListener(listener: (Segment) -> Unit) {
        onSegmentClick = listener
    }
    
    fun updateSegments(newSegments: List<Segment>) {
        segments = newSegments
        adapter.submitList(segments)
    }
} 