package io.mobidoo.a3app.ui.flashcall

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.FragmentFlashCallPreviewBinding
import io.mobidoo.a3app.databinding.FragmentWallpaperPreviewBinding
import io.mobidoo.a3app.ui.FlashCallPreviewActivity
import io.mobidoo.a3app.ui.WallpaperActivity
import io.mobidoo.a3app.ui.wallpaperpreview.WallpaperPreviewFragment
import io.mobidoo.a3app.utils.AppUtils
import io.mobidoo.a3app.utils.AppUtils.createFullLink
import io.mobidoo.a3app.utils.FileDownloaderListener
import io.mobidoo.a3app.utils.MediaLoadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FlashCallPreviewFragment : Fragment() {
    companion object{
        private const val CHANNEL_ID = "6667"
        private const val channelName = "download_notification_channel"
        private const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_notification_channel_id"
        private const val DOWNLOAD_NOTIFICATION_ID = 8888
        const val TYPE_STATIC = 0
        const val TYPE_LIVE = 1
    }
    private var _binding: FragmentFlashCallPreviewBinding? = null
    private val binding get() = _binding!!

    private var link =""
    private var categoryName = ""

    private lateinit var downloadManager: MediaLoadManager
    private lateinit var exoPlayer: ExoPlayer

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
        initializePlayer()
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

        binding.ibBackFlashCallPreview.setOnClickListener {
            activity?.finish()
        }
        binding.ibDownloadFlashPreview.setOnClickListener {
            downloadWallpaper(createFullLink(link), categoryName)
        }
    }

    private fun setTopInset(view: View) {
        view.setOnApplyWindowInsetsListener { view, windowInsets ->
            val topInset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()).top
            } else {
                windowInsets.stableInsetBottom
            }
            val params = binding.root.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(0, topInset, 0, 0)
            binding.root.layoutParams = params

            return@setOnApplyWindowInsetsListener windowInsets
        }
    }

    private fun handleSuccessSaving() {
        findNavController().navigate(R.id.action_flashCallPreviewFragment_to_wallpaperPreviewSuccessFragment2)
        NotificationManagerCompat.from(requireContext()).cancel(DOWNLOAD_NOTIFICATION_ID)
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
        showDownloadNotification()
        lifecycleScope.launch {
            downloadManager.downloadLiveWallpaper(link, subFolder)
        }
    }

    private fun showDownloadNotification(){
        NotificationManagerCompat.from(requireContext()).cancel(DOWNLOAD_NOTIFICATION_ID)
        createNotificationChannel(CHANNEL_ID, channelName, "notification for download flash call")
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
}