package io.mobidoo.a3app.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.ActivityMainBinding
import io.mobidoo.a3app.di.Injector
import io.mobidoo.a3app.viewmodels.MainActivityViewModel
import io.mobidoo.a3app.viewmodels.MainActivityViewModelFactory
import javax.inject.Inject

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavView.itemIconTintList = null
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.bottomNavView, navController)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.startCollectionFragment -> {

            }
            R.id.flashCallFragment -> {

            }
            R.id.liveWallpaperFragment -> {

            }
            R.id.ringtoneFragment -> {

            }
        }
        return false
    }
}