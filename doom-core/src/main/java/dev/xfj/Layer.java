package dev.xfj;

public interface Layer {

    void onAttach();

    void onDetach();

    void onUpdate(float ts);

    void onUIRender();
}