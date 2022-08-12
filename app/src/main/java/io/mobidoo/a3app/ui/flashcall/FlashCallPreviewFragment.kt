package io.mobidoo.a3app.ui.flashcall

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Context.ROLE_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telecom.InCallService
import android.telecom.TelecomManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.RemoteViews
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.FragmentFlashCallPreviewBinding
import io.mobidoo.a3app.di.Injector
import io.mobidoo.a3app.ui.FlashCallPreviewActivity
import io.mobidoo.a3app.ui.MainActivity
import io.mobidoo.a3app.ui.bottomsheet.AllowChangeSettingsPermissions
import io.mobidoo.a3app.ui.ringtone.RingtoneCategoryItemsFragment
import io.mobidoo.a3app.ui.wallpaperpreview.WallpaperPreviewSuccessFragment
import io.mobidoo.a3app.utils.*
import io.mobidoo.a3app.utils.AppUtils.createFullLink
import io.mobidoo.a3app.utils.Constants.APP_PREFS
import io.mobidoo.a3app.utils.Constants.SP_FLASH_CAL_URI
import io.mobidoo.a3app.utils.Constants.interAdKeyList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject


class FlashCallPreviewFragment : Fragment() {
    companion object{
        const val TAG= "FlashCallPreviewFragment"
        private const val CHANNEL_ID = "6667"
        private const val channelName = "download_notification_channel"
        private const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_notification_channel_id"
        private const val DOWNLOAD_NOTIFICATION_ID = 8888
        const val TYPE_STATIC = 0
        const val TYPE_LIVE = 1
        private val PERMISSION_SYSTEM_SETTINGS = Manifest.permission.WRITE_SETTINGS
        private val PERMISSION_APPEAR_ON_TOP_SETTINGS = Manifest.permission.SYSTEM_ALERT_WINDOW
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ANSWER_PHONE_CALLS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE)
    }

    private val REQUEST_CODE_SET_DEFAULT_DIALER: Int = 3335
    private val REQUEST_CODE_WRITE_SETTINGS: Int = 3337
    private var _binding: FragmentFlashCallPreviewBinding? = null
    private val binding get() = _binding!!

    private var link =""
    private var categoryName = ""

    private var flashCallUri = ""
    private var flashCallLoaded = false
    private var systemSettingsOpened = false

    private var downloadJob: Job? = null
    private lateinit var downloadManager: MediaLoadManager
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var requestPermissionLauncher2: ActivityResultLauncher<String>
    private lateinit var requestPermissionLauncher3: ActivityResultLauncher<String>
    private var mInterstitialAd: InterstitialAd? = null
    private var interstitialLoaded = false
    private var onBackPressed = false
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var loadInterAdAttempt = 0

    override fun onAttach(context: Context) {
        (activity?.application as Injector).createWallpaperSubComponent().inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFlashCallPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        link = requireActivity().intent?.extras?.getString(FlashCallPreviewActivity.EXTRA_WALLPAPER_LINK).toString()
        categoryName = requireActivity().intent?.extras?.getString(FlashCallPreviewActivity.EXTRA_CATEGORY_NAME).toString()

        loadInterAdAttempt = 0
        loadInterAd(interAdKeyList[loadInterAdAttempt])
        downloadManager = MediaLoadManager(requireContext().contentResolver, resources,
            object : FileDownloaderListener {
                override suspend fun success(path: String) {
                    withContext(Dispatchers.Main){
                        flashCallUri =path
                        flashCallLoaded = true
                        handleSuccessSaving()
                    }
                }

                override suspend fun error(message: String) {
                    Log.i(TAG, "file not loaded with: $message")
                }
            })
        setTopInset(view)
        binding.ibBackFlashCallPreview.setOnClickListener {
            activity?.onBackPressed()
//            binding.rlInterAdPlaceholderFlashCall.visibility = View.VISIBLE
//            loadInterAd()
//            onBackPressed = true
        }
        binding.ibDownloadFlashPreview.setOnClickListener {
            if (permissionAllowed(REQUIRED_PERMISSIONS[0])
                && permissionAllowed(REQUIRED_PERMISSIONS[1])
                && permissionAllowed(REQUIRED_PERMISSIONS[2])
                && permissionAllowed(REQUIRED_PERMISSIONS[3])
            ){
                Log.i(TAG, "button downloadWallpaper")
                downloadWallpaper(createFullLink(link), categoryName)
            }else{
                val notAllowed = REQUIRED_PERMISSIONS.filter { !permissionAllowed(it) }
                requestPermissionLauncher.launch(notAllowed.toTypedArray())
            }
        }
        binding.ibShareFlashPreview.setOnClickListener {
            val link = Constants.shareLink
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, link)
            }
            startActivity(Intent.createChooser(intent, resources.getString(R.string.shareLink)))
        }
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ isGranted ->
            Log.i(TAG, "isGraned Map $isGranted")
            if (!isGranted.containsValue(false)){
                Log.i(TAG, "launch downloadWallpaper")
                downloadWallpaper(createFullLink(link), categoryName)

            }else{

            }
        }
        requestPermissionLauncher2 = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted ->
            if (isGranted){

                setDefualtRingtoneAndShowAd()

            }else{
                when{
                    shouldShowRequestPermissionRationale(PERMISSION_SYSTEM_SETTINGS) -> {

                        val fr = AllowChangeSettingsPermissions(){
                            startActivityForResult(
                                Intent(
                                    Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                    Uri.parse("package:" + requireContext().packageName)
                                ), REQUEST_CODE_WRITE_SETTINGS
                            )
                        }
                        fr.show(activity?.supportFragmentManager?.beginTransaction()!!, "allow_permissions")
                    }
                    else -> {
                        try{
                            setDefualtRingtoneAndShowAd()
                        }catch (e: Exception){

                        }

                    }
                }
            }
        }
        requestPermissionLauncher3 = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted ->
            if(isGranted){
                Log.i("FlashCallPreview", "isGranted $isGranted")
                setDefualtRingtoneAndShowAd()
            }else{
                if(shouldShowRequestPermissionRationale(PERMISSION_APPEAR_ON_TOP_SETTINGS)){
                    startActivityForResult(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + requireContext().packageName)
                        ), 4556
                    )

                }
            }
        }
    }

    private fun setDefualtRingtoneAndShowAd() {
        try {
            RingtoneManager.setActualDefaultRingtoneUri(requireContext(), RingtoneManager.TYPE_RINGTONE, Uri.parse("android.resource://io.mobidoo.a3app/raw/five_seconds_of_silence"))
            Log.e("FlashCallFragment", "ringtone silent")
        }catch (e: Exception){
            Log.e("FlashCallFragment", "exc $e")
        }
        openAdFragment()
    }

    override fun shouldShowRequestPermissionRationale(permission: String): Boolean {
        return if(permission == PERMISSION_SYSTEM_SETTINGS) !Settings.System.canWrite(requireContext())
        else !Settings.canDrawOverlays(requireContext())
    }
    private fun systemPermissionsGranted() = Settings.System.canWrite(requireContext())

    private fun setAsDefaultCaller(){
//        val sharedPreferences = activity?.getSharedPreferences(APP_PREFS, Activity.MODE_PRIVATE)!!
        with(sharedPreferences.edit()){
            putString(SP_FLASH_CAL_URI, flashCallUri)
            commit()
        }

        if (Settings.canDrawOverlays(requireContext())){
            if (systemPermissionsGranted()){
                setDefualtRingtoneAndShowAd()
            }else{
                requestPermissionLauncher2.launch(PERMISSION_SYSTEM_SETTINGS)
            }
            return
        }else{
            requestPermissionLauncher3.launch(PERMISSION_APPEAR_ON_TOP_SETTINGS)
        }

        if (isQPlus()) {
            val roleManager = activity?.getSystemService(RoleManager::class.java)
            if (roleManager!!.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) && !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                Log.i(TAG, "role intent $intent")
                startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER)
            }else{
                if (systemPermissionsGranted()){
                    setDefualtRingtoneAndShowAd()
                }else{
                    requestPermissionLauncher2.launch(PERMISSION_SYSTEM_SETTINGS)
                }
            }
        }
        else {
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, activity?.packageName).apply {
                try {
                    startActivityForResult(this, REQUEST_CODE_SET_DEFAULT_DIALER)
                } catch (e: ActivityNotFoundException) {
                    // toast(R.string.no_app_found)
                } catch (e: Exception) {
                    // showErrorToast(e)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_SET_DEFAULT_DIALER){
            if (resultCode == Activity.RESULT_OK){
              //  Toast.makeText(requireContext(), "Flash call installed", Toast.LENGTH_SHORT).show()
            }
            if (systemPermissionsGranted()){
                setDefualtRingtoneAndShowAd()
            }else{
                requestPermissionLauncher2.launch(PERMISSION_SYSTEM_SETTINGS)
            }
        }
        if (requestCode == REQUEST_CODE_WRITE_SETTINGS){
            setDefualtRingtoneAndShowAd()
        }
        if(requestCode == 4556){
            if (systemPermissionsGranted()){
                setDefualtRingtoneAndShowAd()
            }else{
                requestPermissionLauncher2.launch(PERMISSION_SYSTEM_SETTINGS)
            }
        }

    }
    private fun loadInterAd(key: String){
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(requireContext(), key, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.i("SplashScreen", "filed to load interstitial attempt $loadInterAdAttempt")
                loadInterAdAttempt++
                if (loadInterAdAttempt > interAdKeyList.size - 1){
                    mInterstitialAd = null
                    interstitialLoaded = true
                    if(onBackPressed){
                        activity?.finish()
                    }else{
                        binding.rlInterAdPlaceholderFlashCall.visibility = View.GONE
                        initializePlayer()
                    }
                }else{
                    loadInterAd(interAdKeyList[loadInterAdAttempt])
                }
            }

            override fun onAdLoaded(p0: InterstitialAd) {
                Log.i("SplashScreen", "interstitial loaded $p0")
                super.onAdLoaded(p0)
                mInterstitialAd = p0
                mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                    override fun onAdClicked() {
                        // Called when a click is recorded for an ad.
                        Log.d("SplashScreen", "Ad was clicked.")
                    }

                    override fun onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        Log.d("SplashScreen", "Ad dismissed fullscreen content.")
                        mInterstitialAd = null
                        if(onBackPressed){
                            activity?.finish()
                        }else{
                            binding.rlInterAdPlaceholderFlashCall.visibility = View.GONE
                            initializePlayer()
                        }
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        Log.d("SplashScreen", "Ad failed to show fullscreen content.")
                        mInterstitialAd = null
                    }

                    override fun onAdImpression() {
                        // Called when an impression is recorded for an ad.
                        Log.d("SplashScreen", "Ad recorded an impression.")
                    }

                    override fun onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d("SplashScreen", "Ad showed fullscreen content.")
                    }
                }
                interstitialLoaded = true
                mInterstitialAd?.show(requireActivity())
            }
        })
    }
    private fun setTopInset(view: View) {
        view.setOnApplyWindowInsetsListener { view, windowInsets ->
            val topInset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()).top
            } else {
                windowInsets.stableInsetBottom
            }
            val params = binding.subConstraintFlashPreview.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(0, topInset, 0, 0)
            binding.subConstraintFlashPreview.layoutParams = params

            return@setOnApplyWindowInsetsListener windowInsets
        }
    }

    private fun handleSuccessSaving() {

        try {

       //     NotificationManagerCompat.from(requireContext()).cancel(DOWNLOAD_NOTIFICATION_ID)
            binding.rlLoadingFlashcall.visibility = View.GONE
            exoPlayer.pause()
            setAsDefaultCaller()
        }catch (e: Exception){

        }finally {

        }
    }

    private fun openAdFragment(){
        findNavController().navigate(
            R.id.action_flashCallPreviewFragment_to_wallpaperPreviewSuccessFragment2,
            Bundle().apply {
                putString(
                    WallpaperPreviewSuccessFragment.ARG_SAVED_VALUE,
                    resources.getString(R.string.flashCall)
                )
            })
    }

    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(requireContext())
            .setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
            .build()
        binding.playerViewFlashCall.apply {
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            controllerAutoShow = false
            useController = false
            player = exoPlayer
            hideController()
        }
        exoPlayer.setMediaItem(MediaItem.fromUri(AppUtils.createFullLink(link)))
        exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ALL
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
    }

    private fun downloadWallpaper(link: String, subFolder: String){
      //  showDownloadNotification()
        binding.rlLoadingFlashcall.visibility = View.VISIBLE
        downloadJob = lifecycleScope.launch {
            downloadManager.downloadLiveWallpaper(link, subFolder, true)
        }
    }

    private fun showDownloadNotification(){
        NotificationManagerCompat.from(requireContext()).cancel(DOWNLOAD_NOTIFICATION_ID)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(CHANNEL_ID, channelName, "notification for download flash call")
        }
        val notificationLayout = RemoteViews(activity?.packageName, R.layout.notification_download_wallpaper)
        val notificationBuilder =
            NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_download_24)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setAutoCancel(true)
        val notification = notificationBuilder.build()
        val notificationCompat = NotificationManagerCompat.from(requireContext())
        notificationCompat.notify(DOWNLOAD_NOTIFICATION_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(id: String, name: String, channelDescription: String){
        val notificationManager = activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val chanel = NotificationChannel(id, name, importance).apply {
            description = channelDescription
        }
        notificationManager.createNotificationChannel(chanel)
    }

    private fun permissionAllowed(perm: String) = ContextCompat.checkSelfPermission(requireContext(), perm) == PackageManager.PERMISSION_GRANTED


    fun launchSetDefaultDialerIntent() {
        if (isQPlus()) {
            val roleManager = activity?.getSystemService(RoleManager::class.java)
            if (roleManager!!.isRoleAvailable(RoleManager.ROLE_DIALER) && !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                Log.i(TAG, "role intent $intent")
                startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER)
            }
        } else {
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, activity?.packageName).apply {
                try {
                    startActivityForResult(this, REQUEST_CODE_SET_DEFAULT_DIALER)
                } catch (e: ActivityNotFoundException) {
                   // toast(R.string.no_app_found)
                } catch (e: Exception) {
                   // showErrorToast(e)
                }
            }
        }
    }


    override fun onDestroyView() {
        exoPlayer.stop()
        exoPlayer.release()
        downloadJob?.cancel()
        super.onDestroyView()
    }
}