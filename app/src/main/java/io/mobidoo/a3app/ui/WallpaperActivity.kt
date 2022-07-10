package io.mobidoo.a3app.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.ActivityWallpaperBinding

class WallpaperActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_WALLPAPER_LINK = "wallpaper_link"
        const val EXTRA_CATEGORY_NAME = "category_name"
        const val EXTRA_WALLPAPER_TYPE = "wallpaper_type"
        fun getIntent(context: Context, link: String, categoryName: String, type: Int) = Intent(context, WallpaperActivity::class.java).apply {
            putExtra(EXTRA_WALLPAPER_LINK, link)
            putExtra(EXTRA_CATEGORY_NAME, categoryName)
            putExtra(EXTRA_WALLPAPER_TYPE, type)
        }
    }
    private lateinit var binding: ActivityWallpaperBinding

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWallpaperBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //makeStatusBarTransparent()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.walls_nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController

    }
}