package dev.xfj.events.key;

public class KeyTypedEvent extends KeyEvent {
    private static final EventType eventType = EventType.KeyTyped;

    public KeyTypedEvent(int keyCode) {
        super(keyCode);

    }

    public String toString() {
        return String.format("KeyTypedEvent: %1$d", getKeyCode());
    }

    public static EventType getStaticType() {
        return eventType;
    }

    @Override
    public EventType getEventType() {
        return getStaticType();
    }
}
