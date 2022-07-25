package io.mobidoo.a3app.utils

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.mobidoo.a3app.BuildConfig
import io.mobidoo.a3app.R
import io.mobidoo.a3app.entity.startcollectionitem.SelectedCategoryWallpapersFragment
import io.mobidoo.a3app.services.LiveWallpaperService
import io.mobidoo.a3app.ui.wallpaperpreview.WallpaperPreviewFragment

object AppUtils {
    val flashCallSubsUrl = "/api/r3/wallpapers/live/6"
    val liveWallpaperUrl = "/api/r3/live/"
    private val videoFormats = setOf<String>("mp4", "m4a", "webM", "fmp4", "mpeg")
    fun isNetworkAvailable(context: Context?):Boolean{
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false

    }

    fun createFullLink(link: String) = StringBuilder().append(BuildConfig.BASE_URL).append(link).toString()

    fun BottomSheetBehavior<RelativeLayout>.expand() {
        if(this.state == BottomSheetBehavior.STATE_COLLAPSED)
            this.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun Activity.makeStatusBarTransparent() {
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS )
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
            statusBarColor = Color.TRANSPARENT
        }
    }

    fun Int.toDayOfWeek(resources: Resources): String{
        return when(this){
            0 -> resources.getString(R.string.sunday)
            1 -> resources.getString(R.string.monday)
            2 -> resources.getString(R.string.tuesday)
            3 -> resources.getString(R.string.wednesday)
            4 -> resources.getString(R.string.thusday)
            5-> resources.getString(R.string.friday)
            else -> resources.getString(R.string.saturday)

        }
    }
    fun Int.toMonth(resources: Resources) : String{
        return when(this){
            0 -> resources.getString(R.string.january)
            1 -> resources.getString(R.string.february)
            2 -> resources.getString(R.string.march)
            3 -> resources.getString(R.string.april)
            4 -> resources.getString(R.string.may)
            5-> resources.getString(R.string.june)
            6 -> resources.getString(R.string.july)
            7 -> resources.getString(R.string.august)
            8 -> resources.getString(R.string.september)
            9 -> resources.getString(R.string.october)
            10 -> resources.getString(R.string.november)
            else-> resources.getString(R.string.december)

        }
    }
    fun getWallpaperTypeFromLink(link: String) : Int{
        val format = link.split("/").last().split(".").last()
        return if (videoFormats.contains(format)) WallpaperPreviewFragment.TYPE_LIVE
        else WallpaperPreviewFragment.TYPE_STATIC
    }

    fun Fragment.startWallpaperService(uri: String) {
        val shp = requireContext().getSharedPreferences("APP_PREFS", Activity.MODE_PRIVATE)
        with(shp.edit()) {
            putString(LiveWallpaperService.PREFERENCES_WAL_URI, uri)
            commit()
        }
        try {
            val cn = ComponentName(requireActivity(), LiveWallpaperService::class.java)
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, cn)
            }

            startActivity(intent)
        } catch (e: Exception) {
            Log.i("InstallWallpaper", "inst exc $e")
        }
    }
}