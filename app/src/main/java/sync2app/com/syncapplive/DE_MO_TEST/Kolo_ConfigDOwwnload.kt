package sync2app.com.syncapplive.DE_MO_TEST
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import sync2app.com.syncapplive.additionalSettings.urlchecks.checkUrlExistence
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.databinding.ActivityKoloWebviewPageBinding
import java.io.File
import java.util.Objects


class Kolo_ConfigDOwwnload : AppCompatActivity() {

    private lateinit var binding: ActivityKoloWebviewPageBinding
    private var downloadId: Long = -1


    private val simpleSavedPassword: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SIMPLE_SAVED_PASSWORD,
            Context.MODE_PRIVATE
        )
    }


    private val fileNameOne  = "app_background.png"
    private val fileNameTwo  = "Splash.mp4"
    private val fileNameThree  = "app_logo.png"


    private  var file1 = false
    private var file2 = false
    private var file3  = false
    private var isCalledToast = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKoloWebviewPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Register the broadcast receiver dynamically
        registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))


        // Start the file download when a button is clicked (replace with your UI element)
        binding.btnDownload.setOnClickListener {
             file1 = false
             file2 = false
             file3  = false
            isCalledToast = false
            loadBackGroundImageIfExist()
        }

    }


    private fun loadBackGroundImageIfExist() {

        val fileTypes = "app_background.png"
        val get_UserID = "CLO"
        val get_LicenseKey = "DE_MO_2021001"

        val pathFolder = "/" + get_UserID + "/" + get_LicenseKey + "/" + Constants.App + "/" + "Config"
        val folder = Environment.getExternalStorageDirectory().absolutePath + "/Download/${Constants.Syn2AppLive}/" + pathFolder
        val file = File(folder, fileTypes)

        if (file.exists()) {
            showToastMessage("Already exist")
        }else{
            check_for_valid_confileUrl()
        }
    }




    private fun check_for_valid_confileUrl() {

        val get_tMaster = simpleSavedPassword.getString(Constants.get_editTextMaster, "").toString()
        val get_UserID = "CLO"
        val get_LicenseKey = "DE_MO_2021001"

        val ServerUrl = "$get_tMaster/$get_UserID/$get_LicenseKey/App/Config/$fileNameOne"

        lifecycleScope.launch(Dispatchers.IO) {

            val Syn2AppLive = Constants.Syn2AppLive
            val innerFolder = "/App/Config/"
            val saveDemoStorage = "/$Syn2AppLive/$get_UserID/$get_LicenseKey/$innerFolder"
            val directoryParsing = Environment.getExternalStorageDirectory().absolutePath + "/Download/" + saveDemoStorage
            val myFileParsing = File(directoryParsing)
            delete(myFileParsing)


            withContext(Dispatchers.Main) {

                if (!file1){
                    file1 = true
                    startDownload(get_UserID,get_LicenseKey, ServerUrl, fileNameOne)
                    showToastMessage("Download ONE!")
                }

            }
        }
    }




    private fun startDownload(getFolderClo:String, getFolderSubpath:String, ServerUrl: String, fileName:String) {

        //  https://cp.cloudappserver.co.uk/app_base/public/CLO/DE_MO_2021001/App/Config/app_background.png

        val Syn2AppLive = Constants.Syn2AppLive
        val innerFolder = "/App/Config/"
        val saveMyFileToStorage = "/$Syn2AppLive/$getFolderClo/$getFolderSubpath/$innerFolder"



        lifecycleScope.launch {
            val result = checkUrlExistence(ServerUrl)
            if (result) {

                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), saveMyFileToStorage)
                if (!dir.exists()) {
                    dir.mkdirs()
                }

                // save files to this folder
                val folder = File(Environment.getExternalStorageDirectory().toString() + "/Download/$saveMyFileToStorage")

                if (!folder.exists()) {
                    folder.mkdirs()
                }

                val request = DownloadManager.Request(Uri.parse(ServerUrl))
                request.setTitle(fileName)
                request.allowScanningByMediaScanner()
                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, "/$saveMyFileToStorage/$fileName"
                )
                val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadId = downloadManager.enqueue(request)


            } else {

                showToastMessage("Invalid url")

            }

        }
    }



    private fun delete(file: File): Boolean {
        if (file.isFile) {
            return file.delete()
        } else if (file.isDirectory) {
            for (subFile in Objects.requireNonNull(file.listFiles())) {
                if (!delete(subFile)) return false
            }
            return file.delete()
        }
        return false
    }


    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val receivedId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (receivedId == downloadId) {

                if (file1 && !file2 && !file3){
                    file1 = true
                    file2 = true

                    val get_tMaster = simpleSavedPassword.getString(Constants.get_editTextMaster, "").toString()
                    val get_UserID = "CLO"
                    val get_LicenseKey = "DE_MO_2021001"

                    val ServerUrl = "$get_tMaster/$get_UserID/$get_LicenseKey/App/Config/$fileNameTwo"
                    startDownload(get_UserID,get_LicenseKey, ServerUrl, fileNameTwo)
                    showToastMessage("Download TWO!")

                }

                if (file1 && file2 && !file3){
                    file1 = true
                    file2 = true
                    file3 = true

                    val get_tMaster = simpleSavedPassword.getString(Constants.get_editTextMaster, "").toString()
                    val get_UserID = "CLO"
                    val get_LicenseKey = "DE_MO_2021001"

                    val ServerUrl = "$get_tMaster/$get_UserID/$get_LicenseKey/App/Config/$fileNameThree"
                    startDownload(get_UserID,get_LicenseKey, ServerUrl, fileNameThree)

                    showToastMessage("Download THREE!")
                }


                if (!isCalledToast){
                    isCalledToast = true
                    if (file1 && file2 && file3){
                        showToastMessage("All files downloaded")
                    }
                }

            }
        }
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
    override fun onDestroy() {
        super.onDestroy()

        if (downloadReceiver != null) {
            unregisterReceiver(downloadReceiver)
        }
    }
}