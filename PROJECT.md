# Toddler Cartoons — Project Log

## Aim

My daughter is 2 years 8 months old and asks for the phone constantly to watch
cartoons. Goal is not just to block the phone, but to reduce how much she
*wants* it over time, without scaring or upsetting her in the process.

This app is the tool for that. It is not a general kids' app — it's built
specifically around her, and this file is where I track what I'm building,
why, and whether it's actually working with her in real life (not just
whether it works technically).

## Ground rules I decided on (and why)

These came out of thinking through what actually helps at this age vs. what
just changes the shape of the problem:

- **No scary/startling sounds or glitch effects when time is up.** A toddler
  can't connect "the phone got weird" to "screens are bad for me" — she'd
  just learn the phone is unpredictable, which tends to make kids *more*
  anxious/clingy around it, not less interested.
- **No forced WiFi disconnect or phone shutdown.** Not reliably possible on
  modern Android without root anyway, and not needed — screen pinning +
  in-app time limits get the same practical result (she can't keep watching).
- **Ending should be boring, not dramatic.** Boring = nothing to react to =
  less reason to keep chasing it. The "All Done" screen is the same static
  image and phrase every single time on purpose.
- **Predictable countdown, not a surprise cutoff.** A calm shrinking ring so
  she can see the end coming, instead of the video just stopping.
- **Cooldown + daily cap, reduced gradually over weeks**, not an abrupt one-time
  cut. The idea is to lower total demand over time, not just enforce a wall
  every session.

## Current approach (as of last update)

- Parent-managed video list, added/removed **from inside the app** (Settings →
  Manage Videos, PIN-gated) — no code editing or Android Studio needed to
  change what's available. Videos aren't copied/duplicated; the app just
  keeps permission to play the file the parent picked (from Gallery,
  Downloads, wherever).
- Session timer (default 10 min) with a plain visual countdown ring.
- Screen pinning (`startLockTask()`) so she can't exit to home screen /
  other apps mid-video.
- **All touch input during playback is absorbed and does nothing** — tapping,
  dragging, mashing the screen with both hands has no effect on playback.
  This was a deliberate design choice: rather than trying to detect and react
  to specific touches, the simplest and most robust approach is to just not
  let touches do anything at all while a video is playing. Nothing to
  accidentally trigger.
- A **hidden parent-only exit**: press and hold the bottom-left corner for
  ~3 seconds, then enter the PIN, to end a session early (dinner time,
  errand, doctor's visit, etc.). Requires a sustained hold specifically so
  a toddler's normal tapping/swiping can't trigger it by accident. Ending
  early this way does NOT start the cooldown timer, since she didn't get
  her full session.
- After a session: plain "All Done" screen, no retry button, no way back into
  a video without a parent handing the device back.
- Cooldown period before she's allowed to start another session.
- Daily total time cap (resets at midnight), which I can manually step down
  from Settings as things improve.
- Everything configurable (session length, cooldown, daily cap, PIN, video
  list) lives behind a PIN-gated Settings screen.

## How to build without Android Studio

Since you're using GitHub instead of Android Studio locally, use the included
GitHub Actions workflow (`.github/workflows/build.yml`):

1. Push this project to a GitHub repo.
2. Go to the repo's **Actions** tab — a build should run automatically on
   every push to `main`.
3. Once it finishes, open the finished run and download the **app-debug**
   artifact — that's your installable `.apk` file.
4. Copy the `.apk` to the phone (e.g. via USB, Google Drive, or email to
   yourself) and open it on the phone to install. You'll need to allow
   "install from unknown sources" for that once, in Android settings.

No Android Studio needed for this flow — GitHub's servers do the actual
build. You only need Android Studio (or similar) if you want to test changes
instantly on an emulator before pushing, which isn't required to ship.

## Update log

Add a new dated entry every time something changes — a setting, a feature, or
just an observation from watching how she reacts. Keep entries short. The
point is to notice patterns over weeks, not to write essays.

Template to copy:

```
### YYYY-MM-DD
- Changed:
- Observed:
- Keep / Change next time:
```

---

### 2026-07-25
- Changed: Initial build — session timer, countdown ring, screen pinning,
  plain "All Done" screen, cooldown + daily cap, PIN-gated settings.
- Observed: Not tested with her yet.
- Keep / Change next time: Install the first build, watch one full cycle
  (ask → watch → timer ends → "All Done" screen) and note her actual
  reaction here before changing anything else.

### 2026-07-25 (later same day)
- Changed: Videos can now be added/removed from inside the app (Settings →
  Manage Videos) instead of being hardcoded — no more editing code to swap
  cartoons. Added full touch-lockdown during playback (all touches do
  nothing) plus a hidden long-press + PIN exit for parents to end a session
  early. Auto-generated thumbnails from each video instead of one generic
  placeholder icon.
- Observed: Not tested with her yet.
- Keep / Change next time: After first real use, note whether the 3-second
  hold for the parent exit is a good length — long enough she can't trigger
  it, short enough it's not annoying for you.

## Feature ideas — backlog (not built yet)

Ideas worth considering later, roughly in order of how much they'd likely help:

- **Earn-to-watch**: a simple in-app task (tap-the-shapes puzzle, matching
  game) she completes before a session unlocks — shifts the pattern from
  "demand phone" to "finish task, then watch."
- **Rotating video order**: instead of always showing the same videos in the
  same grid position, rotate/shuffle which ones are offered each day, so she
  doesn't fixate on one specific episode and melt down if it's not first.
- **Usage dashboard for you**: a simple screen showing today's/this week's
  total minutes watched, so you can see the trend over time instead of just
  remembering it.
- **Bedtime/mealtime lockout windows**: block sessions entirely during set
  hours (e.g. no cartoons after 7pm) regardless of cooldown/cap status.
- **Auto-shrinking session length**: instead of only the daily cap stepping
  down, gradually shorten individual session length too (e.g. 10 min → 9 →
  8) over a set number of weeks, fully automatically.
- **Second PIN for "co-viewing mode"**: a mode where you sit with her that
  temporarily disables the touch-lock, in case you want to pause/rewind
  together sometimes rather than always hands-off.

### 2026-07-25 (build fixes)
- Changed: Fixed 3 build errors found while getting GitHub Actions working:
  (1) a Kotlin syntax bug calling a property as a function
  (`currentProgress()` → `currentProgress`), (2) missing app icon — added a
  real simple adaptive icon instead of just removing the reference, (3) an
  invalid placeholder file in `res/raw/` that would have broken the resource
  build (folder was unused anyway since videos are now added at runtime, not
  bundled — removed it). Did a full pass matching every XML view ID against
  every Kotlin reference to catch anything else before pushing to a fresh repo.
- Observed: —
- Keep / Change next time: If GitHub Actions still fails after this, paste
  the exact error text — it'll be something new, not a repeat of these three.

<!-- Add new entries above this line -->
