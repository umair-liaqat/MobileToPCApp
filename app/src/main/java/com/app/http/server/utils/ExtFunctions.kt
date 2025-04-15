package com.app.http.server.utils

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.view.View
import android.widget.Toast

fun Activity.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT)
        .show()
}


fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val minimumInterval = 1000 // Minimum interval between clicks (in milliseconds)
    var lastClickTime = 0L

    setOnClickListener { view ->
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastClickTime >= minimumInterval) {
            lastClickTime = currentTime
            onSafeClick(view)
        }
    }
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun Activity?.isActivityActive(): Boolean {
    return null != this && !isFinishing && !isDestroyed
}



