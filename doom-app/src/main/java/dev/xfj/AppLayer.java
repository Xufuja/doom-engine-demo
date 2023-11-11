package dev.xfj;

import dev.xfj.events.Event;

public class AppLayer implements Layer {

    @Override
    public void onAttach() {
        System.out.println("Attach not implemented!");
    }

    @Override
    public void onDetach() {
        System.out.println("Detach not implemented!");
    }

    @Override
    public void onUpdate(float ts) {
        System.out.println("Update not implemented!");
    }

    @Override
    public void onUIRender() {
    }

    @Override
    public void onEvent(Event event) {
        System.out.println("Event not implemented!");
    }
}
