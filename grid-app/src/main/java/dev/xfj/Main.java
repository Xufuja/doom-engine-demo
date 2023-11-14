package dev.xfj;

import dev.xfj.application.Application;
import dev.xfj.application.ApplicationSpecification;

public class Main {
    public static void main(String[] args) {
        ApplicationSpecification spec = new ApplicationSpecification();
        spec.name = "Grid Editor";
        Application app = new Application(spec);
        app.pushLayer(new EditorLayer());
        app.run();
    }
}