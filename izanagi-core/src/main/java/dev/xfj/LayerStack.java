package dev.xfj;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LayerStack {
    private final List<Layer> layers;
    private int layerInsertIndex;

    public LayerStack() {
        layers = new ArrayList<>();
        layerInsertIndex = 0;
    }

    public void pushLayer(Layer layer) {
        layers.add(layerInsertIndex, layer);
        layerInsertIndex++;
        layer.onAttach();
    }

    public void pushOverlay(Layer overlay) {
        layers.add(overlay);
        overlay.onAttach();
    }

    public void popLayer(Layer layer) {
        Optional<Integer> index = findIndex(layer);
        index.ifPresent(i -> {
            layer.onDetach();
            layers.remove((int) i);
            layerInsertIndex--;
        });
    }

    public void popOverlay(Layer overlay) {
        Optional<Integer> index = findIndex(overlay);
        index.ifPresent(i -> {
            overlay.onDetach();
            layers.remove((int) i);
        });

    }

    public List<Layer> getLayers() {
        return layers;
    }

    private Optional<Integer> findIndex(Layer layer) {
        int index = layers.indexOf(layer);
        return (index != -1) ? Optional.of(index) : Optional.empty();
    }

}
