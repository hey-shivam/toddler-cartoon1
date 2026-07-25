# Toddler Cartoons — starter app

> See `PROJECT.md` for the project's aim, the reasoning behind its design
> choices, and a running log to update every time something changes or you
> learn something from watching how she reacts.

A minimal Android (Kotlin) project implementing:

- A home screen showing a small, fixed set of approved cartoons (no search, no
  autoplay-into-random-content, no ads).
- A timed player screen with a calm, always-visible countdown ring — no jarring
  colors, no scary sounds.
- Screen pinning (`startLockTask()` / `stopLockTask()`) so she can't back out to
  the home screen, notification shade, or other apps while a cartoon is playing.
  This is a standard Android API, no root required.
- A plain, identical-every-time "All done" screen at the end of a session —
  deliberately boring on purpose, with no retry button.
- A cooldown period after each session before the app allows watching again,
  plus a total daily time cap that resets at midnight.
- A PIN-gated Settings screen where you (the parent) set session length,
  cooldown, and daily cap, and can gradually reduce the daily cap over weeks.

## Before you use it

1. **Set a real parent PIN.** The default PIN is `1234` (see
   `SessionManager.DEFAULT_PIN`) — change this before giving the device to your
   daughter, either by editing the default in code or via the Settings screen
   the first time you open it.
2. **Add videos from inside the app.** No code editing needed: open the app →
   Settings (small icon, top-right) → enter PIN → Manage Videos → "+ Add
   Video" → pick a video file already on the phone (Downloads, Gallery,
   wherever you've saved approved cartoon clips) → give it a name. A thumbnail
   is generated automatically. Remove videos the same way, any time.
3. Repeat step 2 whenever you want to swap out what's available — this is
   meant to be an ongoing thing, not a one-time setup.

## What happens if she touches the screen while a video is playing

Every touch during playback is intentionally absorbed and does nothing —
tapping, dragging, mashing with both hands has no effect. Rather than trying
to detect and block specific gestures, the app simply doesn't respond to
touch at all while a video is on screen, which is the most reliable way to
stop accidental pausing/seeking/exiting.

There's one exception, for you: press and hold the bottom-left corner of the
screen for about 3 seconds, then enter the PIN, to end a session early (for
dinner, an errand, etc.). The hold is deliberately long so a toddler's normal
tapping/swiping won't trigger it by accident.

## How to build it (no Android Studio needed)

This repo includes `.github/workflows/build.yml`, which builds a debug APK
automatically on GitHub's servers every time you push to `main`.

1. Push this project to a GitHub repo.
2. Open the repo's **Actions** tab and wait for the run to finish (green
   check).
3. Open that run, scroll to **Artifacts**, and download **app-debug** — it's
   a zip containing `app-debug.apk`.
4. Get the `.apk` onto the phone (USB transfer, Google Drive, or email it to
   yourself) and tap it on the phone to install. Android will ask you to
   allow "install from unknown sources" the first time — that's expected for
   an app not from the Play Store.

If you ever do get access to Android Studio, you can alternatively open the
folder there and run it straight to a connected device/emulator — but it's
not required for this workflow.

## What's intentionally NOT included (and why)

- **No forced WiFi disconnect or phone shutdown.** Modern Android doesn't
  allow a normal app to do either reliably, and screen pinning + the in-app
  cooldown/daily cap achieve the same practical goal (she can't keep watching)
  without needing special/root permissions.
- **No startling sounds or glitch effects at session end.** The "All done"
  screen is deliberately plain and calm — see the reasoning discussed in the
  chat that produced this project.

## Suggested next steps

- Add a simple reward mechanic (e.g. a puzzle screen before unlocking a
  session) if you want to shift from "ask for phone" to "earn the phone."
- Make the approved cartoon list editable from the Settings screen instead of
  hard-coded, if you'll be swapping cartoons often.
- Add `MediaSession`/`ExoPlayer` instead of `VideoView` later if you need
  streaming support, subtitles, or more reliable playback across devices.
