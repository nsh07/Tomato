---
mode: testing
target: emulator
app: ./app.apk
max_steps: 40
tags: [smoke, timer]
---

# Start and pause a focus timer

## Open the timer
Wait for the app to finish loading. If the system asks for notification permission, allow it. Close any welcome or onboarding screen.
Verify the timer screen shows "Focus" and a time of 25:00.

## Start the timer
Tap the play button to start the timer.
Wait 5 seconds.
Verify the time shown is now less than 25:00, so the timer is counting down.

## Pause the timer
Tap the pause button.
Note the time shown, wait 5 seconds, and verify the time has not changed.
