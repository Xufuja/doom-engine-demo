package dev.xfj.events.mouse;

import dev.xfj.events.Event;

import java.util.EnumSet;

public class MouseScrolledEvent extends Event {
    private static final EventType eventType = EventType.MouseScrolled;

    private final float xOffset;
    private final float yOffset;
    public MouseScrolledEvent(float xOffset, float yOffset) {
        super(EnumSet.of(EventCategory.EventCategoryMouse, EventCategory.EventCategoryInput));
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public float getxOffset() {
        return xOffset;
    }

    public float getyOffset() {
        return yOffset;
    }

    public String toString() {
        return String.format("MouseScrolledEvent: %1$f, %2$f", xOffset, yOffset);
    }

    public static EventType getStaticType() {
        return eventType;
    }

    @Override
    public EventType getEventType() {
        return getStaticType();
    }
}
