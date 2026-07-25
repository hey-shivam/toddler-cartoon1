package com.example.toddlercartoons

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.toddlercartoons.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var videoManager: VideoManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        videoManager = VideoManager(this)

        // Small, out-of-the-way settings icon — a toddler tapping randomly is unlikely
        // to hit it, and it's PIN-gated anyway.
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshScreen()
    }

    private fun refreshScreen() {
        binding.cartoonGrid.removeAllViews()

        if (!sessionManager.canWatchNow()) {
            showLockedState()
        } else {
            showCartoonGrid()
        }
    }

    private fun showLockedState() {
        binding.cartoonGrid.visibility = View.GONE
        binding.lockedMessage.visibility = View.VISIBLE

        val cooldownLeft = sessionManager.cooldownRemainingSec()
        binding.lockedMessage.text = if (cooldownLeft > 0) {
            val mins = (cooldownLeft / 60) + 1
            getString(R.string.locked_cooldown_message, mins)
        } else {
            getString(R.string.locked_daily_cap_message)
        }
    }

    private fun showCartoonGrid() {
        binding.lockedMessage.visibility = View.GONE

        val videos = videoManager.getVideos()

        if (videos.isEmpty()) {
            binding.cartoonGrid.visibility = View.GONE
            binding.lockedMessage.visibility = View.VISIBLE
            binding.lockedMessage.text = getString(R.string.no_videos_yet_child_view)
            return
        }

        binding.cartoonGrid.visibility = View.VISIBLE

        for (video in videos) {
            val button = ImageButton(this)
            if (video.thumbnailPath != null) {
                val bmp = BitmapFactory.decodeFile(video.thumbnailPath)
                if (bmp != null) button.setImageBitmap(bmp) else button.setImageResource(R.drawable.thumb_placeholder)
            } else {
                button.setImageResource(R.drawable.thumb_placeholder)
            }
            button.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            val params = GridLayout.LayoutParams()
            params.width = 320
            params.height = 320
            params.setMargins(24, 24, 24, 24)
            button.layoutParams = params
            button.setOnClickListener {
                val intent = Intent(this, PlayerActivity::class.java)
                intent.putExtra(PlayerActivity.EXTRA_VIDEO_URI, video.uriString)
                intent.putExtra(PlayerActivity.EXTRA_TITLE, video.title)
                startActivity(intent)
            }
            binding.cartoonGrid.addView(button)
        }
    }
}
