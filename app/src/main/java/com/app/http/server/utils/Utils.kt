package com.app.http.server.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Locale
object Utils {
    private val TAG = javaClass.simpleName

    fun isSupportedFileType(file: File): Boolean {
        if (!file.exists() || !file.isFile) return false

        val supportedExtensions = listOf(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic",
            "mp3", "wav", "m4a", "aac", "ogg", "flac",
            "mp4", "mkv", "avi", "mov", "flv", "wmv", "webm",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf")

        val fileExtension = file.extension.lowercase(Locale.ROOT)
        return supportedExtensions.contains(fileExtension)
    }


    fun generateRandomPin(): String {
        return (1..4)
            .map { (1..9).random() }
            .joinToString("")
    }

    fun openFile(activity: Activity, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, getMimeTypeFromUri(activity,file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            activity.showToast("No app found to open this file type.")
        }
    }

    private fun getMimeTypeFromUri(context: Context, file: File): String? {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return context.contentResolver.getType(uri)
    }

    fun getLocalIpAddress(): String? {
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            for (interfaces in networkInterfaces) {
                val inetAddresses = interfaces.inetAddresses
                for (address in inetAddresses) {
                    if (!address.isLoopbackAddress && address is InetAddress) {
                        val ip = address.hostAddress!!
                        if (ip.contains(".")) return ip
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Error: ", ex)
        }
        return null
    }

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}