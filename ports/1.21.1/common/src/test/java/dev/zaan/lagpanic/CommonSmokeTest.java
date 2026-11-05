package dev.zaan.lagpanic;

public final class CommonSmokeTest {
    public static void main(String[] args) {
        FrameMonitor frames = new FrameMonitor();
        long now = 1_000_000_000L;
        for (int i = 0; i < 140; i++) {
            now += 50_000_000L;
            frames.sample(now);
        }
        require(frames.estimatedFps() == 20, "FPS sampling");
        require(frames.sustainedLowFps(35), "Low FPS trigger");
        require(!frames.sustainedLowFps(15), "Threshold handling");
        SpikeDetector spikes = new SpikeDetector();
        long start = 1_000_000_000L;
        require(!spikes.recordTick(start, 200, 3), "First tick");
        require(!spikes.recordTick(start + 300_000_000L, 200, 3), "First spike");
        require(!spikes.recordTick(start + 600_000_000L, 200, 3), "Second spike");
        require(spikes.recordTick(start + 900_000_000L, 200, 3), "Third spike");
        require(Preset.forName("Nuclear").renderDistance() == 4, "Nuclear preset");
        require(Preset.forName("Medium").renderDistance() == 8, "Medium preset");
        require(MemoryPressure.sample().maxMiB() >= 0, "Heap sample");
        System.out.println("Common core smoke tests passed on Java " + Runtime.version());
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
