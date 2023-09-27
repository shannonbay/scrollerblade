package com.github.shannonbay.wordstream

import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.DialogFragment

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class AboutDialog(val packageManager: PackageManager, val packageName: String) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_about, null)

            val pInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
            val buildNumber = pInfo.versionCode.toString()

            builder.setCustomTitle(dialogView)
                .setMessage(getString(R.string.build_number, buildNumber))
                .setPositiveButton("OK") { _, _ ->
                    // Handle positive button click (if needed)
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}