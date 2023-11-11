package dev.xfj.events.mouse;

import dev.xfj.events.Event;

import java.util.EnumSet;

public class MouseMovedEvent extends Event {
    private static final EventType eventType = EventType.MouseMoved;

    private final float x;
    private final float y;
    public MouseMovedEvent(float x, float y) {
        super(EnumSet.of(EventCategory.EventCategoryMouse, EventCategory.EventCategoryInput));
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public String toString() {
        return String.format("MouseMovedEvent: %1$f, %2$f", x, y);
    }

    public static EventType getStaticType() {
        return eventType;
    }

    @Override
    public EventType getEventType() {
        return getStaticType();
    }
}
