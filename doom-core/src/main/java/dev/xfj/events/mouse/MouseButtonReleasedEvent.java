package dev.xfj.events.mouse;

public class MouseButtonReleasedEvent extends MouseButtonEvent {
    private static final EventType eventType = EventType.MouseButtonReleased;

    public MouseButtonReleasedEvent(int button) {
        super(button);

    }
    public String toString() {
        return String.format("MouseButtonReleasedEvent: %1$d", getMouseButton());
    }

    public static EventType getStaticType() {
        return eventType;
    }

    @Override
    public EventType getEventType() {
        return getStaticType();
    }
}
