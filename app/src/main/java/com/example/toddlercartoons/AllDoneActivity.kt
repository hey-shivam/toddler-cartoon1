package com.example.toddlercartoons

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.toddlercartoons.databinding.ActivityAllDoneBinding

/**
 * The session-end screen. Deliberately plain and identical every single time:
 * same image, same short phrase, same calm tone, no buttons to tap, no countdown
 * to "next time", nothing that rewards lingering here. The goal is for this
 * screen to be the least interesting part of the app.
 *
 * There is intentionally no way to navigate from here back into a video —
 * she has to hand the device to a parent, who can reopen MainActivity, which
 * will show the locked/cooldown state on its own.
 */
class AllDoneActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllDoneBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllDoneBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    // Disable back button here on purpose — nothing "clever" to escape to.
    // A parent takes the device and returns to MainActivity manually.
    override fun onBackPressed() {
        // no-op
    }
}
