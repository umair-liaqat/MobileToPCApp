package com.app.http.server.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.http.server.R
import com.bumptech.glide.Glide
import java.io.File

class FilesReceivedAdapter(
    private val context: Context,
    private val files: List<File>,
    private val onFileClick: (File) -> Unit
) : RecyclerView.Adapter<FilesReceivedAdapter.FileViewHolder>() {

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fileName: TextView = itemView.findViewById(R.id.fileName)
        private val imageView: ImageView = itemView.findViewById(R.id.iv)

        fun bind(fileItem: File) {
            fileName.text = fileItem.name

            Glide.with(context)
                .load(fileItem.absoluteFile)
                .placeholder(R.drawable.baseline_file_24)
                .into(imageView)

            itemView.setOnClickListener {
                onFileClick(fileItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.itemview_files, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount(): Int = files.size
}