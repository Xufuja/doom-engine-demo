package dev.xfj.input;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import dev.xfj.application.Application;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class Input {
    public static boolean isKeyDown(int keyCode) {
        long window = Application.getInstance().getWindowHandle();
        int state = glfwGetKey(window, keyCode);
        return state == GLFW_PRESS;
    }

    public static boolean isMouseButtonDown(int button) {
        long window = Application.getInstance().getWindowHandle();
        int state = glfwGetMouseButton(window, button);
        return state == GLFW_PRESS;
    }

    public static Vector2f getMousePosition() {
        long window = Application.getInstance().getWindowHandle();
        DoubleBuffer xPosition = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer yPosition = BufferUtils.createDoubleBuffer(1);
        glfwGetCursorPos(window, xPosition, yPosition);
        return new Vector2f((float) xPosition.get(0), (float) yPosition.get(0));
    }

    public static void setCursorMode(MouseButtonCodes.CursorMode cursorMode) {
        long window = Application.getInstance().getWindowHandle();
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL + cursorMode.ordinal());
    }
}
