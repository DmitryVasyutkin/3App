package io.mobidoo.a3app.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

class MainActivity : AppCompatActivity() {

 //   private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var navHostFragment: NavHostFragment? = null

    private lateinit var bottomNavView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
     //   binding = ActivityMainBinding.inflate(layoutInflater)
        Log.i("MainActivityTimes", "before setcontent")
        setContentView(R.layout.activity_main)
        Log.i("MainActivityTimes", "after setcontent")
        bottomNavView = findViewById(R.id.bottomNavView)
        bottomNavView.itemIconTintList = null
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        Log.i("MainActivityTimes", "after findFragmentById")
        navController = navHostFragment?.navController!!
        Log.i("MainActivityTimes", "after navHostFragment?.navController!!")
        NavigationUI.setupWithNavController(bottomNavView, navController)
        Log.i("MainActivityTimes", "after setupWithNavController")
    }

    fun navigateToFlashCalls(){
        bottomNavView.selectedItemId = R.id.flashcalls
    }

    fun navigateToLiveWalls(){
        bottomNavView.selectedItemId = R.id.live
    }
}