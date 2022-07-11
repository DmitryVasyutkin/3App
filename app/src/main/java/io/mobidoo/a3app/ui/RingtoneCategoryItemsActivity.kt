package io.mobidoo.a3app.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.ActivityRingtoneCategoryItemsBinding
import io.mobidoo.a3app.databinding.ActivityWallpaperBinding

class RingtoneCategoryItemsActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_RINGTONE_LINK = "ringtone_link"
        const val EXTRA_RINGTONE_CATEGORY_NAME = "ringtone_category_name"

        fun getIntent(context: Context, link: String, categoryName: String) = Intent(context, RingtoneCategoryItemsActivity::class.java).apply {
            putExtra(EXTRA_RINGTONE_LINK, link)
            putExtra(EXTRA_RINGTONE_CATEGORY_NAME, categoryName)

        }
    }
    private lateinit var binding: ActivityRingtoneCategoryItemsBinding

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRingtoneCategoryItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val  hostFragment = supportFragmentManager.findFragmentById(R.id.ringtone_nav_host_fragment_container) as NavHostFragment
        navController = hostFragment.findNavController()
    }
}