package sync2app.com.syncapplive.DE_MO_TEST

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.databinding.ActivityKoloWebviewPageBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class KoloWebviewPageZipIssues : AppCompatActivity() {
    private lateinit var binding: ActivityKoloWebviewPageBinding

    private val sharedP: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.MY_DOWNLOADER_CLASS,
            Context.MODE_PRIVATE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKoloWebviewPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnDownload.setOnClickListener {
            funUnZipFile()
        }

        binding.btnShowall.setOnClickListener {
           showToastMessage("Hello user")
        }
    }

    private fun funUnZipFile() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val getFolderClo = sharedP.getString(Constants.getFolderClo, "").toString()
                val getFolderSubpath = sharedP.getString(Constants.getFolderSubpath, "").toString()
                val zipFileName = sharedP.getString("Zip", "").toString()
                val fileName = sharedP.getString("fileNamy", "").toString()
                val extractedFolder = sharedP.getString(Constants.Extracted, "").toString()

                val finalFolderPath = "/$getFolderClo/$getFolderSubpath/$zipFileName"
                val finalFolderPathDesired = "/$getFolderClo/$getFolderSubpath/$extractedFolder"

                val directoryPathString = Environment.getExternalStorageDirectory().absolutePath + "/Download/${Constants.Syn2AppLive}/" + finalFolderPath
                val destinationFolder = File(Environment.getExternalStorageDirectory().absolutePath + "/Download/${Constants.Syn2AppLive}/" + finalFolderPathDesired)

                if (!destinationFolder.exists()) {
                    destinationFolder.mkdirs()
                }

                val myFile = File(directoryPathString, File.separator + fileName)
                if (myFile.exists()) {
                    extractZip(myFile.absolutePath, destinationFolder.absolutePath)
                } else {
                    withContext(Dispatchers.Main) {
                        showToastMessage("ZIP file not found: $directoryPathString")
                        allExtractionCompleted()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showToastMessage("An error occurred: ${e.localizedMessage}")
                }
            }
        }
    }


/*
    private fun extractZip(zipFilePath: String, destinationPath: String) {

    // No progress state
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val buffer = ByteArray(1024)
                val zipInputStream = ZipInputStream(FileInputStream(zipFilePath))

                var entry: ZipEntry? = zipInputStream.nextEntry
                while (entry != null) {
                    val entryFile = File(destinationPath, entry.name)
                    if (entry.isDirectory) {
                        entryFile.mkdirs()
                    } else {
                        val parentDir = entryFile.parentFile
                        if (!parentDir.exists()) parentDir.mkdirs()

                        FileOutputStream(entryFile).use { outputStream ->
                            var len: Int
                            while (zipInputStream.read(buffer).also { len = it } > 0) {
                                outputStream.write(buffer, 0, len)
                            }
                        }
                    }

                    MediaScannerConnection.scanFile(applicationContext, arrayOf(entryFile.absolutePath), null) { path, uri ->
                        Log.d("MediaScanner", "Scanned $path -> $uri")
                    }

                    entry = zipInputStream.nextEntry
                }

                zipInputStream.close()

                withContext(Dispatchers.Main) {
                    allExtractionCompleted()
                    showToastMessage("Extraction completed successfully.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showToastMessage("Error during extraction: ${e.localizedMessage}")
                    allExtractionCompleted()
                }
            }
        }
    }
*/


    private fun allExtractionCompleted() {
       // binding.progressBarPref.progress = 100
        showToastMessage(Constants.media_ready)
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }


    /////  with progress state


      private fun extractZip(zipFilePath: String, destinationPath: String) {
          // With progress Stae
      lifecycleScope.launch(Dispatchers.IO) {
          try {
              val zipFile = File(zipFilePath)
              val totalSize = zipFile.length() // Total size of the ZIP file in bytes
              var processedSize = 0L // Bytes processed so far

              val buffer = ByteArray(1024)
              val zipInputStream = ZipInputStream(FileInputStream(zipFile))

              var entry: ZipEntry? = zipInputStream.nextEntry
              while (entry != null) {
                  val entryFile = File(destinationPath, entry.name)
                  if (entry.isDirectory) {
                      entryFile.mkdirs()
                  } else {
                      val parentDir = entryFile.parentFile
                      if (!parentDir.exists()) parentDir.mkdirs()

                      FileOutputStream(entryFile).use { outputStream ->
                          var len: Int
                          while (zipInputStream.read(buffer).also { len = it } > 0) {
                              outputStream.write(buffer, 0, len)
                              processedSize += len

                              // Update progress on the main thread
                              val progress = ((processedSize.toDouble() / totalSize) * 100).toInt()
                              withContext(Dispatchers.Main) {
                                  binding.progressBarPref.progress = progress
                                  binding.textStatusProcess.text = "%$progress"
                              }
                          }
                      }
                  }

                  MediaScannerConnection.scanFile(applicationContext, arrayOf(entryFile.absolutePath), null) { path, uri ->
                      Log.d("MediaScanner", "Scanned $path -> $uri")
                  }

                  entry = zipInputStream.nextEntry
              }

              zipInputStream.close()

              withContext(Dispatchers.Main) {
                  allExtractionCompleted()
                  showToastMessage("Extraction completed successfully.")
              }
          } catch (e: Exception) {
              e.printStackTrace()
              withContext(Dispatchers.Main) {
                  showToastMessage("Error during extraction: ${e.localizedMessage}")
                  allExtractionCompleted()
              }
          }
      }
  }


}