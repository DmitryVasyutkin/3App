package io.mobidoo.a3app.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.ActivityFlashCallPreviewBinding
import io.mobidoo.a3app.databinding.ActivityWallpaperBinding

class FlashCallPreviewActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_WALLPAPER_LINK = "wallpaper_link"
        const val EXTRA_CATEGORY_NAME = "category_name"
        fun getIntent(context: Context, link: String, categoryName: String) = Intent(context, FlashCallPreviewActivity::class.java).apply {
            putExtra(EXTRA_WALLPAPER_LINK, link)
            putExtra(EXTRA_CATEGORY_NAME, categoryName)
        }
    }
    private lateinit var binding: ActivityFlashCallPreviewBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlashCallPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.flashcall_nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController
    }
}