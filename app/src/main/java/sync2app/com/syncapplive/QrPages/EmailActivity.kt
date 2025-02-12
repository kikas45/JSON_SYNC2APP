package sync2app.com.syncapplive.QrPages

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import sync2app.com.syncapplive.WebViewPage
import sync2app.com.syncapplive.R
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.databinding.ActivityEmailBinding
import java.io.File

class EmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmailBinding


    private val sharedBiometric: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_BIOMETRIC,
            Context.MODE_PRIVATE
        )
    }
    private val myDownloadClass: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.MY_DOWNLOADER_CLASS,
            Context.MODE_PRIVATE
        )
    }


    private var preferences: SharedPreferences? = null


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        binding.apply {
            if (preferences!!.getBoolean("darktheme", false)) {


                // Set status bar color
                window?.statusBarColor = Color.parseColor("#171616")
                // Set navigation bar color
                window?.navigationBarColor = Color.parseColor("#171616")

                // Ensure the text and icons are white
                WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
                WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false



                parentContainer.setBackgroundColor(resources.getColor(R.color.dark_layout_for_ui))

                // set text view

                textTitle.setTextColor(resources.getColor(R.color.white))


                textToemail.setTextColor(resources.getColor(R.color.white))
                textSubject.setTextColor(resources.getColor(R.color.white))
                textBodyMessage.setTextColor(resources.getColor(R.color.white))


                textSendMySms.setTextColor(resources.getColor(R.color.white))
                textSendMySms.setBackgroundResource(R.drawable.card_design_darktheme_outline)

                textDoNothing.setTextColor(resources.getColor(R.color.white))
                textDoNothing.setBackgroundResource(R.drawable.card_design_darktheme_outline)



                // fir back button
                val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.ic_baseline_arrow)
                drawable?.setColorFilter(ContextCompat.getColor(applicationContext, R.color.white), PorterDuff.Mode.SRC_IN)
                closeBs.setImageDrawable(drawable)



                //  for divider i..n
                divider21.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.light_gray))


            }
        }






        val get_imgToggleImageBackground = sharedBiometric.getString(Constants.imgToggleImageBackground, "")
        val get_imageUseBranding = sharedBiometric.getString(Constants.imageUseBranding, "")
        if (get_imgToggleImageBackground.equals(Constants.imgToggleImageBackground) && get_imageUseBranding.equals(Constants.imageUseBranding) ){
            loadBackGroundImage()
        }



        val emailTo: String = sharedBiometric.getString("emailTo", "").toString()
        val emailSubject: String = sharedBiometric.getString("emailSubject", "").toString()
        val emailBody: String = sharedBiometric.getString("emailBody", "").toString()


        binding.textSendMySms.setOnClickListener {
            sendEmail(emailTo, emailSubject, emailBody)
        }


        binding.apply {
            textToemail.text = "To:      $emailTo"
            textSubject.text = "Subject: $emailSubject"
            textBodyMessage.text = "Body:    $emailBody"
        }



        binding.closeBs.setOnClickListener {
            endMyActivity()
        }


        binding.textDoNothing.setOnClickListener {
            endMyActivity()
        }


       binding.closeBs.setOnClickListener {
            endMyActivity()
        }



    }


    private fun loadBackGroundImage() {

        val fileTypes = "app_background.png"
        val getFolderClo = myDownloadClass.getString(Constants.getFolderClo, "").toString()
        val getFolderSubpath = myDownloadClass.getString(Constants.getFolderSubpath, "").toString()

        val pathFolder = "/" + getFolderClo + "/" + getFolderSubpath + "/" + Constants.App + "/" + "Config"
        val folder =
            Environment.getExternalStorageDirectory().absolutePath + "/Download/${Constants.Syn2AppLive}/" + pathFolder
        val file = File(folder, fileTypes)

        if (file.exists()) {
            Glide.with(this).load(file).centerCrop().into(binding.backgroundImage)

        }
    }



    fun sendEmail(emailTo: String?, emailSubject: String?, emailBody: String?) {
        val uriText = "mailto:" + Uri.encode(emailTo) +
                "?subject=" + Uri.encode(emailSubject) +
                "&body=" + Uri.encode(emailBody)
        val uri = Uri.parse(uriText)

        // Intent for sending email
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = uri
        try {
            startActivity(emailIntent)
        } catch (e: ActivityNotFoundException) {
            // Handle case where no email app is available
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onBackPressed() {
        endMyActivity()
    }

    private fun endMyActivity() {
        val editor = sharedBiometric.edit()
        editor.remove("emailTo")
        editor.remove("emailSubject")
        editor.remove("emailBody")
        editor.apply()
        startActivity(Intent(applicationContext, WebViewPage::class.java))
        finish()
    }



}