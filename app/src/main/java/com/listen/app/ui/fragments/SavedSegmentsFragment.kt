package com.listen.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.listen.app.R
import com.listen.app.data.SavedSegment
import com.listen.app.ui.SavedSegmentAdapter

class SavedSegmentsFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SavedSegmentAdapter
    private var savedSegments: List<SavedSegment> = emptyList()
    private var onSavedSegmentClick: ((SavedSegment) -> Unit)? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_saved_segments, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.rv_saved_segments)
        adapter = SavedSegmentAdapter(emptyList()) { savedSegment ->
            onSavedSegmentClick?.invoke(savedSegment)
        }
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        
        updateSavedSegments(savedSegments)
    }
    
    fun setOnSavedSegmentClickListener(listener: (SavedSegment) -> Unit) {
        onSavedSegmentClick = listener
    }
    
    fun updateSavedSegments(newSavedSegments: List<SavedSegment>) {
        savedSegments = newSavedSegments
        adapter.submitList(savedSegments)
    }
} 