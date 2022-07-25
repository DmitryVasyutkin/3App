package io.mobidoo.a3app.ui.wallpaperpreview

import android.app.Activity
import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.*
import androidx.fragment.app.Fragment
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
import io.mobidoo.a3app.databinding.FragmentInstallWallpaperBinding
import io.mobidoo.a3app.services.LiveWallpaperService
import io.mobidoo.a3app.ui.bottomsheet.SelectWallpaperDestinationDialog
import io.mobidoo.a3app.utils.MediaLoadManager
import java.io.InputStream


class InstallWallpaperFragment : Fragment() {
    companion object{
        const val RC_LIVE_WALL = 2223
        const val ARG_WALLPAPER_URI = "arg_wallpaper_uri"
        const val ARG_WALL_TYPE = "arg_wall_type"
        const val TYPE_STATIC = 0
        const val TYPE_LIVE = 1
    }

    private var _binding: FragmentInstallWallpaperBinding? = null
    private val binding get() = _binding!!

    private var wallpaperUri =""
    private var categoryName = ""
    private var wallpaperType = -1

    private var previewShowing = false
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<RelativeLayout>
    private lateinit var downloadManager: MediaLoadManager
    private lateinit var exoPlayer: ExoPlayer
    private var dialogSelectDest: SelectWallpaperDestinationDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInstallWallpaperBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wallpaperUri = arguments?.getString(ARG_WALLPAPER_URI).toString()
        wallpaperType = arguments?.getInt(ARG_WALL_TYPE)!!
        if (wallpaperType == WallpaperPreviewFragment.TYPE_STATIC){
            binding.ivWallpaperInstall.visibility = View.VISIBLE
            binding.videoViewWallpaperInstall.visibility = View.GONE
            binding.ivWallpaperInstall.load(wallpaperUri)
        }else{
            binding.ivWallpaperInstall.visibility = View.GONE
            binding.videoViewWallpaperInstall.visibility = View.VISIBLE
            initializePlayer()
        }
        dialogSelectDest = SelectWallpaperDestinationDialog {
            installStaticWallpaper(it)

        }
        binding.btnInstallWallpaper.setOnClickListener {
            if (wallpaperType == TYPE_STATIC) {
                dialogSelectDest?.show(activity?.supportFragmentManager?.beginTransaction()!!, "dialog_select_destination")
            }else{

            }
        }
        binding.ibBackWallpaperInstall.setOnClickListener {
            activity?.onBackPressed()
        }

        setInsets(view)
    }

    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(requireContext())
            .setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
            .build()
        binding.videoViewWallpaperInstall.apply {
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            controllerAutoShow = false
            useController = false
            player = exoPlayer
            hideController()
        }
        exoPlayer.setMediaItem(MediaItem.fromUri(wallpaperUri))
        exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ALL
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
    }

    private fun handleSuccessInstall() {
        findNavController().navigate(R.id.action_installWallpaperFragment_to_wallpaperPreviewSuccessFragment, Bundle().apply {
            putString(WallpaperPreviewSuccessFragment.ARG_SAVED_VALUE, resources.getString(R.string.wallpaper))
        })
    }

    private fun installStaticWallpaper(flag: Int){
        val wm = WallpaperManager.getInstance(requireContext())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            wm.addOnColorsChangedListener(object : WallpaperManager.OnColorsChangedListener {
                override fun onColorsChanged(p0: WallpaperColors?, p1: Int) {
                    Log.i("InstallWallpaper", "onColorsChanged $p0, $p1")
                }

            }, Handler(Looper.getMainLooper()))
        }
        var istr: InputStream? = null
        try {
            istr = activity?.contentResolver?.openInputStream(Uri.parse(wallpaperUri))
            if (flag == 0)  wm.setStream(istr)
                else wm.setStream(istr, null, true, flag)
        }catch (e: Exception){
            dialogSelectDest?.dismiss()
        }finally {
            handleSuccessInstall()
            dialogSelectDest?.dismiss()
            istr?.close()
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
            val params1 = binding.btnInstallWallpaper.layoutParams as ViewGroup.MarginLayoutParams
            params1.setMargins(0, 0, 0, botInset)
            binding.btnInstallWallpaper.layoutParams = params1

            val ibParams = binding.ibBackWallpaperInstall.layoutParams as ViewGroup.MarginLayoutParams
            ibParams.setMargins(0, topInsets, 0, 0)
            binding.ibBackWallpaperInstall.layoutParams = ibParams

            return@setOnApplyWindowInsetsListener windowInsets
        }
    }

    private fun showAdvertising() {

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("InstallWall", "requestCode $requestCode")
        Log.i("InstallWall", "resultCode $resultCode")
        if(requestCode == RC_LIVE_WALL){
            if (resultCode == Activity.RESULT_OK){
                handleSuccessInstall()
            }
        }
    }

    override fun onDestroy() {
        if(wallpaperType == TYPE_LIVE){
            if(this::exoPlayer.isInitialized) exoPlayer.release()
        }

        super.onDestroy()
    }
}