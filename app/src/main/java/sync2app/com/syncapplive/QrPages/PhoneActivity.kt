package sync2app.com.syncapplive.QrPages

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
import sync2app.com.syncapplive.databinding.ActivityPhoneBinding
import java.io.File

class PhoneActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhoneBinding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneBinding.inflate(layoutInflater)
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


                textBodyMessage.setTextColor(resources.getColor(R.color.white))

                textCallNumber.setTextColor(resources.getColor(R.color.white))
                textCallNumber.setBackgroundResource(R.drawable.card_design_darktheme_outline)

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



        val phoneNumber: String = sharedBiometric.getString("phoneNumber", "").toString()

        binding.textBodyMessage.text = phoneNumber


        binding.closeBs.setOnClickListener {
            endMyActivity()
        }


        binding.textDoNothing.setOnClickListener {
            endMyActivity()
        }

        binding.textCallNumber.setOnClickListener {
            makeCall(phoneNumber)
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



    private  fun makeCall(phoneNumber: String?) {


        // Create intent to dial the phone number
        val callIntent = Intent(Intent.ACTION_DIAL)

        // Set the phone number in the data field of the intent
        callIntent.data = Uri.parse("tel:" + Uri.encode(phoneNumber))
        try {
            // Start the activity to initiate the call
            startActivity(callIntent)
            finish()
        } catch (e: ActivityNotFoundException) {
            // Handle case where no dialer app is available
            Toast.makeText(this, "No dialer app found", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onBackPressed() {
        endMyActivity()
    }

    private fun endMyActivity() {
        val editor = sharedBiometric.edit()
        editor.remove("phoneNumber")
        editor.apply()
        startActivity(Intent(applicationContext, WebViewPage::class.java))
        finish()
    }
}