# Lag Panic — runtime test and benchmark checklist

Compilation and JUnit tests are **not** proof of game behavior or FPS improvement.

Use a disposable Minecraft 26.3 Fabric profile in Prism Launcher, ideally in a copy of a world.

## Functional test

- Start with custom render distance, entity distance, particles and cloud settings. Screenshot them.
- Press F8 and verify that one graphics setting is changed per configured stage interval.
- Light/Medium/Nuclear must never raise an already-lower setting.
- Press F8 again: every setting changed by Lag Panic should return to its prior value.
- While active, manually change one setting in Vanilla Video Settings. Lag Panic must preserve that manual change after deactivation.
- With F8 active, close Minecraft normally. Reopen to verify recovery where settings were persisted.
- Simulate sudden process termination ONLY on a **disposable test instance**, then restart and verify backup restoration.
- Leave a world while Panic is active: original settings should return.
- Test profile changes, independent toggles, threshold, cooldown, stage ticks and HUD.
- Rebind F8 and Right Shift via Minecraft key controls.
- Try a dedicated server without installing Lag Panic on the server.
- Test with Sodium installed and not installed; record compatibility honestly.

## Auto behavior

Auto OFF must never trigger without pressing F8. With Auto ON, repeated tick stalls above threshold may trigger Panic; isolated pauses must not. Auto-activated panic must restore only after prolonged stability, and must not oscillate.

## Benchmark

- Run 5+ repeated routes for ON and OFF, with identical settings, world state, view distance and hardware.
- Capture external FPS/frametimes (p95/p99, 1% low and median).
- Record any stutter caused by applying render distance and any delayed chunk rendering.
- The mod may be worse than vanilla. Publish the results even if there is no improvement.

## Direct JAR release

Do not tag or release until this checklist is completed. After merging the verified code into main, use the manual release workflow with explicit runtime_tested confirmation. It builds and uploads `lagpanic-0.1.0.jar` as a direct GitHub Release asset, not as a ZIP.

**Status: not performed on a live Minecraft client yet.**
