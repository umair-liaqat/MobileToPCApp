package com.app.http.server.utils

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import com.app.http.server.R

object CustomDialogs {

    fun showNoInternetDialog(context: Context, onRetry: (Boolean) -> Unit) {

        val dialog = Dialog(context).apply {
                setContentView(R.layout.dialog_no_internet)
                setCancelable(false)
            }

        val btnRetry = dialog.findViewById<Button>(R.id.btn_retry)
        val btnExit = dialog.findViewById<Button>(R.id.btn_exit)
        btnRetry.setOnClickListener {
            dialog.dismiss()
            onRetry(true)
        }
        btnExit.setOnClickListener {
            dialog.dismiss()
            onRetry(false)
        }

        dialog.show()
    }

}