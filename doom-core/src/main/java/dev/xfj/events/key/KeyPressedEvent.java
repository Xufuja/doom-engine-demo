package dev.xfj.events.key;

public class KeyPressedEvent extends KeyEvent {
    private static final EventType eventType = EventType.KeyPressed;
    private final boolean isRepeat;

    public KeyPressedEvent(int keyCode) {
        this(keyCode, false);
    }

    public KeyPressedEvent(int keyCode, boolean isRepeat) {
        super(keyCode);
        this.isRepeat = isRepeat;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public String toString() {
        return String.format("KeyPressedEvent: %1$d (repeat = %2$d)", getKeyCode(), isRepeat);
    }

    public static EventType getStaticType() {
        return eventType;
    }

    @Override
    public EventType getEventType() {
        return getStaticType();
    }
}
