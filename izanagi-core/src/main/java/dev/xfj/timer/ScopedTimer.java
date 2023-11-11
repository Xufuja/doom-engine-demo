package dev.xfj.timer;

public class ScopedTimer {
    private String name;
    private Timer timer;

    public ScopedTimer(String name) {
        this.name = name;
        this.timer = new Timer();
    }
    public void stop() {
        float time = timer.elapsedMillis();
        System.out.println(String.format("[TIMER] %1$s - %2$fms", name, time));
    }
}
