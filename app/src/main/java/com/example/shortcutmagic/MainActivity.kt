package com.example.shortcutmagic

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.shortcutmagic.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener {
            navController.navigate(R.id.SecondFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_all -> {
                ShortcutStorage(this).clearAll()
                Toast.makeText(this, R.string.msg_history_cleared, Toast.LENGTH_SHORT).show()
                refreshList()
                true
            }
            R.id.action_contact -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/albertincx/ai-shortcut-magic-android/issues"))
                startActivity(intent)
                true
            }
            R.id.action_github -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/albertincx/ai-shortcut-magic-android"))
                startActivity(intent)
                true
            }
            R.id.action_about -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.app_name)
                    .setMessage("Shortcut Magic - Easily create shortcuts for your favorite apps, files, and websites.\n\nVersion 1.0")
                    .setPositiveButton("OK", null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun refreshList() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
        if (currentFragment is FirstFragment) {
            currentFragment.loadShortcuts()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}