package dev.xfj.events.application;

import dev.xfj.events.Event;

import java.util.EnumSet;

public class WindowCloseEvent extends Event {
    private static final EventType eventType = EventType.WindowClose;

    public WindowCloseEvent() {
        super(EnumSet.of(EventCategory.EventCategoryApplication));
    }

    public static EventType getStaticType() {
        return eventType;
    }

    @Override
    public EventType getEventType() {
        return getStaticType();
    }

}
