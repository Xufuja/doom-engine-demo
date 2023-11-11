package dev.xfj.events.mouse;

import dev.xfj.events.Event;

import java.util.EnumSet;

public abstract class MouseButtonEvent extends Event {
    private final int button;

    public MouseButtonEvent(int button) {
        super(EnumSet.of(EventCategory.EventCategoryMouse, EventCategory.EventCategoryInput, EventCategory.EventCategoryMouseButton));
        this.button = button;
    }

    public int getMouseButton() {
        return button;
    }
}
