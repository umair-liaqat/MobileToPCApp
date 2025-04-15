package com.app.http.server

import android.content.Context
import android.os.Environment
import android.util.Log
import com.app.http.server.utils.Constants
import com.app.http.server.utils.Utils
import com.app.http.server.utils.Utils.generateRandomPin
import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.IOException


class FileServer(private val context: Context,private val requiredPin: String,private val listener: (File)->Unit) : NanoHTTPD(8080) {

    private val TAG = javaClass.simpleName
    private var isAuthenticated = false

    override fun serve(session: IHTTPSession): Response {
        Log.e(TAG, "serve: ${session.uri}" )

        return when {
            session.uri == "/" || session.uri == "/login.html" -> serveStaticFile("login.html", "text/html")
            session.method == Method.POST && session.uri == "/login" -> handleLogin(session)

            session.uri.endsWith(".css") -> serveStaticFile(session.uri.substring(1), "text/css")
            session.uri.endsWith(".js") -> serveStaticFile(session.uri.substring(1), "application/javascript")

            !isAuthenticated -> serveStaticFile("login.html", "text/html")

            // Authenticated routes
            session.uri == "/index.html" -> serveStaticFile("index.html", "text/html")
            session.uri.startsWith("/files") -> listFiles()
            session.uri.startsWith("/download/") -> serveFile(session.uri.substring(10))
            session.uri.startsWith("/upload") -> handleUpload(session)

            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found")
        }
    }

    private fun serveStaticFile(filename: String, mimeType: String): Response {
        val assetManager = context.assets
        return try {
            val inputStream = assetManager.open("web/$filename")
            val bytes = inputStream.readBytes()
            newFixedLengthResponse(Response.Status.OK, mimeType, bytes.inputStream(), bytes.size.toLong())
        } catch (e: IOException) {
            newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File not found")
        }
    }


    private fun listFiles(): Response {
        val externalStorage = Environment.getExternalStorageDirectory() // Root of external storage
        val fileList = mutableListOf<String>()

        fun scanDirectory(directory: File) {
            directory.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    scanDirectory(file) // Recursively scan subdirectories
                } else {
                    if (Utils.isSupportedFileType(file)){
                        fileList.add(file.absolutePath)
                    }

                }
            }
        }
        scanDirectory(externalStorage)

        return newFixedLengthResponse(Gson().toJson(fileList))
    }


    private fun serveFile(filename: String): Response {
        val file = File(filename)

        Log.e(TAG, "serveFile: ${file.absoluteFile}" )
        return if (file.exists()) newFixedLengthResponse(Response.Status.OK, "application/octet-stream", file.inputStream(), file.length())
        else newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File not found")
    }

    private fun handleUpload(session: IHTTPSession): Response {
        val fullType = session.headers["type"]
        val contentType = fullType
            ?.substringAfter("/")
            ?.substringBefore("+")
        Log.e(TAG, "handleUpload: $contentType" )
        val appFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Constants.appFolderName)
        if(!appFolder.exists()){
            appFolder.mkdirs()
        }
        val files = mutableMapOf<String, String>()
        session.parseBody(files)
        Log.e(TAG, "handleUpload: file ${files["file"]}" )
        val uploadedFile = File(files["file"]!!)

        val destination = File(appFolder, "${System.currentTimeMillis()}.$contentType")
        uploadedFile.copyTo(destination, true)
        listener.invoke(destination)
        return newFixedLengthResponse("File uploaded successfully!")
    }

    private fun handleLogin(session: IHTTPSession): Response {
        session.parseBody(mutableMapOf()) // Parse POST data
        val pin = session.parameters["pin"]?.firstOrNull()

        return if (pin == requiredPin) {
            isAuthenticated = true
            newFixedLengthResponse("Login successful")
        } else {
            newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", "Invalid PIN")
        }
    }
}
