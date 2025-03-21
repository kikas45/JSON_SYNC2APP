package sync2app.com.syncapplive.DE_MO_TEST
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import sync2app.com.syncapplive.additionalSettings.DownloadApisFilesActivityParsing
import sync2app.com.syncapplive.additionalSettings.myApiDownload.FilesApi
import sync2app.com.syncapplive.additionalSettings.myApiDownload.FilesViewModel
import sync2app.com.syncapplive.additionalSettings.myFailedDownloadfiles.DnFailedApi
import sync2app.com.syncapplive.additionalSettings.myFailedDownloadfiles.DnFailedViewModel
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.additionalSettings.utils.Utility
import sync2app.com.syncapplive.databinding.ActivityDemoServiceMainBinding
import java.io.File
import java.util.Objects


class Kolo_Download_Grab_files_Manager : AppCompatActivity() {

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


      //  binding.editTextText3.setText("https://cp.cloudappserver.co.uk/app_base/public//CLO/DE_MO_2021001/App/index.html")

       // val url ="https://www.cnbc.com/2025/02/03/putin-says-europe-will-stand-at-feet-of-master-as-trump-imposes-tariffs.html"
       // val url =  "https://www.w3schools.com/kotlin/trykotlin.php?filename=demo_helloworld"
        val url =  "https://www.gtbank.com/"


        binding.editTextText3.setText(url)

        binding.btnFecthData.setOnClickListener {

            cleanTempFolder()
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



        binding.btnStartDownload.setOnClickListener {
            val intent = Intent(applicationContext, DownloadApisFilesActivityParsing::class.java)
            startActivity(intent)
            finish()
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
        val fullPath = extractFolderAndFile(url)
        if (fullPath.isEmpty()) return // Skip invalid URLs

        // Extract folder and file name
        val folderName = fullPath.substringBeforeLast("/", "") // Everything before the last "/"
        val fileName = fullPath.substringAfterLast("/", "") // The actual file name

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

        lifecycleScope.launch(Dispatchers.IO) {
            dnFailedViewModel.addFiles(files)
        }

        val filesApi = FilesApi(
            SN = index.toString(),
            FolderName = folderName,
            FileName = fileName,
            Status = status
        )

        lifecycleScope.launch(Dispatchers.IO) {
            mfilesViewModel.addFiles(filesApi)
        }
    }


    private fun extractFolderAndFile(url: String): String {

        val get_tMaster: String = sharedP.getString(Constants.get_ModifiedUrl, "").toString()
        val get_UserID: String = sharedP.getString(Constants.getSavedCLOImPutFiled, "").toString()
        val get_LicenseKey: String = sharedP.getString(Constants.getSaveSubFolderInPutFiled, "").toString()

        val originalUrl = "$get_tMaster/$get_UserID/$get_LicenseKey/"

        Log.d("SOLOMON", "extractFolderAndFile: $originalUrl")

       // val baseUrlPattern = "https?://cp\\.cloudappserver\\.co\\.uk/app_base/public/CLO/DE_MO_2021001/?".toRegex()

        val baseUrlPattern = originalUrl.toRegex()

        // Remove base URL (handling variations)
        val relativePath = url.replace(baseUrlPattern, "")

        // Ensure we are working within the "App" folder
        val appIndex = relativePath.indexOf("App/")
        if (appIndex == -1) return "" // Return empty if "App" folder is missing

        // Extract everything after "App/"
        val appRelativePath = relativePath.substring(appIndex)

        // Ensure it starts with "/"
        return "/$appRelativePath"
    }







    // This function will be called when all files are processed
    private fun onAllFilesProcessed() {

            mfilesViewModel.readAllData.observe(this@Kolo_Download_Grab_files_Manager, Observer { files ->
                handler.postDelayed(Runnable {
                    displayFiles(files)
                },2000)

            })

    }


    private fun displayFiles(files: List<FilesApi>) {
        // Join file names or other relevant info into a single string
        val fileNames = files.joinToString("\n") { "${it.SN} : ${it.FolderName} / ${it.FileName} / ${it.Status}" }
        handler.postDelayed(Runnable {
            binding.textDisplaytext.text =  fileNames

            Log.d("The_List_Of_Files", fileNames)
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


}
