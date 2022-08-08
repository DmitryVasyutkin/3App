package io.mobidoo.a3app.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.ActivityLiveWallpaperBinding

class LiveWallpaperActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLiveWallpaperBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveWallpaperBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}