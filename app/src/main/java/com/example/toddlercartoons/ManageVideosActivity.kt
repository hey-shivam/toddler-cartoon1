package com.example.toddlercartoons

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.toddlercartoons.databinding.ActivityManageVideosBinding

class ManageVideosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageVideosBinding
    private lateinit var videoManager: VideoManager
    private lateinit var sessionManager: SessionManager
    private var pendingUri: Uri? = null

    private val pickVideoLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingUri = uri
            promptForTitle(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageVideosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        videoManager = VideoManager(this)
        sessionManager = SessionManager(this)

        binding.pinLayout.visibility = android.view.View.VISIBLE
        binding.contentLayout.visibility = android.view.View.GONE

        binding.pinSubmitButton.setOnClickListener {
            if (binding.pinInput.text.toString() == sessionManager.parentPin) {
                binding.pinLayout.visibility = android.view.View.GONE
                binding.contentLayout.visibility = android.view.View.VISIBLE
                refreshList()
            } else {
                Toast.makeText(this, R.string.wrong_pin, Toast.LENGTH_SHORT).show()
            }
        }

        binding.addVideoButton.setOnClickListener {
            pickVideoLauncher.launch(arrayOf("video/*"))
        }
    }

    private fun promptForTitle(uri: Uri) {
        val input = EditText(this)
        input.hint = getString(R.string.video_title_hint)

        AlertDialog.Builder(this)
            .setTitle(R.string.name_this_video)
            .setView(input)
            .setPositiveButton(R.string.save) { _, _ ->
                val title = input.text.toString().ifBlank { getString(R.string.untitled_video) }
                val added = videoManager.addVideo(uri, title)
                if (added != null) {
                    Toast.makeText(this, R.string.video_added, Toast.LENGTH_SHORT).show()
                    refreshList()
                } else {
                    Toast.makeText(this, R.string.video_add_failed, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun refreshList() {
        binding.videoListContainer.removeAllViews()
        val videos = videoManager.getVideos()

        if (videos.isEmpty()) {
            val empty = TextView(this)
            empty.text = getString(R.string.no_videos_yet)
            empty.setPadding(16, 32, 16, 32)
            binding.videoListContainer.addView(empty)
            return
        }

        for (video in videos) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.setPadding(16, 16, 16, 16)
            row.gravity = android.view.Gravity.CENTER_VERTICAL

            val thumb = ImageView(this)
            val thumbParams = LinearLayout.LayoutParams(120, 120)
            thumb.layoutParams = thumbParams
            if (video.thumbnailPath != null) {
                val bmp = BitmapFactory.decodeFile(video.thumbnailPath)
                if (bmp != null) thumb.setImageBitmap(bmp) else thumb.setImageResource(R.drawable.thumb_placeholder)
            } else {
                thumb.setImageResource(R.drawable.thumb_placeholder)
            }
            row.addView(thumb)

            val title = TextView(this)
            title.text = video.title
            title.textSize = 18f
            val titleParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            titleParams.marginStart = 24
            title.layoutParams = titleParams
            row.addView(title)

            val removeButton = Button(this)
            removeButton.text = getString(R.string.remove)
            removeButton.setOnClickListener {
                AlertDialog.Builder(this)
                    .setMessage(getString(R.string.confirm_remove_video, video.title))
                    .setPositiveButton(R.string.remove) { _, _ ->
                        videoManager.removeVideo(video.id)
                        refreshList()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
            row.addView(removeButton)

            binding.videoListContainer.addView(row)
        }
    }
}
