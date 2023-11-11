package dev.xfj;

import dev.xfj.application.Application;
import dev.xfj.application.ApplicationSpecification;
import imgui.ImGui;

public class Main {
    public static void main(String[] args) {
        ApplicationSpecification spec = new ApplicationSpecification();
        spec.name = "Izanagi Example";
        Application app = new Application(spec);
        app.pushLayer(new ExampleLayer());

        app.setMenuBarCallback(() -> {
            if (ImGui.beginMenu("File")) {
                if (ImGui.menuItem("Exit")) {
                    Application.close(Application.getInstance());
                }
                ImGui.endMenu();
            }
        });
        app.run();
    }
}