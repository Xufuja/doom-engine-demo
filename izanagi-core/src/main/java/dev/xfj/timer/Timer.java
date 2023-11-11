package dev.xfj.timer;

public class Timer {
    private long start;

    public Timer() {
        reset();
    }

    public void reset() {
        this.start = System.nanoTime();
    }

    public float elapsed() {
        return (System.nanoTime() - start)  * 0.001f * 0.001f * 0.001f;
    }
    public float elapsedMillis() {
        return elapsed() * 1000.0f;
    }
}
