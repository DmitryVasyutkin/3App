package io.mobidoo.a3app.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.ActivityDialpadBinding

class DialpadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDialpadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDialpadBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}