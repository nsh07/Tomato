---
mode: testing
target: emulator
app: ./app.apk
max_steps: 40
tags: [timer]
---

# Skip from focus to a break

## Open the timer
Wait for the app to finish loading. If the system asks for notification permission, allow it. Close any welcome or onboarding screen.
Verify the timer screen shows "Focus".

## Skip to the next timer
Tap the "Skip to next" control.
Verify the timer screen now shows "Break" with a time of 05:00.
