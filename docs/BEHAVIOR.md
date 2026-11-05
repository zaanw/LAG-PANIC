# Lag Panic behavior

- **F8** activates/deactivates a temporary emergency graphics profile.
- **Right Shift** opens the config screen.
- Profiles: Light (render 12 / entity scale 0.8), Medium (8 / 0.65), Nuclear (4 / 0.5).
- Profiles only **lower** applicable numeric quality settings, never increase them.
- Particle and cloud controls are separately opt-in/out.
- Stores original and applied values in a durable recovery JSON file.
- Restores only options still matching what Lag Panic applied, to avoid stomping on deliberate manual changes.
- Startup recovery protects user settings if the game closed unexpectedly.
- Auto-detection disabled by default. Detects **large pauses between client ticks**, not exact GPU frametimes.
- Auto-trigger requires multiple spikes over ~15s, cooldown ~20s; auto-restore only after a minimum active period and stable interval.
- Render distance changes can trigger chunk geometry rebuilds; aggressive toggling can itself cause stutter.
- No world manipulation, no server plugin, no modification of simulation distance.
- On a dedicated server, local render-distance controls may be capped by server constraints.
- Not verified in an actual Minecraft game session yet.
