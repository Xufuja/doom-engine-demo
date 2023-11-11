package dev.xfj;

import dev.xfj.events.Event;

public interface Layer {

    void onAttach();
    void onDetach();
    void onUpdate(float ts);
    void onUIRender();
    void onEvent(Event event);
}
