package dev.xfj.events.key;

public class KeyReleasedEvent extends KeyEvent {
    private static final EventType eventType = EventType.KeyReleased;

    public KeyReleasedEvent(int keyCode) {
        super(keyCode);

    }

    public String toString() {
        return String.format("KeyReleasedEvent: %1$d", getKeyCode());
    }

    public static EventType getStaticType() {
        return eventType;
    }

    @Override
    public EventType getEventType() {
        return getStaticType();
    }
}
