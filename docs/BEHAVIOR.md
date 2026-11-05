# Lag Panic — technical behavior and limitations

This is a **client-only graphics profile manager**, NOT a renderer optimization. It does not promise higher FPS or shorter frametimes.

## Activation
- Manual F8. Settings menu backslash (`\\`). Both are remappable through Minecraft controls.
- Original values and target caps are captured; durable atomic recovery backup written first.
- If recovery backup cannot be written, activation aborts with no settings changed.
- One option applied per configured stage interval: particles, clouds, entity distance, render distance.
- Existing lower graphics quality is never increased. Manual settings changes are not overwritten if the current value differs from the captured original.
- On deactivation, only values still equal to the targets set by Lag Panic are restored.
- When leaving a world the client deactivates and restores values.
- If the game closes unexpectedly, recovery is attempted on next startup; a broken recovery file blocks activation.

## Automatic mode
- OFF by default; repeated **client tick gaps** above configured threshold within a 15-second window.
- Applies 60 second cooldown by default between automatic transitions.
- Restores only automatically activated mode after minimum 30s active and quiet window (default 45s).
- Menus pause sampling; it does not sample exact GPU frame durations.
- Per-setting staged application reduces simultaneous changes but **cannot guarantee that no stutter occurs**.

## Memory safeguards

The optional debug HUD / settings show only Java heap occupancy (used, committed and
maximum). This is not OS process RAM. By default no GC calls are made.
If the user explicitly enables experimental GC, then **manual** F8 panic may issue a
single System.gc() request at high memory pressure (85% of Xmx by default), respecting
a five-minute cooldown. It is issued from a short daemon thread to avoid making
the GC call on the client tick; JVM collector stop-the-world pauses can still freeze
the client. Automatic panic never requests GC. Do not promise lower physical RAM.

## Config
`config/lagpanic.json` — all preferences saved as JSON.
`config/lagpanic-recovery.json` — crash recovery record, should not be manually edited or deleted while Panic is active.

## Status
- Compiles under Minecraft 26.3 and runs unit tests only when CI is green.
- No in-game runtime testing or external performance measurements have been performed.
- Sodium and shader pack compatibility are **not proven**; both may change how graphics options behave.
- GitHub Release is gated behind manual verification and is not published by CI pushes.

## Security and privacy
No telemetry, accounts, network calls, world data modifications, or server-side dependency.
