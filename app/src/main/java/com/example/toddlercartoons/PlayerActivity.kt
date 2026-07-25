package com.example.toddlercartoons

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.toddlercartoons.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_VIDEO_URI = "extra_video_uri"
        const val EXTRA_TITLE = "extra_title"
        private const val PARENT_HOLD_MS = 3000L
    }

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var sessionManager: SessionManager
    private var countDownTimer: CountDownTimer? = null
    private var totalSessionSec: Int = 0
    private var sessionEndedNormally = false
    private var endedEarlyByParent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val videoUri = intent.getStringExtra(EXTRA_VIDEO_URI)
        if (videoUri == null) {
            finish()
            return
        }

        // The session can never run longer than what's left in today's budget,
        // even if the configured session length is longer.
        totalSessionSec = sessionManager.nextAllowedSessionLengthSec()
        if (totalSessionSec <= 0) {
            goToAllDone()
            return
        }

        binding.videoView.setVideoURI(Uri.parse(videoUri))
        binding.videoView.setOnPreparedListener { it.isLooping = true }
        binding.videoView.setOnErrorListener { _, _, _ -> true } // fail quietly, no scary error UI
        binding.videoView.start()

        setupTouchBlocking()
        setupParentExitHotspot()
        startCountdown(totalSessionSec)
    }

    /** Swallows every touch on the main overlay so nothing she does can pause,
     * seek, drag, or otherwise interfere with playback. */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchBlocking() {
        binding.touchBlockerOverlay.setOnTouchListener { _, _ -> true }
    }

    /** Long-press the bottom-left corner for ~3 seconds, then enter the PIN,
     * to end a session early. Ordinary tapping/dragging anywhere, including on
     * this corner, does nothing — only a sustained hold starts the PIN prompt. */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupParentExitHotspot() {
        val handler = android.os.Handler(mainLooper)
        var holdRunnable: Runnable? = null

        binding.parentExitHotspot.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    holdRunnable = Runnable { promptParentExit() }
                    handler.postDelayed(holdRunnable!!, PARENT_HOLD_MS)
                    true
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    holdRunnable?.let { handler.removeCallbacks(it) }
                    true
                }
                else -> true
            }
        }
    }

    private fun promptParentExit() {
        val input = EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or
            android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD

        AlertDialog.Builder(this)
            .setTitle(R.string.enter_parent_pin)
            .setView(input)
            .setPositiveButton(R.string.unlock) { _, _ ->
                if (input.text.toString() == sessionManager.parentPin) {
                    endedEarlyByParent = true
                    endSession()
                } else {
                    Toast.makeText(this, R.string.wrong_pin, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun startCountdown(totalSec: Int) {
        binding.countdownRing.setMax(totalSec)

        countDownTimer = object : CountDownTimer(totalSec * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val secLeft = (millisUntilFinished / 1000).toInt()
                binding.countdownRing.setProgress(secLeft)
            }

            override fun onFinish() {
                sessionEndedNormally = true
                endSession()
            }
        }.start()
    }

    private fun endSession() {
        countDownTimer?.cancel()
        val secondsWatched = if (sessionEndedNormally) totalSessionSec else approxSecondsWatched()
        sessionManager.addUsedSeconds(secondsWatched)
        // A parent-triggered early exit does NOT start the cooldown — she didn't
        // get her full session, so it wouldn't be fair to also make her wait longer.
        if (!endedEarlyByParent) {
            sessionManager.markSessionEndedNow()
        }
        goToAllDone()
    }

    private fun approxSecondsWatched(): Int {
        return totalSessionSec - (binding.countdownRing.currentProgress)
    }

    private fun goToAllDone() {
        stopScreenPinningIfActive()
        startActivity(Intent(this, AllDoneActivity::class.java))
        finish()
    }

    // ---------- Screen pinning: keeps her inside this app while it plays ----------
    // This uses Android's standard Lock Task / Screen Pinning API (no root, no special
    // permission). The first time it's used on a device, Android shows a one-time system
    // dialog explaining pinning; after that it just pins silently. To unpin, back+recents
    // held together is needed — a toddler won't manage that combination.

    override fun onResume() {
        super.onResume()
        try {
            startLockTask()
        } catch (_: Exception) {
            // Pinning not available on this device/OS version — app still works,
            // just without the extra lock-out protection.
        }
    }

    private fun stopScreenPinningIfActive() {
        try {
            stopLockTask()
        } catch (_: Exception) {
            // Wasn't pinned; nothing to do.
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        // If the activity is destroyed before the timer finished (e.g. she managed
        // to exit somehow), still record partial usage and start the cooldown so the
        // app can't just be reopened immediately for a fresh full session.
        if (!sessionEndedNormally && !endedEarlyByParent) {
            sessionManager.addUsedSeconds(approxSecondsWatched())
            sessionManager.markSessionEndedNow()
        }
    }
}
