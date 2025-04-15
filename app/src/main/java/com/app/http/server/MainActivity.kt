package com.app.http.server

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.http.server.adapters.FilesReceivedAdapter
import com.app.http.server.databinding.ActivityMainBinding
import com.app.http.server.utils.AppPermissions
import com.app.http.server.utils.AppPermissions.PERMISSION_REQUEST_CODE
import com.app.http.server.utils.Constants.appFolderName
import com.app.http.server.utils.CustomDialogs
import com.app.http.server.utils.PermissionDialog
import com.app.http.server.utils.Utils
import com.app.http.server.utils.Utils.getLocalIpAddress
import com.app.http.server.utils.gone
import com.app.http.server.utils.showToast
import com.app.http.server.utils.visible
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : AppCompatActivity() {
    private val TAG = javaClass.simpleName
    private lateinit var binding: ActivityMainBinding
    private var server: FileServer? = null
    private var isSettingsOpened = AtomicBoolean(false)
    private var filesList = ArrayList<File>()
    private lateinit var adapter: FilesReceivedAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val permissionsGranted = AppPermissions.hasPermissions(this,permissions())

        Log.e(TAG, "onCreate: http://${getLocalIpAddress()}" )
        if (!permissionsGranted){
            binding.clPermission.visible()
        }else{
            proceed()
        }
        binding.btnAllow.setOnClickListener {
            requestStoragePermissions()
        }
    }

    private fun proceed() {
        fetchAllFiles()
        if (Utils.isInternetAvailable(this)){
            startServer()
        }else{
            CustomDialogs.showNoInternetDialog(this){
                if (it){
                    proceed()
                }else{
                    finishAffinity()
                }
            }
        }
    }

    private fun fetchAllFiles() {
        // Fetching all files from the directory
        val folderList = getAppFolder().listFiles()
        folderList?.let {
            if (it.isNotEmpty()){
                filesList = (it.map { file ->
                    file
                }) as ArrayList<File>
            }
        }


        if (filesList.isNotEmpty()){
            binding.tvInfoFiles.gone()
        }
        adapter = FilesReceivedAdapter(this,filesList){
            Utils.openFile(this,it)
        }
        binding.rv.layoutManager = LinearLayoutManager(this)
        binding.rv.adapter = adapter
    }

    private fun startServer() {
        val requiredPin = Utils.generateRandomPin()
        binding.tvPin.text = getString(R.string.your_pin_for_login_into_browser, requiredPin)
        binding.tvIpAddress.text = getString(
            R.string.connect_via_http_phone_and_pc_must_be_on_a_same_network,
            getLocalIpAddress()
        )

        server = FileServer(this@MainActivity,requiredPin)
        {
            filesList.add(it)
            runOnUiThread {
                binding.tvInfoFiles.gone()
                adapter.notifyItemInserted(filesList.size - 1)
            }
        }
        server?.start()
        showToast(getString(R.string.server_started_at_http, getLocalIpAddress()))
    }



    private fun getAppFolder():File{
        return File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),appFolderName)
    }

    private fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            ActivityCompat.requestPermissions(
                this,
                AppPermissions.storagePermissionFor13,
                PERMISSION_REQUEST_CODE
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                AppPermissions.storagePermissionBelow13,
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, per: Array<String>, PResult: IntArray) {
        super.onRequestPermissionsResult(requestCode, per, PResult)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            val permissionResults = HashMap<String, Int>()
            var deniedCode = 0

            for (i in PResult.indices) {
                if (PResult[i] == PackageManager.PERMISSION_DENIED) {
                    permissionResults[per[i]] = PResult[i]
                    deniedCode++
                }
            }
            if (deniedCode != 0) {
                for ((perName, permResult) in permissionResults) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, perName)) {
                        PermissionDialog.showDialog(this,
                            object : PermissionDialog.DialogClickListener {
                                override fun onAllow() {
                                    AppPermissions.checkAndRequestPermissions(
                                        this@MainActivity,
                                        permissions(),
                                        PERMISSION_REQUEST_CODE
                                    )
                                }

                                override fun onSkip() {

                                }
                            })
                        break

                    } else {

                        PermissionDialog.showDialog(this,
                            object : PermissionDialog.DialogClickListener {
                                override fun onAllow() {
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", packageName, null)
                                    )
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    isSettingsOpened.set(true)
                                }

                                override fun onSkip() {

                                }
                            })


                        break
                    }
                }

            }else{
                binding.clPermission.gone()
                fetchAllFiles()
                binding.tvIpAddress.text = "http://${getLocalIpAddress()}"
                startServer()
            }
        }
    }

    private fun permissions(): Array<String> {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            AppPermissions.storagePermissionFor13
        }else
        {
            AppPermissions.storagePermissionBelow13
        }
        return permissions
    }

    override fun onResume() {
        super.onResume()
        if (isSettingsOpened.getAndSet(false)){
            if (AppPermissions.hasPermissions(this,permissions())){
                binding.clPermission.gone()
                fetchAllFiles()
                binding.tvIpAddress.text = "http://${getLocalIpAddress()}"
                startServer()
            }
        }
    }
}