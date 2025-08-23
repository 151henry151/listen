package com.listen.app.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.listen.app.R

/**
 * Dialog to request user consent for audio recording
 */
class ConsentDialog : DialogFragment() {
    
    private var onConsentGranted: (() -> Unit)? = null
    private var onConsentDenied: (() -> Unit)? = null
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.Theme_Listen)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_recording_consent, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<Button>(R.id.btn_consent_accept).setOnClickListener {
            onConsentGranted?.invoke()
            dismiss()
        }
        
        view.findViewById<Button>(R.id.btn_consent_decline).setOnClickListener {
            onConsentDenied?.invoke()
            dismiss()
        }
    }
    
    fun setOnConsentGranted(listener: () -> Unit) {
        onConsentGranted = listener
    }
    
    fun setOnConsentDenied(listener: () -> Unit) {
        onConsentDenied = listener
    }
    
    companion object {
        const val TAG = "ConsentDialog"
        
        fun newInstance(): ConsentDialog {
            return ConsentDialog()
        }
    }
} 