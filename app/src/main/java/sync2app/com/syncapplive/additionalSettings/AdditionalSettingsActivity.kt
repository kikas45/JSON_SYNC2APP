package sync2app.com.syncapplive.additionalSettings
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.app.admin.SystemUpdatePolicy
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.UserManager
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import sync2app.com.syncapplive.DE_MO_TEST.KoloWebviewPageZipIssues
import sync2app.com.syncapplive.DE_MO_TEST.Kolo_ConfigDOwwnload
import sync2app.com.syncapplive.DE_MO_TEST.Kolo_Download_Grab_files_Manager
import sync2app.com.syncapplive.R
import sync2app.com.syncapplive.SettingsActivityKT
import sync2app.com.syncapplive.additionalSettings.autostartAppOncrash.Methods
import sync2app.com.syncapplive.additionalSettings.cloudAppsync.schedules.ScheduleMediaActivity
import sync2app.com.syncapplive.additionalSettings.devicelock.MyDeviceAdminReceiver
import sync2app.com.syncapplive.additionalSettings.scanutil.CustomShortcuts
import sync2app.com.syncapplive.additionalSettings.scanutil.DefaultCustomShortCut
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.additionalSettings.utils.Utility
import sync2app.com.syncapplive.databinding.ActivityAppAdminBinding
import sync2app.com.syncapplive.databinding.CustomServerUrlLayoutBinding
import sync2app.com.syncapplive.databinding.CustomShortCutLayoutBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class AdditionalSettingsActivity : AppCompatActivity() {


    private fun isAdmin() = mDevicePolicyManager.isDeviceOwnerApp(packageName)

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

    private lateinit var mAdminComponentName: ComponentName
    private lateinit var mDevicePolicyManager: DevicePolicyManager

    companion object {
        const val LOCK_ACTIVITY_KEY = "pl.mrugacz95.kiosk.MainActivity"
    }


    private lateinit var binding: ActivityAppAdminBinding

    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }


    private val sharedTVAPPModePreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_TV_APP_MODE, Context.MODE_PRIVATE
        )
    }

    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }


    // setting for shortCut icons
    private val SELECT_PICTURE = 200
    private var isImagePicked = false
    var imagePicked: Uri? = null
    private lateinit var custImageView: ImageView
    private lateinit var customimageRadipoButton: RadioButton



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.textTitle.setOnClickListener {
           /// startActivity(Intent(applicationContext, Kolo_ConfigDOwwnload::class.java))
          //  startActivity(Intent(applicationContext, Kolo_Download_Grab_files_Manager::class.java))
        }


        initUtilityForOnCreateView()

        initView()

        setUpFullScreenWindows()

    }

    private fun setUpFullScreenWindows() {
        val get_INSTALL_TV_JSON_USER_CLICKED = sharedTVAPPModePreferences.getString(Constants.INSTALL_TV_JSON_USER_CLICKED, "").toString()
        if (get_INSTALL_TV_JSON_USER_CLICKED != Constants.INSTALL_TV_JSON_USER_CLICKED) {
            val img_imgImmesriveModeToggle = preferences.getBoolean(Constants.immersive_mode, false)
            if (img_imgImmesriveModeToggle){
                Utility.hideSystemBars(window)
            }else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }


        }else{

            val immersive_Mode_APP = sharedTVAPPModePreferences.getBoolean(Constants.immersive_Mode_APP, false)
            if (immersive_Mode_APP) {
                Utility.hideSystemBars(window)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }

        }
    }



    private fun initUtilityForOnCreateView() {
        applyOritenation()



        binding.apply {
            // Hide Some Buttons for Mobile Mode
            val sharedBiometricPref = getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)
            val get_AppMode = sharedBiometricPref.getString(Constants.MY_TV_OR_APP_MODE, "").toString()
            if (get_AppMode != Constants.TV_Mode) {

                imageView43.visibility = View.GONE
                textScheduleMedia.visibility = View.GONE

                imageView44.visibility = View.GONE
                imageView10.visibility = View.GONE

                divider48.visibility = View.GONE

                textSyncManager.visibility = View.GONE
                imageView17.visibility = View.GONE
                divider15.visibility = View.GONE

                textCameraSettings.visibility = View.GONE
                imageView54.visibility = View.GONE
                imageView55.visibility = View.GONE
                divider19.visibility = View.GONE
                divider57.visibility = View.GONE

            }else{
                textEbnableLockScreen.visibility = View.GONE
                imageView1.visibility = View.GONE
                imgEnableLockScreen.visibility = View.GONE
                divider8.visibility = View.GONE


            }


        }


        binding.apply {
            if (preferences!!.getBoolean("darktheme", false)) {


                // Set status bar color
                window?.statusBarColor = Color.parseColor("#171616")
                // Set navigation bar color
                window?.navigationBarColor = Color.parseColor("#171616")

                // Ensure the text and icons are white
                WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
                    false
                WindowInsetsControllerCompat(
                    window,
                    window.decorView
                ).isAppearanceLightNavigationBars = false



                parentContainer.setBackgroundColor(resources.getColor(R.color.dark_layout_for_ui))

                // set text view

                textTitle.setTextColor(resources.getColor(R.color.white))


                textEbnableLockScreen.setTextColor(resources.getColor(R.color.white))
                textenaleBootFromscreen.setTextColor(resources.getColor(R.color.white))
                textAllowFingerPrint.setTextColor(resources.getColor(R.color.white))
                textSyncManager.setTextColor(resources.getColor(R.color.white))
                textScheduleMedia.setTextColor(resources.getColor(R.color.white))
                textPassword.setTextColor(resources.getColor(R.color.white))
                textManageShortCuts.setTextColor(resources.getColor(R.color.white))
                textShareApp.setTextColor(resources.getColor(R.color.white))
                textCameraSettings.setTextColor(resources.getColor(R.color.white))
                textDeviceSettings.setTextColor(resources.getColor(R.color.white))
                textAppSettings.setTextColor(resources.getColor(R.color.white))
                textWifiSettings.setTextColor(resources.getColor(R.color.white))
                textSystemInfo.setTextColor(resources.getColor(R.color.white))
                textVolume.setTextColor(resources.getColor(R.color.white))
                textMaintencePage.setTextColor(resources.getColor(R.color.white))


                // fir back button
                val drawable =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_baseline_arrow)
                drawable?.setColorFilter(
                    ContextCompat.getColor(applicationContext, R.color.white),
                    PorterDuff.Mode.SRC_IN
                )
                closeBs.setImageDrawable(drawable)


                // for all forward icon
                val drawable_imageView6 =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_arrow_move_front)
                drawable_imageView6?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.white
                    ), PorterDuff.Mode.SRC_IN
                )
                imageView6.setImageDrawable(drawable_imageView6)
                imageView17.setImageDrawable(drawable_imageView6)
                imageView43.setImageDrawable(drawable_imageView6)
                imageView4.setImageDrawable(drawable_imageView6)
                imageView.setImageDrawable(drawable_imageView6)
                imageView5.setImageDrawable(drawable_imageView6)
                imageView54.setImageDrawable(drawable_imageView6)
                imageView6.setImageDrawable(drawable_imageView6)
                imageViewDeviceSettings2.setImageDrawable(drawable_imageView6)
                imageViewDeviceSettings3.setImageDrawable(drawable_imageView6)
                imageViewDeviceSettings4.setImageDrawable(drawable_imageView6)
                imageViewDeviceSettings5.setImageDrawable(drawable_imageView6)
                imageViewDeviceSettings.setImageDrawable(drawable_imageView6)


                // for nav icons
                val drawable_imageView1 =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic__lock_icon_24)
                drawable_imageView1?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.deep_blue_light_extra
                    ), PorterDuff.Mode.SRC_IN
                )
                imageView1.setImageDrawable(drawable_imageView1)


                val drawable_imageView8 =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_power_icon)
                drawable_imageView8?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.deep_blue_light_extra
                    ), PorterDuff.Mode.SRC_IN
                )
                imageView8.setImageDrawable(drawable_imageView8)


                val drawable_imageView9 =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_fingerprint)
                drawable_imageView9?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.deep_blue_light_extra
                    ), PorterDuff.Mode.SRC_IN
                )
                imageView9.setImageDrawable(drawable_imageView9)

                val drawable_imageView10 =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_wifi_icon_bar_24)
                drawable_imageView10?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.deep_blue_light_extra
                    ), PorterDuff.Mode.SRC_IN
                )
                imageView10.setImageDrawable(drawable_imageView10)

                val drawable_imageView44 =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_schedule)
                drawable_imageView44?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.deep_blue_light_extra
                    ), PorterDuff.Mode.SRC_IN
                )
                imageView44.setImageDrawable(drawable_imageView44)

                val drawable_imageView11 =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_key_password)
                drawable_imageView11?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.deep_blue_light_extra
                    ), PorterDuff.Mode.SRC_IN
                )
                imageView11.setImageDrawable(drawable_imageView11)

                val drawable_imageView12 =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_short_path)
                drawable_imageView12?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.deep_blue_light_extra
                    ), PorterDuff.Mode.SRC_IN
                )
                imageView12.setImageDrawable(drawable_imageView12)

                val drawable_imageView55 =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_camera)
                drawable_imageView55?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.deep_blue_light_extra
                    ), PorterDuff.Mode.SRC_IN
                )
                imageView55.setImageDrawable(drawable_imageView55)

                val drawable_imageView13 =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_share_24)
                drawable_imageView13?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.deep_blue_light_extra
                    ), PorterDuff.Mode.SRC_IN
                )
                imageView13.setImageDrawable(drawable_imageView13)


                val drawable_imageView14 =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_device_settings)
                drawable_imageView14?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.deep_blue_light_extra
                    ), PorterDuff.Mode.SRC_IN
                )
                imageView14.setImageDrawable(drawable_imageView14)

                val drawable_imageView15 =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_app_settings_icon)
                drawable_imageView15?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.deep_blue_light_extra
                    ), PorterDuff.Mode.SRC_IN
                )
                imageView15.setImageDrawable(drawable_imageView15)

                val drawable_imageView16 =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_wifi_settings_icon)
                drawable_imageView16?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.deep_blue_light_extra
                    ), PorterDuff.Mode.SRC_IN
                )
                imageView16.setImageDrawable(drawable_imageView16)

                val drawable_imageView18 =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_system_memory_24)
                drawable_imageView18?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.deep_blue_light_extra
                    ), PorterDuff.Mode.SRC_IN
                )
                imageView18.setImageDrawable(drawable_imageView18)


                val drawable_imageView7 =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_volume_icon_24)
                drawable_imageView7?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.deep_blue_light_extra
                    ), PorterDuff.Mode.SRC_IN
                )
                imageView7.setImageDrawable(drawable_imageView7)

                val drawable_imageView25 =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_settings_svg)
                drawable_imageView25?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.deep_blue_light_extra
                    ), PorterDuff.Mode.SRC_IN
                )
                imageView25.setImageDrawable(drawable_imageView25)


                // Define colors for different states
                val thumbColorStateList = ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_checked),
                        intArrayOf(-android.R.attr.state_checked)
                    ),
                    intArrayOf(
                        Color.LTGRAY,  // Color when checked
                        Color.LTGRAY   // Color when unchecked
                    )
                )

                val trackColorStateList = ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_enabled),
                        intArrayOf(-android.R.attr.state_enabled)
                    ),
                    intArrayOf(
                        Color.DKGRAY,  // Color when enabled
                        Color.LTGRAY   // Color when disabled
                    )
                )

                // Apply the color state lists
                imgEnableLockScreen.thumbTintList = thumbColorStateList
                imgEnableLockScreen.trackTintList = trackColorStateList

                imgEnableAutoBoot.thumbTintList = thumbColorStateList
                imgEnableAutoBoot.trackTintList = trackColorStateList

                imgAllowFingerPrint.thumbTintList = thumbColorStateList
                imgAllowFingerPrint.trackTintList = trackColorStateList


                //  for divider i..n
                divider21.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.light_gray
                    )
                )
                divider12.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.light_gray
                    )
                )
                divider8.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.light_gray
                    )
                )
                divider9.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.light_gray
                    )
                )
                divider12.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.light_gray
                    )
                )
                divider14.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.light_gray
                    )
                )
                divider15.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.light_gray
                    )
                )
                divider48.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.light_gray
                    )
                )
                divider17.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.light_gray
                    )
                )
                divider20.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.light_gray
                    )
                )
                divider22.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.light_gray
                    )
                )
                divider24.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.light_gray
                    )
                )
                divider4.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.light_gray
                    )
                )
                divider6.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.light_gray
                    )
                )
                divider37.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.light_gray
                    )
                )
                divider18.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.light_gray
                    )
                )
                divider52.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.light_gray
                    )
                )
                divider19.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.light_gray
                    )
                )
                divider57.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.light_gray
                    )
                )
                divider23.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.light_gray
                    )
                )


            }
        }


        //add exception
        Methods.addExceptionHandler(this)

        val get_imgToggleImageBackground =
            sharedBiometric.getString(Constants.imgToggleImageBackground, "")
        val get_imageUseBranding = sharedBiometric.getString(Constants.imageUseBranding, "")
        if (get_imgToggleImageBackground.equals(Constants.imgToggleImageBackground) && get_imageUseBranding.equals(
                Constants.imageUseBranding
            )
        ) {
            loadBackGroundImage()
        }


        mAdminComponentName = MyDeviceAdminReceiver.getComponentName(this)
        mDevicePolicyManager =
            getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager


        // we will handle this call back when we enable device owner later

        if (Build.VERSION.SDK_INT >= 30) {
            // not  device owner
        } else {
            mDevicePolicyManager.removeActiveAdmin(mAdminComponentName);
        }

        val isAdmin = isAdmin()

        if (isAdmin) {
            //   Snackbar.make(binding.content, "device_owner", Snackbar.LENGTH_SHORT).show()
        } else {
            //    Snackbar.make(binding.content, "not_device_owner", Snackbar.LENGTH_SHORT).show()

        }



        binding.apply {


            textCameraSettings.setOnClickListener {
                val intent = Intent(applicationContext, UsbCamConfigActivity::class.java)
                // val intent = Intent(applicationContext, WenCamActivity::class.java)
                startActivity(intent)
                finish()
            }



            textSyncManager.setOnClickListener {
                val editor = sharedBiometric.edit()
                editor.putString(Constants.SAVE_NAVIGATION, Constants.AdditionNalPage)
                editor.apply()

                val intent = Intent(applicationContext, ReSyncActivity::class.java)
                startActivity(intent)
                finish()


            }


            textScheduleMedia.setOnClickListener {

                val intent = Intent(applicationContext, ScheduleMediaActivity::class.java)
                startActivity(intent)
                finish()


            }



            textSystemInfo.setOnClickListener {
                val intent = Intent(applicationContext, SystemInfoActivity::class.java)
                startActivity(intent)
                finish()
            }




            textDeviceSettings.setOnClickListener {
                val intent = Intent(Settings.ACTION_SETTINGS)
                startActivity(intent)


            }





            textVolume.setOnClickListener {
                val audioManager =
                    applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

                // Adjust the volume (you can change AudioManager.STREAM_MUSIC to other types)
                audioManager.adjustVolume(AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI)
            }



            textAppSettings.setOnClickListener {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }

            textWifiSettings.setOnClickListener {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(intent)
            }


            textPassword.setOnClickListener {

                val editor = sharedBiometric.edit()
                editor.putString(Constants.SAVE_NAVIGATION, Constants.AdditionNalPage)
                editor.apply()

                val intent = Intent(applicationContext, PasswordActivity::class.java)
                startActivity(intent)
                finish()
            }



            textMaintencePage.setOnClickListener {
                val intent = Intent(applicationContext, MaintenanceActivity::class.java)
                startActivity(intent)
                finish()

                val editor = sharedBiometric.edit()
                editor.putString(Constants.SAVE_NAVIGATION, Constants.AdditionNalPage)
                editor.apply()

            }





            textShareApp.setOnClickListener {
                try {
                    shareMyApk()
                } catch (_: Exception) {
                }
            }


            textManageShortCuts.setOnClickListener {
                showPopShortCustom()
            }


            closeBs.setOnClickListener {
                val intent = Intent(applicationContext, SettingsActivityKT::class.java)
                startActivity(intent)
                finish()

            }


        }

    }


    private fun initView() {


        binding.apply {


            val editor = sharedBiometric.edit()


            val imgFingerPrint = sharedBiometric.getString(Constants.imgAllowFingerPrint, "").toString()
            val autoBooatApp = sharedBiometric.getString(Constants.imgEnableAutoBoot, "").toString()
            val lockDown = sharedBiometric.getString(Constants.imgEnableLockScreen, "").toString()


            imgEnableAutoBoot.isChecked = autoBooatApp.equals(Constants.imgEnableAutoBoot)
            imgAllowFingerPrint.isChecked = imgFingerPrint.equals(Constants.imgAllowFingerPrint)
            imgEnableLockScreen.isChecked = lockDown.equals(Constants.imgEnableLockScreen)


            if (lockDown == Constants.imgEnableLockScreen){
                textEbnableLockScreen.text = "Device Lock Down Enabled"
            }else{
                textEbnableLockScreen.text = "Device Lock Down Disabled"
            }

            if (imgFingerPrint == Constants.imgAllowFingerPrint){
                textAllowFingerPrint.text = "Fingerprint Enabled"
            }else{
                textAllowFingerPrint.text = "Fingerprint Disabled"
            }

            if (autoBooatApp == Constants.imgEnableAutoBoot){
                textenaleBootFromscreen.text = "Auto Boot Enabled"
            }else{
                textenaleBootFromscreen.text = "Auto Boot Disabled"
            }


            /// enable the lockscreen
            imgEnableLockScreen.setOnCheckedChangeListener { compoundButton, isValued ->
                if (compoundButton.isChecked) {

                    setKioskPolicies(true, isAdmin())

                    handler.postDelayed(Runnable {
                        val edito333r = sharedBiometric.edit()
                        edito333r.putString(Constants.imgEnableLockScreen, "imgEnableLockScreen")
                        edito333r.apply()
                        textEbnableLockScreen.text = "Device Lock Down Enabled"

                    }, 800)

                } else {

                    setKioskPolicies(false, isAdmin())

                    handler.postDelayed(Runnable {

                        val edito333r = sharedBiometric.edit()
                        edito333r.remove(Constants.imgEnableLockScreen)
                        edito333r.apply()
                        val intent = Intent(applicationContext, AdditionalSettingsActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.putExtra(LOCK_ACTIVITY_KEY, false)
                        startActivity(intent)
                        finish()
                        textEbnableLockScreen.text = "Device Lock Down Disabled"

                    }, 500)

                }
            }



            // enable finger print
            imgAllowFingerPrint.setOnCheckedChangeListener { compoundButton, isValued -> // we are putting the values into SHARED PREFERENCE
                if (compoundButton.isChecked) {
                    editor.putString(Constants.imgAllowFingerPrint, "imgAllowFingerPrint")
                    editor.apply()
                    textAllowFingerPrint.text = "Fingerprint Enabled"
                } else {

                    editor.remove(Constants.imgAllowFingerPrint)
                    editor.apply()
                    textAllowFingerPrint.text = "Fingerprint Disabled"
                }
            }




            /// enable the Auto Boot
            imgEnableAutoBoot.setOnCheckedChangeListener { compoundButton, isValued ->
                if (compoundButton.isChecked) {
                    optimizedbattry()

                } else {
                    // stop lock screen
                    editor.remove(Constants.imgEnableAutoBoot)
                    editor.apply()
                    binding.textenaleBootFromscreen.text = "Auto Boot Disabled"
                }
            }

        }
    }

    @SuppressLint("BatteryLife")
    private fun optimizedbattry() {
        try {
            val packageName = packageName

            if (!Settings.System.canWrite(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
                binding.textenaleBootFromscreen.text = "Auto Boot Disabled"
            }


            if (Settings.System.canWrite(applicationContext) && isIgnoringBatteryOptimizations(applicationContext, packageName)) {
                val editor = sharedBiometric.edit()
                editor.putString(Constants.imgEnableAutoBoot, Constants.imgEnableAutoBoot)
                editor.putString(Constants.BattryOptimzationOkay, Constants.BattryOptimzationOkay)
                editor.apply()
                binding.imgEnableAutoBoot.isChecked = true
                binding.textenaleBootFromscreen.text = "Auto Boot Enabled"

            }



        } catch (e: Exception) {
        }
    }


    private fun isIgnoringBatteryOptimizations(context: Context, packageName: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.isIgnoringBatteryOptimizations(packageName)
        } else {
            false
        }
    }


    override fun onResume() {
        super.onResume()
        try {

            val packageName = packageName
            if (Settings.System.canWrite(this)) {
                if (!isIgnoringBatteryOptimizations(this, packageName)) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                }
            }


            val getBattryOptimization = sharedBiometric.getString(Constants.BattryOptimzationOkay, "").toString()
            if (Settings.System.canWrite(applicationContext) && isIgnoringBatteryOptimizations(applicationContext, packageName) && getBattryOptimization != Constants.BattryOptimzationOkay) {
                val editor = sharedBiometric.edit()
                editor.putString(Constants.imgEnableAutoBoot, Constants.imgEnableAutoBoot)
                editor.putString(Constants.BattryOptimzationOkay, Constants.BattryOptimzationOkay)
                editor.apply()
                binding.imgEnableAutoBoot.isChecked = true
                binding.textenaleBootFromscreen.text = "Auto Boot Enabled"
            }

        } catch (e: Exception) {
        }

    }



    private fun shareMyApk() {
        try {
            val nameOfApk = "Syn2App.apk"
            val baseApkLocation =
                applicationContext.packageManager.getApplicationInfo(
                    applicationContext.packageName,
                    PackageManager.GET_META_DATA
                ).sourceDir

            val baseApk = File(baseApkLocation)

            val path = Environment.getExternalStorageDirectory()
                .toString() + "/Download/Syn2AppLive/APK/"

            val dir = File(path)
            if (!dir.exists()) {
                dir.mkdirs()
            }

            // Copy the .apk file to downloads directory
            val destination = File(
                path, nameOfApk
            )
            if (destination.exists()) {
                destination.delete()
            }

            destination.createNewFile()
            val input = FileInputStream(baseApk)
            val output = FileOutputStream(destination)
            val buffer = ByteArray(1024)
            var length: Int = input.read(buffer)
            while (length > 0) {
                output.write(buffer, 0, length)
                length = input.read(buffer)
            }
            output.flush()
            output.close()
            input.close()


            val directoryPath =
                Environment.getExternalStorageDirectory().absolutePath + "/Download/Syn2AppLive/APK/$nameOfApk"

            val nameOfpackage = this.packageName

            val fileUri = FileProvider.getUriForFile(
                applicationContext,
                // "$nameOfpackage.fileprovider",
                "$nameOfpackage.provider",
                File(directoryPath)
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, fileUri)
            }

            startActivity(Intent.createChooser(shareIntent, "Share APK using"))

            Log.d("shareMyApk", "shareMyApk: sucesss ")


        } catch (e: Exception) {
            Log.d("TAG", "shareMyApk: \"Failed To Share The App${e.message}")
            e.printStackTrace()
        }
    }


    override fun onBackPressed() {

        val intent = Intent(applicationContext, SettingsActivityKT::class.java)
        startActivity(intent)
        finish()

    }

    override fun onDestroy() {
        super.onDestroy()

    }


    private fun setKioskPolicies(enable: Boolean, isAdmin: Boolean) {
        val editor = sharedBiometric.edit()
        if (isAdmin) {
            setRestrictions(enable)
            enableStayOnWhilePluggedIn(enable)
            setUpdatePolicy(enable)
            setAsHomeApp(enable)
            setKeyGuardEnabled(enable)
        } else {
            setLockTask(enable, isAdmin)
            setImmersiveMode(enable)
            editor.remove("imgEnableLockScreen")
            editor.apply()

        }
    }

    // region restrictions
    private fun setRestrictions(disallow: Boolean) {
        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, disallow)
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, disallow)
        setUserRestriction(UserManager.DISALLOW_ADD_USER, disallow)
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, disallow)
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, disallow)
        mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, disallow)
    }

    private fun setUserRestriction(restriction: String, disallow: Boolean) = if (disallow) {
        mDevicePolicyManager.addUserRestriction(mAdminComponentName, restriction)
    } else {
        mDevicePolicyManager.clearUserRestriction(mAdminComponentName, restriction)
    }
    // endregion

    private fun enableStayOnWhilePluggedIn(active: Boolean) = if (active) {
        mDevicePolicyManager.setGlobalSetting(
            mAdminComponentName,
            Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
            (BatteryManager.BATTERY_PLUGGED_AC
                    or BatteryManager.BATTERY_PLUGGED_USB
                    or BatteryManager.BATTERY_PLUGGED_WIRELESS).toString()
        )
    } else {
        mDevicePolicyManager.setGlobalSetting(
            mAdminComponentName,
            Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
            "0"
        )
    }

    private fun setLockTask(start: Boolean, isAdmin: Boolean) {
        if (isAdmin) {
            mDevicePolicyManager.setLockTaskPackages(
                mAdminComponentName, if (start) arrayOf(packageName) else arrayOf()
            )
        }
        if (start) {
            startLockTask()
        } else {
            stopLockTask()
        }
    }

    private fun setUpdatePolicy(enable: Boolean) {
        if (enable) {
            mDevicePolicyManager.setSystemUpdatePolicy(
                mAdminComponentName,
                SystemUpdatePolicy.createWindowedInstallPolicy(60, 120)
            )
        } else {
            mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName, null)
        }
    }

    private fun setAsHomeApp(enable: Boolean) {
        if (enable) {
            val intentFilter = IntentFilter(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            mDevicePolicyManager.addPersistentPreferredActivity(
                mAdminComponentName,
                intentFilter,
                ComponentName(packageName, AdditionalSettingsActivity::class.java.name)
            )
        } else {
            mDevicePolicyManager.clearPackagePersistentPreferredActivities(
                mAdminComponentName, packageName
            )
        }
    }

    private fun setKeyGuardEnabled(enable: Boolean) {
        mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, !enable)
    }

    @Suppress("DEPRECATION")
    private fun setImmersiveMode(enable: Boolean) {
        if (enable) {
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.decorView.systemUiVisibility = flags
        } else {
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            window.decorView.systemUiVisibility = flags
        }
    }

    private fun loadBackGroundImage() {

        val fileTypes = "app_background.png"
        val getFolderClo = myDownloadClass.getString(Constants.getFolderClo, "").toString()
        val getFolderSubpath = myDownloadClass.getString(Constants.getFolderSubpath, "").toString()

        val pathFolder =
            "/" + getFolderClo + "/" + getFolderSubpath + "/" + Constants.App + "/" + "Config"
        val folder =
            Environment.getExternalStorageDirectory().absolutePath + "/Download/${Constants.Syn2AppLive}/" + pathFolder
        val file = File(folder, fileTypes)

        if (file.exists()) {
            Glide.with(this).load(file).centerCrop().into(binding.backgroundImage)

        }

    }


    @SuppressLint("MissingInflatedId", "NewApi")
    private fun showPopShortCustom() {
        val bindingCM: CustomShortCutLayoutBinding = CustomShortCutLayoutBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        builder.setView(bindingCM.getRoot())
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(false)
        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        }

        custImageView = bindingCM.custImageView as ImageView
        customimageRadipoButton = bindingCM.customimageRadipoButton




        var defaultradio = false
        var customradio = false


        val textLogin = bindingCM.textLogin
        val editTextText = bindingCM.editTextText
        val imgCancel = bindingCM.imgCancel
        val imgCancelSmall = bindingCM.imgCancelSmall
        custImageView =bindingCM.custImageView
        val defaultImageFaltImage = bindingCM.defaultImageFaltImage
        val defaultImageRadioButton = bindingCM.defaultImageRadioButton
        customimageRadipoButton = bindingCM.customimageRadipoButton


        imgCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        imgCancelSmall.setOnClickListener {
            alertDialog.dismiss()
        }

        custImageView.setOnClickListener {
            imageChooser()
            customimageRadipoButton.isChecked = true
            defaultImageRadioButton.isChecked = false
            defaultradio = false
            customradio = true
            bindingCM.editTextText.setText("")

        }



        defaultImageFaltImage.setOnClickListener {
            defaultImageRadioButton.isChecked = true
            customimageRadipoButton.isChecked = false
            bindingCM.editTextText.setText("Sync2App")

        }



        defaultImageRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                defaultImageRadioButton.isChecked = true
                customimageRadipoButton.isChecked = false
                defaultradio = true
                customradio = false
                bindingCM.editTextText.setText("Sync2App")
            }
        }



        textLogin.setOnClickListener {
            hideKeyBoard(editTextText)

            val getEditString = editTextText.text.toString().trim()

            if (defaultradio) {
                if (getEditString.isNotEmpty()) {
                    //  val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imagePicked)
                    if (Build.VERSION.SDK_INT >= 25) {
                        DefaultCustomShortCut.setUp(applicationContext, getEditString)
                    }
                    if (Build.VERSION.SDK_INT >= 28) {
                        shortcutPin(applicationContext, Constants.shortcut_website_id, 0)
                    }
                    alertDialog.dismiss()
                } else {
                    showToastMessage("Add name")
                }
            } else {
                 showToastMessage("Image and name required")
            }



            if (customradio) {
                if (getEditString.isNotEmpty() && isImagePicked) {
                    if (Build.VERSION.SDK_INT >= 25) {
                        val bitmap = MediaStore.Images.Media.getBitmap(applicationContext.contentResolver, imagePicked)
                        CustomShortcuts.setUp(applicationContext, getEditString, bitmap)
                    }
                    if (Build.VERSION.SDK_INT >= 28) {
                        shortcutPin(applicationContext, Constants.shortcut_messages_id, 1)
                    }
                    alertDialog.dismiss()
                } else {
                    showToastMessage("Image and name required")
                }
            } else {
                 showToastMessage("Image and name required")
            }
        }




        alertDialog.show()
    }



    private fun imageChooser() {
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                val selectedImageUri: Uri? = data?.data
                if (selectedImageUri != null) {
                    custImageView.setImageURI(selectedImageUri)
                    customimageRadipoButton.isChecked = true
                    isImagePicked = true

                    imagePicked = selectedImageUri

                } else {
                    customimageRadipoButton.isChecked = false
                    isImagePicked = false
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun shortcutPin(context: Context, shortcut_id: String, requestCode: Int) {

        val shortcutManager = applicationContext.getSystemService(ShortcutManager::class.java)

        if (shortcutManager!!.isRequestPinShortcutSupported) {
            val pinShortcutInfo =
                ShortcutInfo.Builder(context, shortcut_id).build()

            val pinnedShortcutCallbackIntent =
                shortcutManager.createShortcutResultIntent(pinShortcutInfo)

            val successCallback = PendingIntent.getBroadcast(
                context, /* request code */ requestCode,
                pinnedShortcutCallbackIntent, /* flags */ PendingIntent.FLAG_MUTABLE
            )

            shortcutManager.requestPinShortcut(
                pinShortcutInfo,
                successCallback.intentSender
            )
        }
    }


    private fun showToastMessage(messages: String) {

        try {
            Toast.makeText(applicationContext, messages, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
        }
    }

    private fun hideKeyBoard(editText: EditText) {
        try {
            editText.clearFocus()
            val imm =
                applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
        } catch (ignored: java.lang.Exception) {
        }
    }
    @SuppressLint("SourceLockedOrientationActivity")
    private fun applyOritenation() {
        val getState = sharedBiometric.getString(Constants.IMG_TOGGLE_FOR_ORIENTATION, "").toString()

        if (getState == Constants.USE_POTRAIT){
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        }else if (getState == Constants.USE_LANDSCAPE){

            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        }else if (getState == Constants.USE_UNSEPECIFIED){
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        }

    }

}