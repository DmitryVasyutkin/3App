package io.mobidoo.a3app.ui.wallpaperpreview

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import android.icu.util.Calendar
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.mobidoo.a3app.BuildConfig
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.FragmentWallpaperPreviewBinding
import io.mobidoo.a3app.ui.WallpaperActivity
import io.mobidoo.a3app.utils.AppUtils.createFullLink
import io.mobidoo.a3app.utils.AppUtils.toDayOfWeek
import io.mobidoo.a3app.utils.AppUtils.toMonth
import io.mobidoo.a3app.utils.FileDownloaderListener
import io.mobidoo.a3app.utils.MediaLoadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class WallpaperPreviewFragment : Fragment() {
    companion object{
        private const val CHANNEL_ID = "6668"
        private const val channelName = "download_notification_channel"
        private const val DOWNLOAD_NOTIFICATION_ID = 8889
        const val TYPE_STATIC = 0
        const val TYPE_LIVE = 1
    }

    private var _binding: FragmentWallpaperPreviewBinding? = null
    private val binding get() = _binding!!

    private var link =""
    private var categoryName = ""
    private var wallpaperType = -1

    private var previewShowing = false
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<RelativeLayout>
    private lateinit var downloadManager: MediaLoadManager
    private lateinit var exoPlayer: ExoPlayer

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
            activity?.onBackPressed()
        }
        view.findViewById<ImageButton>(R.id.ib_close_ad_wall_preview)?.setOnClickListener {
            binding.rlWallAdvertising.visibility = View.GONE
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if(previewShowing){
                        previewAction(resources, false)
                    }else{
                        activity?.finish()
                    }
                }
            }
        )
        downloadManager = MediaLoadManager(requireContext().contentResolver, resources,
            object : FileDownloaderListener {
                override suspend fun success(path: String) {
                    withContext(Dispatchers.Main){
                        handleSuccessSaving()
                    }
                }

                override fun error(message: String) {
                    Log.i("WallpaperActionFragment", "file not loaded with: $message")
                }
            })
        setInsets(view)
        loadAd()
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

    private fun handleSuccessSaving() {
        findNavController().navigate(R.id.action_wallpaperActionFragment_to_wallpaperPreviewSuccessFragment)
        NotificationManagerCompat.from(requireContext()).cancel(DOWNLOAD_NOTIFICATION_ID)
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

    private fun showAdvertising() {
        binding.rlWallAdvertising.visibility = View.VISIBLE
    }

    private fun downloadWallpaper(link: String, subFolder: String){
        showDownloadNotification()
        lifecycleScope.launch {
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
            binding.rlWallAdvertising.visibility = View.GONE
            hideActions(true)
            previewShowing = true
        }else{
            binding.linearLayout2.visibility = View.GONE
            binding.rlWallAdvertising.visibility = View.VISIBLE
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

    private fun loadAd(){
        val builder = AdLoader.Builder(requireContext(), BuildConfig.AD_MOB_KEY)
            .forNativeAd { nativeAd ->
                val adView = layoutInflater.inflate(R.layout.layout_ad_wall_preview, null) as NativeAdView

                adView.findViewById<TextView>(R.id.ad_headline_wallP).text = nativeAd.headline
                adView.findViewById<ImageView>(R.id.ad_app_icon_wallP).load(nativeAd.icon?.drawable)
                if (!previewShowing)
                    showAdvertising()
                Log.i("WallpaperActionFragment", "nativeAd $nativeAd")
                if(isDetached){
                    nativeAd.destroy()
                    return@forNativeAd
                }
            }
            .withAdListener(object : AdListener(){
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    if (!previewShowing)
                        showAdvertising()
                    Log.i("WallpaperActionFragment", "nativeAd failed ${p0.message}")
                }
            })
            .build()
        val request = AdRequest.Builder()
            .build()
        Log.i("WallpaperActionFragment", "is Test device ${request.isTestDevice(requireContext())}")
        builder.loadAds(request, 10)
    }

    private fun showDownloadNotification(){
        NotificationManagerCompat.from(requireContext()).cancel(DOWNLOAD_NOTIFICATION_ID)
        createNotificationChannel(CHANNEL_ID, channelName, "notification for download wallpapers")
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

        super.onDestroy()
    }
}