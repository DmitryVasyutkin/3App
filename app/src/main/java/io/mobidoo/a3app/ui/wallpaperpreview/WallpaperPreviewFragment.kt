package io.mobidoo.a3app.ui.wallpaperpreview

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.icu.util.Calendar
import android.os.*
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.mobidoo.a3app.BuildConfig
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.FragmentWallpaperPreviewBinding
import io.mobidoo.a3app.databinding.LayoutAdCollectionsBinding
import io.mobidoo.a3app.databinding.LayoutAdWallPreviewBinding
import io.mobidoo.a3app.services.LiveWallpaperService
import io.mobidoo.a3app.ui.WallpaperActivity

import io.mobidoo.a3app.utils.AppUtils
import io.mobidoo.a3app.utils.AppUtils.createFullLink
import io.mobidoo.a3app.utils.AppUtils.isServiceRunning
import io.mobidoo.a3app.utils.AppUtils.startWallpaperService
import io.mobidoo.a3app.utils.AppUtils.toDayOfWeek
import io.mobidoo.a3app.utils.AppUtils.toMonth
import io.mobidoo.a3app.utils.Constants
import io.mobidoo.a3app.utils.Constants.interAdKeyList
import io.mobidoo.a3app.utils.Constants.nativeAdKeyList
import io.mobidoo.a3app.utils.FileDownloaderListener
import io.mobidoo.a3app.utils.MediaLoadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class WallpaperPreviewFragment : Fragment() {
    companion object{
        private const val CHANNEL_ID = "6668"
        private const val channelName = "download_notification_channel"
        private const val DOWNLOAD_NOTIFICATION_ID = 8889
        const val TYPE_STATIC = 0
        const val TYPE_LIVE = 1
        private const val RC_SET_LIVE_WALL = 4747
     //
    }

    private var _binding: FragmentWallpaperPreviewBinding? = null
    private val binding get() = _binding!!

    private var link =""
    private var categoryName = ""
    private var wallpaperType = -1

    private var liveWallDownloaded = false
    private var congratsShowed = false

    private val isLiveWallServiceRunning by lazy {
        isServiceRunning(requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager, LiveWallpaperService::class.java)
    }

    private var downloadJob: Job? = null

    private var previewShowing = false
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<RelativeLayout>
    private lateinit var downloadManager: MediaLoadManager
    private lateinit var exoPlayer: ExoPlayer
    private var currentAd: NativeAd? = null
    private var mInterstitialAd: InterstitialAd? = null
    private var interstitialLoaded = false
    private var onBackPressed = false

    private var handler: Handler? = null
    private val  liveWallStateLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    private var loadNativeAdAttempt = 0
    private var loadInterAdAttempt = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWallpaperPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        link = requireActivity().intent?.extras?.getString(WallpaperActivity.EXTRA_WALLPAPER_LINK).toString()
        categoryName = requireActivity().intent?.extras?.getString(WallpaperActivity.EXTRA_CATEGORY_NAME).toString()
        wallpaperType = requireActivity().intent?.extras?.getInt(WallpaperActivity.EXTRA_WALLPAPER_TYPE)!!
        loadInterAdAttempt = 0
        loadNativeAdAttempt = 0
        loadInterAd(interAdKeyList[loadInterAdAttempt])
        loadAd(nativeAdKeyList[loadNativeAdAttempt])
        if (wallpaperType == TYPE_STATIC){
            binding.ivWallpaperAction.visibility = View.VISIBLE
            binding.videoViewWallpaperAction.visibility = View.GONE
            binding.ivWallpaperAction.load(createFullLink(link))
        }else{
            binding.ivWallpaperAction.visibility = View.GONE
            binding.videoViewWallpaperAction.visibility = View.VISIBLE
            initializePlayer()
        }

        binding.fabShowWallpaperPreview.setOnClickListener {
            previewAction(resources, true)
        }
        binding.fabDownloadWall.setOnClickListener {
            downloadWallpaper(createFullLink(link), categoryName)
        }
        binding.ibBackWallpaperPreview.setOnClickListener {
//            binding.rlInterAdPlaceholder.visibility = View.VISIBLE
//            loadInterAd()
//            onBackPressed = true
            activity?.onBackPressed()
        }
        binding.ibShareWallpaper.setOnClickListener {
            val link = Constants.shareLink
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, link)
            }
            startActivity(Intent.createChooser(intent, resources.getString(R.string.shareLink)))
        }
        view.findViewById<ImageButton>(R.id.ib_close_ad_wall_preview)?.setOnClickListener {
               binding.rlWallAdvertisingContainer.visibility = View.GONE
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {
                    if(previewShowing){
                        previewAction(resources, false)
                    }else{
//                        binding.rlInterAdPlaceholder.visibility = View.VISIBLE
//                        loadInterAd()
//                        onBackPressed = true
                        if(isEnabled){
                            isEnabled = false
                            requireActivity().onBackPressed()
                        }

                    }
                }
            }
        )
        downloadManager = MediaLoadManager(requireContext().contentResolver, resources,
            object : FileDownloaderListener {
                override suspend fun success(path: String) {
                    withContext(Dispatchers.Main){
                        handleSuccessSaving(path)
                    }
                }

                override suspend fun error(message: String) {
                    Log.i("WallpaperActionFragment", "file not loaded with: $message")
                }
            })
        handler = Handler(Looper.getMainLooper())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            WallpaperManager.getInstance(requireActivity()).addOnColorsChangedListener(colorChangedListener,
                handler!!)
        }
        setInsets(view)

        liveWallStateLiveData.observe(viewLifecycleOwner){
            if (it){
                if (wallpaperType != TYPE_STATIC && !congratsShowed){
                    Log.e("WallpaperPreview", "onColorsChanged ${wallpaperType != TYPE_STATIC}")
                  //  val navController = Navigation.findNavController(requireView())
                    findNavController().navigate(
                        R.id.action_wallpaperActionFragment_to_wallpaperPreviewSuccessFragment,
                        Bundle().apply {
                            putString(
                                WallpaperPreviewSuccessFragment.ARG_SAVED_VALUE,
                                resources.getString(R.string.wallpaper)
                            )
                        })
                    congratsShowed = true
                }
            }
        }
    }


    private val colorChangedListener = @RequiresApi(Build.VERSION_CODES.O_MR1)
    object : WallpaperManager.OnColorsChangedListener {
        override fun onColorsChanged(p0: WallpaperColors?, p1: Int) {
//            Log.e("WallpaperPreview", "onColorsChanged $congratsShowed")
//            Log.e("WallpaperPreview", "onColorsChanged ${wallpaperType != TYPE_STATIC}")
            liveWallStateLiveData.postValue(true)
        }
    }

    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(requireContext())
            .setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
            .build()
        binding.videoViewWallpaperAction.apply {
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            controllerAutoShow = false
            useController = false
            player = exoPlayer
            hideController()
        }
        exoPlayer.setMediaItem(MediaItem.fromUri(createFullLink(link)))
        exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ALL
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
    }

    private fun handleSuccessSaving(path: String) {
        if (wallpaperType == TYPE_STATIC){
            binding.rlLoadingWallpaper.visibility = View.GONE
            findNavController().navigate(R.id.action_wallpaperActionFragment_to_installWallpaperFragment, Bundle().apply {
                putString(InstallWallpaperFragment.ARG_WALLPAPER_URI, path)
                putInt(InstallWallpaperFragment.ARG_WALL_TYPE, wallpaperType)
            })
            (activity as WallpaperActivity).isLiveWall = false
        }else{
            binding.rlLoadingWallpaper.visibility = View.GONE
            val shp = requireContext().getSharedPreferences("APP_PREFS", Activity.MODE_PRIVATE)
            with(shp.edit()) {
                putString(LiveWallpaperService.PREFERENCES_WAL_URI, path)
                commit()
            }
            liveWallDownloaded = true
            try {
                val cn = ComponentName(requireActivity(), LiveWallpaperService::class.java)
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, cn)
                }

                startActivityForResult(intent, RC_SET_LIVE_WALL)
                (activity as WallpaperActivity).isLiveWall = true
            } catch (e: Exception) {
                Log.e("WallpaperPreview", "inst exc $e")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("WallpaperPreview", "requestCode $requestCode")
        if(requestCode == RC_SET_LIVE_WALL){
            Log.i("WallpaperPreview", "resultcode $resultCode")
//            findNavController().navigate(
//                R.id.action_wallpaperActionFragment_to_wallpaperPreviewSuccessFragment,
//                Bundle().apply {
//                    putString(
//                        WallpaperPreviewSuccessFragment.ARG_SAVED_VALUE,
//                        resources.getString(R.string.wallpaper)
//                    )
//                })
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
                (activity as WallpaperActivity).isLiveWall = true
                findNavController().navigate(
                    R.id.action_wallpaperActionFragment_to_wallpaperPreviewSuccessFragment,
                    Bundle().apply {
                        putString(
                            WallpaperPreviewSuccessFragment.ARG_SAVED_VALUE,
                            resources.getString(R.string.wallpaper)
                        )
                    })
            }
        }
    }

    private fun setInsets(view: View) {
        view.setOnApplyWindowInsetsListener { view, windowInsets ->
            val botInset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()).bottom
            } else {
                windowInsets.stableInsetBottom
            }
            val topInsets = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()).top
            } else {
                windowInsets.stableInsetTop
            }
            val params = binding.subConstraintWallpaperPreview.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(0, topInsets, 0, botInset)
            binding.subConstraintWallpaperPreview.layoutParams = params

//            val ibParams = binding.ibBackWallpaperPreview.layoutParams as ViewGroup.MarginLayoutParams
//            params.setMargins(0, topInsets, 0, 0)
//            binding.ibBackWallpaperPreview.layoutParams = ibParams

            return@setOnApplyWindowInsetsListener windowInsets
        }
    }

    override fun onResume() {
      //  (activity as WallpaperActivity).isLiveWall = false
        super.onResume()
    }

    private fun showAdvertising() {
        binding.rlWallAdvertisingContainer.visibility = View.VISIBLE
    }

    private fun downloadWallpaper(link: String, subFolder: String){
      //  showDownloadNotification()
        binding.rlLoadingWallpaper.visibility = View.VISIBLE
        downloadJob = lifecycleScope.launch {
            if(wallpaperType == TYPE_STATIC)
                downloadManager.downloadStaticWallpaper(link, subFolder)
            else
                downloadManager.downloadLiveWallpaper(link, subFolder, false)
        }
    }

    private fun setPreviewValues(resources: Resources){
        val calendar = Calendar.getInstance()
        val h = calendar.get(Calendar.HOUR_OF_DAY)
        val m = calendar.get(Calendar.MINUTE)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONDAY)
        binding.tvSystemClock.text = "$h:$m"
        binding.tvSystemDay.text = "${dayOfWeek.toDayOfWeek(resources).capitalize()}, $dayMonth ${month.toMonth(resources).capitalize()}"
    }

    private fun previewAction(resources: Resources, show: Boolean){
        if (show){
            setPreviewValues(resources)
            binding.linearLayout2.visibility = View.VISIBLE
            binding.rlWallAdvertisingContainer.visibility = View.GONE
            hideActions(true)
            previewShowing = true
        }else{
            binding.linearLayout2.visibility = View.GONE
            binding.rlWallAdvertisingContainer.visibility = View.VISIBLE
            hideActions(false)
            previewShowing = false
        }
    }

    private fun hideActions(act: Boolean){
        if (act){
            binding.linearLayout1.visibility = View.GONE
            binding.linearlayout3.visibility = View.GONE
        }else{
            binding.linearLayout1.visibility = View.VISIBLE
            binding.linearlayout3.visibility = View.VISIBLE
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
                        binding.rlInterAdPlaceholder.visibility = View.GONE
                    }
                }else{
                    try {
                        loadInterAd(interAdKeyList[loadInterAdAttempt])
                    }catch (e: Exception){

                    }
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
                        if (onBackPressed){
                            activity?.finish()
                        }else{
                            binding.rlInterAdPlaceholder.visibility = View.GONE
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

    private fun loadAd(key: String){
        val builder = AdLoader.Builder(requireContext(), key)
            .forNativeAd { nativeAd ->
                currentAd = nativeAd
                if(isDetached){
                    nativeAd.destroy()
                    return@forNativeAd
                }
                showAdvertising()
                val adBinding = activity?.layoutInflater?.let {
                    LayoutAdWallPreviewBinding.inflate(
                        it
                    )
                }
                if (adBinding != null) {
                    populateNativeAdView(nativeAd, adBinding)
                }
                binding.flWallAdvertising.removeAllViews()
                binding.flWallAdvertising.addView(adBinding?.root)
            }
            .withAdListener(object : AdListener(){

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    Log.i("WallpaperActionFragment", "nativeAd failed ${p0.message}, attempt $loadNativeAdAttempt")
                    loadNativeAdAttempt++
                    if (loadNativeAdAttempt <= nativeAdKeyList.size - 1){
                        try {
                            loadAd(nativeAdKeyList[loadNativeAdAttempt])
                        }catch (e: Exception){

                        }
                    }
                }
            })
            .build()
        val request = AdRequest.Builder()
            .build()
        builder.loadAd(request)
    }
    private fun populateNativeAdView(nativeAd: NativeAd, adViewBinding: LayoutAdWallPreviewBinding){
        val nativeAdView = adViewBinding.root

        nativeAdView.bodyView = adViewBinding.adHeadlineWallP
        nativeAdView.iconView = adViewBinding.adAppIconWallP
        nativeAdView.callToActionView = adViewBinding.btnNativeAdWallPreviewAction

        if (nativeAd.body != null){
            adViewBinding.adHeadlineWallP.text = nativeAd.body
        }else if (nativeAd.headline != null){
            adViewBinding.adHeadlineWallP.text = nativeAd.headline
        }

        nativeAd.icon?.uri?.let {
            adViewBinding.adAppIconWallP.load(it)
        }
        if(nativeAd.callToAction != null)
            adViewBinding.btnNativeAdWallPreviewAction.text = nativeAd.callToAction

    }
    private fun showDownloadNotification(){
        NotificationManagerCompat.from(requireContext()).cancel(DOWNLOAD_NOTIFICATION_ID)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(CHANNEL_ID, channelName, "notification for download wallpapers")
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

    override fun onDestroy() {
        if(wallpaperType == TYPE_LIVE){
            if(this::exoPlayer.isInitialized) exoPlayer.release()
        }
        currentAd?.destroy()
        downloadJob?.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            WallpaperManager.getInstance(requireContext()).removeOnColorsChangedListener(colorChangedListener)
        }
        super.onDestroy()
    }
}

fun NavController.safeNavigate(direction: NavDirections) {
    currentDestination?.getAction(direction.actionId)?.run { navigate(direction) }
}

fun NavController.safeNavigate(
    @IdRes currentDestinationId: Int,
    @IdRes id: Int,
    args: Bundle? = null
) {
    if (currentDestinationId == currentDestination?.id) {
        navigate(id, args)
    }
}