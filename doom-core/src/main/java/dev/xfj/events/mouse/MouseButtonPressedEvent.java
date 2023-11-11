package dev.xfj.events.mouse;

public class MouseButtonPressedEvent extends MouseButtonEvent {
    private static final EventType eventType = EventType.MouseButtonPressed;

    public MouseButtonPressedEvent(int button) {
        super(button);

    }
    public String toString() {
        return String.format("MouseButtonPressedEvent: %1$d", getMouseButton());
    }

    public static EventType getStaticType() {
        return eventType;
    }

    @Override
    public EventType getEventType() {
        return getStaticType();
    }
}
