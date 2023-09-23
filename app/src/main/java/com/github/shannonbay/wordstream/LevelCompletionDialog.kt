package com.github.shannonbay.wordstream

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.DialogFragment

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class LevelCompletionDialog(val currentLevel: Int, val currentStage: Int, val s: String) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val customTitleView = inflater.inflate(R.layout.level_completion, null)

            builder.setCustomTitle(customTitleView)
                .setMessage(s)
                .setPositiveButton("Begin Level ${currentLevel + 1}") { _, _ ->
                    // Handle positive button click (if needed)
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}