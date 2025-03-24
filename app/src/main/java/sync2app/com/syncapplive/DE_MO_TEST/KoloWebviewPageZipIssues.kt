package sync2app.com.syncapplive.DE_MO_TEST

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import sync2app.com.syncapplive.databinding.ActivityCrazyWebviewpageBinding


class KoloWebviewPageZipIssues : AppCompatActivity(){

    private lateinit var binding: ActivityCrazyWebviewpageBinding

    val baseUrl = "https://cp.cloudappserver.co.uk/app_base/public/CLO/DE_MO_2021001/App/"

    @SuppressLint("SetJavaScriptEnabled", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrazyWebviewpageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.editTextText4.setText(baseUrl)


        setUpWebViewInints()

        binding.btnLoadWeb.setOnClickListener {
            binding.myWebViewer.reload()
        }

    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebViewInints() {

        binding.myWebViewer.settings.apply {

            loadsImagesAutomatically = true
            builtInZoomControls = true
            displayZoomControls = false

            loadWithOverviewMode = false
            useWideViewPort = false

            databaseEnabled = true
            domStorageEnabled = true
            setSupportZoom(false)

            setUserAgentString(userAgentString.replace("wv", ""))
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.acceptThirdPartyCookies(binding.myWebViewer)

            javaScriptEnabled = true

            allowFileAccess = false

            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = true

            cacheMode = WebSettings.LOAD_DEFAULT
            binding.myWebViewer.isSaveEnabled = true

            mediaPlaybackRequiresUserGesture = false

            layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
            binding.myWebViewer.webViewClient = AdvancedWebViewClient(binding.progressBar3)

            WebView.setWebContentsDebuggingEnabled(true)

            val getEditInput = binding.editTextText4.text.toString().trim()
            binding.myWebViewer.loadUrl(getEditInput)


        }
    }


    private class AdvancedWebViewClient(private val progressBar: ProgressBar?) : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            progressBar?.visibility = ProgressBar.VISIBLE
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            progressBar?.visibility = ProgressBar.GONE
        }
    }


}
