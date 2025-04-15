package com.app.http.server.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.app.http.server.R

object PermissionDialog {

    private lateinit var dialog: Dialog


    fun showDialog(context: Context, dialogClickListener: DialogClickListener)
    {
        dialog = Dialog(context)
        dialog.let {
            it.requestWindowFeature(Window.FEATURE_NO_TITLE)
            it.setContentView(R.layout.dialog_denied_permission)
            it.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setCancelable(true)
            it.setCanceledOnTouchOutside(false)
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(it.window!!.attributes)
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT

            it.show()
            it.window!!.attributes = lp

            it.findViewById<TextView>(R.id.tv_skip).setOnClickListener {view->
                it.dismiss()
                dialogClickListener.onSkip()
            }
            it.findViewById<TextView>(R.id.tv_allow).setOnClickListener {view->
                it.dismiss()
                dialogClickListener.onAllow()
            }
        }
    }

    interface DialogClickListener
    {
        fun onAllow()

        fun onSkip()
    }
}