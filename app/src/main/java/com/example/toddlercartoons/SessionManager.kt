package com.example.toddlercartoons

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

/**
 * Handles all the "rules" logic:
 * - how long a single watching session is allowed to run
 * - how long the child must wait after a session before watching again (cooldown)
 * - how much total time is allowed per day (resets at midnight)
 * - the parent PIN used to change settings
 *
 * Nothing here talks to the UI directly; PlayerActivity / MainActivity just ask
 * this class "am I allowed to play right now?" and "how much time is left?".
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("toddler_cartoons_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SESSION_LENGTH_SEC = "session_length_sec"
        private const val KEY_COOLDOWN_MIN = "cooldown_min"
        private const val KEY_DAILY_CAP_MIN = "daily_cap_min"
        private const val KEY_PIN = "parent_pin"

        private const val KEY_LAST_SESSION_END = "last_session_end_millis"
        private const val KEY_TODAY_USED_SEC = "today_used_sec"
        private const val KEY_TODAY_DATE = "today_date_stamp"

        // sensible defaults for a first run
        const val DEFAULT_SESSION_LENGTH_SEC = 10 * 60   // 10 minutes
        const val DEFAULT_COOLDOWN_MIN = 30              // wait 30 min before next session
        const val DEFAULT_DAILY_CAP_MIN = 40             // 40 min total per day
        const val DEFAULT_PIN = "1234"
    }

    // ---------- Parent-configurable settings ----------

    var sessionLengthSec: Int
        get() = prefs.getInt(KEY_SESSION_LENGTH_SEC, DEFAULT_SESSION_LENGTH_SEC)
        set(value) = prefs.edit().putInt(KEY_SESSION_LENGTH_SEC, value).apply()

    var cooldownMin: Int
        get() = prefs.getInt(KEY_COOLDOWN_MIN, DEFAULT_COOLDOWN_MIN)
        set(value) = prefs.edit().putInt(KEY_COOLDOWN_MIN, value).apply()

    var dailyCapMin: Int
        get() = prefs.getInt(KEY_DAILY_CAP_MIN, DEFAULT_DAILY_CAP_MIN)
        set(value) = prefs.edit().putInt(KEY_DAILY_CAP_MIN, value).apply()

    var parentPin: String
        get() = prefs.getString(KEY_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
        set(value) = prefs.edit().putString(KEY_PIN, value).apply()

    // ---------- Daily usage tracking (resets automatically at midnight) ----------

    private fun todayStamp(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
    }

    private fun rolloverIfNewDay() {
        val stored = prefs.getString(KEY_TODAY_DATE, null)
        if (stored != todayStamp()) {
            prefs.edit()
                .putString(KEY_TODAY_DATE, todayStamp())
                .putInt(KEY_TODAY_USED_SEC, 0)
                .apply()
        }
    }

    private fun todayUsedSec(): Int {
        rolloverIfNewDay()
        return prefs.getInt(KEY_TODAY_USED_SEC, 0)
    }

    fun addUsedSeconds(seconds: Int) {
        rolloverIfNewDay()
        val newTotal = todayUsedSec() + seconds
        prefs.edit().putInt(KEY_TODAY_USED_SEC, newTotal).apply()
    }

    fun remainingDailyCapSec(): Int {
        val capSec = dailyCapMin * 60
        val used = todayUsedSec()
        return (capSec - used).coerceAtLeast(0)
    }

    // ---------- Cooldown between sessions ----------

    fun markSessionEndedNow() {
        prefs.edit().putLong(KEY_LAST_SESSION_END, System.currentTimeMillis()).apply()
    }

    /** Returns 0 if she's allowed to watch right now, otherwise seconds still left to wait. */
    fun cooldownRemainingSec(): Int {
        val lastEnd = prefs.getLong(KEY_LAST_SESSION_END, 0L)
        if (lastEnd == 0L) return 0
        val cooldownMillis = cooldownMin * 60 * 1000L
        val elapsed = System.currentTimeMillis() - lastEnd
        val remainingMillis = cooldownMillis - elapsed
        return if (remainingMillis <= 0) 0 else (remainingMillis / 1000).toInt()
    }

    /** How long (in seconds) the NEXT session is allowed to run, capped by remaining daily budget. */
    fun nextAllowedSessionLengthSec(): Int {
        return minOf(sessionLengthSec, remainingDailyCapSec())
    }

    fun canWatchNow(): Boolean {
        return cooldownRemainingSec() <= 0 && remainingDailyCapSec() > 0
    }

    // Slowly wean total daily time down over weeks. Call this manually, e.g. once a week,
    // from Settings ("Reduce daily limit"), rather than automatically, so the parent stays in control.
    fun stepDownDailyCap(byMinutes: Int = 5, floorMinutes: Int = 15) {
        val newCap = (dailyCapMin - byMinutes).coerceAtLeast(floorMinutes)
        dailyCapMin = newCap
    }
}
