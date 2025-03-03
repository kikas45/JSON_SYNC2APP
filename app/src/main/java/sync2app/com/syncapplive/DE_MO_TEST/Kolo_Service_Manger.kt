package sync2app.com.syncapplive.DE_MO_TEST
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import sync2app.com.syncapplive.additionalSettings.myApiDownload.FilesApi
import sync2app.com.syncapplive.additionalSettings.myApiDownload.FilesViewModel
import sync2app.com.syncapplive.additionalSettings.myFailedDownloadfiles.DnFailedApi
import sync2app.com.syncapplive.additionalSettings.myFailedDownloadfiles.DnFailedViewModel
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.additionalSettings.utils.Utility
import sync2app.com.syncapplive.databinding.ActivityDemoServiceMainBinding
import sync2app.com.syncapplive.myService.ParsingSyncService
import sync2app.com.syncapplive.myService.RetryParsingSyncService
import java.io.File
import java.util.Objects


class Kolo_Service_Manger : AppCompatActivity() {

    private lateinit var binding: ActivityDemoServiceMainBinding
    private val mfilesViewModel by viewModels<FilesViewModel>()
    private val dnFailedViewModel by viewModels<DnFailedViewModel>()
    private var filesToProcess = 0
    private  var filIst = ""
    private val mutex = Mutex()
    private var  KoloLog = "Kolo_APIS_Downloads"

    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val sharedP: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.MY_DOWNLOADER_CLASS,
            Context.MODE_PRIVATE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDemoServiceMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val filter = IntentFilter().apply { addAction(Constants.RECIVER_PROGRESS) }
        registerReceiver(progressReceiver, filter)

        val filterPr = IntentFilter().apply { addAction(Constants.RECIVER_DOWNLOAD_BYTES_PROGRESS) }
        registerReceiver(progressDownloadBytesReceiver, filterPr)



        binding.editTextText3.setText("https://cp.cloudappserver.co.uk/app_base/public//CLO/DE_MO_2021001/App/index.html")

        binding.btnFecthData.setOnClickListener {

            cleanTempFolder()
            applicationContext.applicationContext.stopService(Intent(applicationContext.applicationContext, ParsingSyncService::class.java))
            applicationContext.applicationContext.stopService(Intent(applicationContext.applicationContext, RetryParsingSyncService::class.java))
            binding.textDisplaytext.text = ""
            binding.textStatusProcess.text = "PR:Running"
            binding.textFilecount.text = "Dl:--/--"
            binding.textDownladByes.visibility = View.GONE
            binding.progressBarPref.progress = 0
            val editTextValue = binding.editTextText3.text.toString().trim()
            getAllIndexUrls(editTextValue)

            val editor = sharedP.edit()
            editor.remove(Constants.RetryCount)
            editor.apply()
        }

    }



    ///////// fetch Files into Room Database

    @SuppressLint("SetTextI18n")
    private fun getAllIndexUrls(url: String) {

        dnFailedViewModel.deleteAllFiles()
        mfilesViewModel.deleteAllFiles()

        CoroutineScope(Dispatchers.IO).launch {
            val urls = Utility.fetchUrlsFromHtml(url)
            filesToProcess = urls.size
            // Prepare a StringBuilder to accumulate URLs
            val builder = StringBuilder()

            withContext(Dispatchers.Main) {
                var validCount = 0  // Counter for valid URLs

                urls.forEach { it ->
                    Log.d(KoloLog, "Fetched URL: $it")
                    filIst = builder.append("$validCount : Fetched URL: $it\n").toString()

                    // Check if the URL should be saved
                    if (shouldSaveUrl(it)) {
                        // Save URL pairs with required details
                        saveParsingURLPairs(validCount, it, urls.size)
                        validCount++  // Increment only for valid URLs
                    } else {
                        Log.d(KoloLog, "$validCount :  Ignoring URL: $it")
                    }

                    mutex.withLock {
                        filesToProcess--
                        if (filesToProcess == 0) {
                            // All files are processed, trigger your action here
                            onAllFilesProcessed()
                        }
                    }

                }

            }
        }
    }

    // Function to determine if a URL should be saved
    private fun shouldSaveUrl(url: String): Boolean {
        // Check if the URL ends with a slash
        if (url.endsWith("/")) return false

        // Extract the relative path after the base URL
        val baseUrl = "https://cp.cloudappserver.co.uk/app_base/public//CLO/DE_MO_2021001/"
        val relativePath = url.removePrefix(baseUrl)

        // Check if there is a file name with a dot (.)
        val fileName = relativePath.substringAfterLast('/')
        return fileName.contains('.')
    }

    private fun saveParsingURLPairs(index: Int, url: String, totalFiles: Int) {
        val folderAndFile = extractFolderAndFile(url)
        val folderName = folderAndFile.first
        val fileName = folderAndFile.second
        val status = "true"

        // Initialize filesToProcess only once
        if (filesToProcess == 0) {
            filesToProcess = totalFiles
        }


        val files = DnFailedApi(
            SN = index.toString(),
            FolderName = folderName,
            FileName = fileName,
            Status = status
        )

        // Add file to Room Database
        lifecycleScope.launch(Dispatchers.IO) {
            dnFailedViewModel.addFiles(files)

        }



        val filesApi = FilesApi(
            SN = index.toString(),
            FolderName = folderName,
            FileName = fileName,
            Status = status
        )

        // Add file to Room Database
        lifecycleScope.launch(Dispatchers.IO) {
            mfilesViewModel.addFiles(filesApi)

        }



    }

    private fun extractFolderAndFile(url: String): Pair<String, String> {
        val baseUrl = "https://cp.cloudappserver.co.uk/app_base/public//CLO/DE_MO_2021001/"
        val relativePath = url.removePrefix(baseUrl)

        val folderName = relativePath.substringBeforeLast('/')
        val fileName = relativePath.substringAfterLast('/')

        return Pair(folderName, fileName)
    }

    // This function will be called when all files are processed
    private fun onAllFilesProcessed() {
        handler.postDelayed(Runnable {
            if (!Utility.foregroundParsingServiceClass(applicationContext)) {
                applicationContext.startService(Intent(applicationContext, ParsingSyncService::class.java))
                binding.textDownladByes.visibility = View.VISIBLE
            }

        },2000)

        mfilesViewModel.readAllData.observe(this@Kolo_Service_Manger, Observer { files ->
            displayFiles(files)
        })


    }


    private fun displayFiles(files: List<FilesApi>) {
        // Join file names or other relevant info into a single string
        val fileNames = files.joinToString("\n") { "${it.SN} : ${it.FolderName} / ${it.FileName} / ${it.Status}" }
        handler.postDelayed(Runnable {
            binding.textDisplaytext.text =  fileNames
        }, 3000)

    }

    private fun cleanTempFolder() {
        lifecycleScope.launch(Dispatchers.IO) {
            val Demo_Parsing_Folder = Constants.TEMP_PARS_FOLDER
            val getFolderClo = sharedP.getString(Constants.getFolderClo, "").toString()
            val getFolderSubpath = sharedP.getString(Constants.getFolderSubpath, "").toString()
            val saveDemoStorage = "/${Constants.Syn2AppLive}/$Demo_Parsing_Folder/$getFolderClo/$getFolderSubpath/App/"
            val directoryParsing = Environment.getExternalStorageDirectory().absolutePath + "/Download/" + saveDemoStorage
            val myFileParsing = File(directoryParsing)
            delete(myFileParsing)

            val parsingStorage_second = "/${Constants.Syn2AppLive}/$Demo_Parsing_Folder/$getFolderClo/$getFolderSubpath/"
            val fileNameParsing = "/App/"
            val dirParsing = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), parsingStorage_second)
            val myFile_Parsing = File(dirParsing, fileNameParsing)
            delete(myFile_Parsing)

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


    private val progressReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Constants.RECIVER_PROGRESS) {
                val status = intent.getStringExtra(Constants.ParsingStatusSync)
                if (status == Constants.PR_Downloading) {
                    Log.d("ProgressReceiver", "Status: $status")
                    // Update UI or take necessary actions
                    binding.textStatusProcess.text = status.toString()
                }

                if (status == Constants.PR_Refresh) {
                    Log.d("ProgressReceiver", "Refresh webviewpage...")
                    // Update UI or take necessary actions
                    binding.textStatusProcess.text = status.toString()

                }

                if (status == Constants.PR_Retry_Failed) {
                    binding.textStatusProcess.text = status.toString()

                }

                if (status == Constants.PR_Failed_Files_Number) {
                    handler.postDelayed(Runnable {
                        val getValue = sharedP.getString(Constants.numberFailedFiles, "").toString()
                        Log.d("ProgressReceiver", "failed files $getValue")
                        if (getValue.isNotEmpty()){
                            binding.textStatusProcess.text = getValue.toString()
                        }

                    }, 1000)
                }



                if (status == Constants.PR_Indexing_Files) {
                    Log.d("ProgressReceiver", "files indexing.")
                    // Update UI or take necessary actions
                    binding.textStatusProcess.text = status.toString()
                }

                val dLFileCounts = intent.getStringExtra(Constants.ParsingProgressBar)
                if (dLFileCounts != null){
                    Log.d("ProgressReceiver", "DL:$dLFileCounts")
                    binding.textFilecount.text = dLFileCounts.toString()
                }
            }

        }
    }

    private val progressDownloadBytesReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Constants.RECIVER_DOWNLOAD_BYTES_PROGRESS) {
                handler.postDelayed(Runnable {
                    val getValue = sharedP.getInt(Constants.ParsingDownloadBytesProgress, 0).toInt()
                    if (getValue != 0){
                        Log.d("ProgressReceiverBytes", "$getValue")
                        binding.progressBarPref.progress = getValue.toInt()

                        binding.textDownladByes.text = "$getValue%"
                    }
                },1000)

            }

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (progressReceiver!=null){
            unregisterReceiver(progressReceiver)
        }

        if (progressDownloadBytesReceiver!=null){
            unregisterReceiver(progressDownloadBytesReceiver)
        }

        if (Utility.foregroundParsingServiceClass(applicationContext)) {
            applicationContext.stopService(Intent(applicationContext, ParsingSyncService::class.java))
        }


        if (Utility.foregroundRetryParsingServiceClass(applicationContext)) {
            applicationContext.stopService(Intent(applicationContext, RetryParsingSyncService::class.java))
        }

    }

}
