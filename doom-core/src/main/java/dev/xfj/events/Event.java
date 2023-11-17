package dev.xfj.events;

import java.util.EnumSet;
import java.util.Set;

public abstract class Event {
    private final EnumSet<EventCategory> eventCategories;
    protected boolean handled;

    protected Event(EnumSet<EventCategory> eventCategories) {
        //He sets the type via a macro but cannot do that in Java
        this.eventCategories = eventCategories;
        this.handled = false;
    }

    public enum EventType {
        None,
        WindowClose, WindowResize, WindowFocus, WindowLostFocus, WindowMoved,
        AppTick, AppUpdate, AppRender,
        KeyPressed, KeyReleased, KeyTyped,
        MouseButtonPressed, MouseButtonReleased, MouseMoved, MouseScrolled
    }

    public enum EventCategory {
        None(0),
        EventCategoryApplication(1 << 0),
        EventCategoryInput(1 << 1),
        EventCategoryKeyboard(1 << 2),
        EventCategoryMouse(1 << 3),
        EventCategoryMouseButton(1 << 4);

        private final long categoryValue;

        EventCategory(long categoryValue) {
            this.categoryValue = categoryValue;
        }

        public long getCategoryValue() {
            return categoryValue;
        }
    }

    public EnumSet<EventCategory> getEventCategory(long statusValue) {
        EnumSet<EventCategory> categoryFlags = EnumSet.noneOf(EventCategory.class);
        for (EventCategory statusFlag : EventCategory.values()) {
            long flagValue = statusFlag.getCategoryValue();
            if ((flagValue & statusValue) == flagValue) {
                categoryFlags.add(statusFlag);
            }
        }
        return categoryFlags;
    }

    public long getFlagValue(Set<EventCategory> flags) {
        long value = 0;
        for (EventCategory statusFlag : flags) {
            value |= statusFlag.getCategoryValue();
        }
        return value;
    }

    public boolean isInCategory(EventCategory eventCategory) {
        return getCategoryFlags().contains(eventCategory);
    }

    public String getName() {
        return getEventType().name();
    }

    public boolean isHandled() {
        return handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }

    public EnumSet<EventCategory> getCategoryFlags() {
        return eventCategories;
    }

    //Cannot be marked as abstract, but functionally is
    public static EventType getStaticEventType() {
        return EventType.None;
    }

    public abstract EventType getEventType();

}