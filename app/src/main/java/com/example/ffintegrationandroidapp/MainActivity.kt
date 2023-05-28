package com.example.ffintegrationandroidapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ffintegrationandroidapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    val CAMERA_PERMISSION_REQUEST_CODE = 1
    var cameraPermissionGranted = false
    var recordAudioPermissionGranted = false


    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            launchFirstFragment()
        }
    }

    fun launchFirstFragment() {

        val permissionRequired = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
            // Camera Permission not granted
            permissionRequired.add(Manifest.permission.CAMERA)
            // You can start using the camera here
        } else {
            cameraPermissionGranted = true
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ) {
            permissionRequired.add(Manifest.permission.RECORD_AUDIO)
        } else {
            recordAudioPermissionGranted = true
        }

        if(permissionRequired.size > 0) {
            // Permission not yet granted
            // Request permissions
            ActivityCompat.requestPermissions(this, permissionRequired.toTypedArray(), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_launch_frankieone_fragment)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                // Camera permission granted

                for((index, permission) in permissions.withIndex()) {
                    if(permission == Manifest.permission.CAMERA) {
                        cameraPermissionGranted = grantResults[index] == PackageManager.PERMISSION_GRANTED
                    } else if(permission == Manifest.permission.RECORD_AUDIO) {
                        recordAudioPermissionGranted = grantResults[index] == PackageManager.PERMISSION_GRANTED
                    }
                }

                if(cameraPermissionGranted && recordAudioPermissionGranted) {
                    // You can start using the camera here
                    findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_launch_frankieone_fragment)
                } else if(cameraPermissionGranted) {
                    Toast.makeText(this, "Audio permission denied", Toast.LENGTH_SHORT).show()
                } else if(recordAudioPermissionGranted) {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Camera and record audio permission denied", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Camera permission denied
                Toast.makeText(this, "Camera and record audio permission denied", Toast.LENGTH_SHORT).show()
                // Handle permission denied case
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

}