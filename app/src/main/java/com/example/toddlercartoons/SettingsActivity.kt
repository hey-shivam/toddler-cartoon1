package com.example.toddlercartoons

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.toddlercartoons.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sessionManager: SessionManager
    private var unlocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.pinLayout.visibility = android.view.View.VISIBLE
        binding.controlsLayout.visibility = android.view.View.GONE

        binding.pinSubmitButton.setOnClickListener {
            val entered = binding.pinInput.text.toString()
            if (entered == sessionManager.parentPin) {
                unlocked = true
                showControls()
            } else {
                Toast.makeText(this, R.string.wrong_pin, Toast.LENGTH_SHORT).show()
            }
        }

        binding.saveButton.setOnClickListener {
            saveSettings()
        }

        binding.reduceDailyCapButton.setOnClickListener {
            sessionManager.stepDownDailyCap()
            populateCurrentValues()
            Toast.makeText(this, R.string.daily_cap_reduced, Toast.LENGTH_SHORT).show()
        }

        binding.manageVideosButton.setOnClickListener {
            startActivity(android.content.Intent(this, ManageVideosActivity::class.java))
        }
    }

    private fun showControls() {
        binding.pinLayout.visibility = android.view.View.GONE
        binding.controlsLayout.visibility = android.view.View.VISIBLE
        populateCurrentValues()
    }

    private fun populateCurrentValues() {
        binding.sessionLengthInput.setText((sessionManager.sessionLengthSec / 60).toString())
        binding.cooldownInput.setText(sessionManager.cooldownMin.toString())
        binding.dailyCapInput.setText(sessionManager.dailyCapMin.toString())
    }

    private fun saveSettings() {
        val sessionMin = binding.sessionLengthInput.text.toString().toIntOrNull()
        val cooldown = binding.cooldownInput.text.toString().toIntOrNull()
        val dailyCap = binding.dailyCapInput.text.toString().toIntOrNull()

        if (sessionMin == null || cooldown == null || dailyCap == null) {
            Toast.makeText(this, R.string.invalid_values, Toast.LENGTH_SHORT).show()
            return
        }

        sessionManager.sessionLengthSec = sessionMin * 60
        sessionManager.cooldownMin = cooldown
        sessionManager.dailyCapMin = dailyCap

        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show()
        finish()
    }
}
