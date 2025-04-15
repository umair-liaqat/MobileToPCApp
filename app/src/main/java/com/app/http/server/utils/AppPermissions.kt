package com.app.http.server.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object AppPermissions {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    var storagePermissionFor13 = arrayOf<String>(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_MEDIA_VIDEO
    )

    var storagePermissionBelow13 = arrayOf<String>(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    val PERMISSION_REQUEST_CODE = 1122

    fun checkAndRequestPermissions(activity: Activity,permissions: Array<String>,permissionCode: Int): Boolean {

        val list = java.util.ArrayList<String>()

        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    perm
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                list.add(perm)
            }
        }
        if (list.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity, list.toTypedArray(),
                permissionCode
            )
            return false
        }
        return true
    }



    fun hasPermissions(context: Context,permissions: Array<String>): Boolean {

        val list: ArrayList<String?> = ArrayList()


        for (i in permissions.indices) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permissions[i]
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                list.add(permissions[i])
            }
        }

        return list.isEmpty()
    }
}