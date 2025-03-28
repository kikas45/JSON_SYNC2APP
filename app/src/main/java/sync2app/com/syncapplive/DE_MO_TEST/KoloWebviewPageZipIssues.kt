package sync2app.com.syncapplive.DE_MO_TEST

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import sync2app.com.syncapplive.databinding.ActivityCrazyWebviewpageBinding


class KoloWebviewPageZipIssues : AppCompatActivity() {

    private lateinit var binding: ActivityCrazyWebviewpageBinding

    val baseUrl = "https://cp.cloudappserver.co.uk/app_base/public/CLO/DE_MO_2021001/App/"

    @SuppressLint("SetJavaScriptEnabled", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrazyWebviewpageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.editTextText4.setText(baseUrl)



        loadWebPage()

        binding.btnLoadWeb.setOnClickListener {
            binding.myWebViewer.reload()
        }


        binding.btnMove.setOnClickListener {
            finish()
            startActivity(Intent(applicationContext, KoloWebviewPageZipIssues::class.java))
        }

    }


    private fun loadWebPage() {
        // Configure WebView settings
        binding.myWebViewer.settings.apply {
            setUserAgentString(userAgentString.replace("wv", ""))
            CookieManager.getInstance().apply {
                setAcceptCookie(true)
                binding.myWebViewer.settings.javaScriptEnabled = true  // Enable JavaScript if needed
                acceptThirdPartyCookies(binding.myWebViewer)
            }
            allowFileAccess = true
            allowContentAccess = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            domStorageEnabled = true
            databaseEnabled = true
            setMediaPlaybackRequiresUserGesture(false)
        }

        // Set the click listener for loading the site
        binding.btnLoadSite.setOnClickListener {
            binding.myWebViewer.loadUrl(baseUrl)
        }

        // Configure WebViewClient
        binding.myWebViewer.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.progressBar3.visibility = View.VISIBLE
            }
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                binding.progressBar3.visibility = View.GONE
                view.clearHistory()
            }
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                Toast.makeText(view.context, "shouldOverrideUrlLoading", Toast.LENGTH_SHORT).show()
                return if (url == null || url.startsWith("http://") || url.startsWith("https://")) {
                    false
                } else true
            }
        }

        // Configure WebChromeClient
        binding.myWebViewer.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }
            override fun onReceivedTitle(view: WebView, title: String) {
                super.onReceivedTitle(view, title)
                if (title.contains("Object not found!") || title.contains("not found!")) {
                    Toast.makeText(view.context, "HTTP error 404", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Optional: Clear history if desired (avoid calling multiple times if not necessary)
        binding.myWebViewer.clearHistory()
    }


}
