package dev.xfj.application;

import dev.xfj.Layer;
import dev.xfj.LayerStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL41;
import org.lwjgl.opengl.GL45;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Application {
    public static final int RESOLUTION = 1;
    public static final int SCREEN_WIDTH = 160 * RESOLUTION;
    public static final int SCREEN_HEIGHT = 120 * RESOLUTION;
    public static final int SCREEN_WIDTH_HALF = SCREEN_WIDTH / 2;
    public static final int SCREEN_HEIGHT_HALF = SCREEN_HEIGHT / 2;
    public static final int PIXEL_SCALE = 4 / RESOLUTION;
    public static final int GL_SCREEN_WIDTH = SCREEN_WIDTH * PIXEL_SCALE;
    public static final int GL_SCREEN_HEIGHT = SCREEN_HEIGHT * PIXEL_SCALE;

    private static Application instance;
    private final ApplicationSpecification specification;
    private long windowHandle;
    private boolean running;
    private float timeStep;
    private float frameTime;
    private float lastFrameTime;
    private LayerStack layerStack;

    public Application(ApplicationSpecification specification) {
        this.specification = specification;
        this.timeStep = 0.0f;
        this.frameTime = 0.0f;
        this.lastFrameTime = 0.0f;
        this.layerStack = new LayerStack();
        instance = this;
        init();
    }

    private void init() {
        boolean success = glfwInit();

        if (!success) {
            throw new RuntimeException("Could not initialize GLFW!");
        } else {
            glfwSetErrorCallback(new GLFWErrorCallback() {
                @Override
                public void invoke(int error, long description) {
                    System.err.println(String.format("GLFW error (%1$d): %2$d", error, description));
                }
            });
        }
        windowHandle = glfwCreateWindow(specification.width, specification.height, specification.name, NULL, NULL);

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 5);

        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();

        glfwSetWindowSizeCallback(windowHandle, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                specification.width = width;
                specification.height = height;

                GL45.glViewport(0, 0, specification.width, specification.height);
            }
        });

        glfwSetWindowCloseCallback(windowHandle, new GLFWWindowCloseCallback() {
            @Override
            public void invoke(long window) {
                running = false;
            }
        });

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 5);

        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();
    }

    public void run() {
        running = true;

        while (running) {
            glfwPollEvents();
            GL41.glClear(GL41.GL_COLOR_BUFFER_BIT | GL41.GL_DEPTH_BUFFER_BIT);


            //float time = getTime();
            //frameTime = time - lastFrameTime;
            //timeStep = Math.min(frameTime, 0.05f);
            //lastFrameTime = time;

            if (timeStep - lastFrameTime >= 0.05f) {
                lastFrameTime = timeStep;

                for (Layer layer : layerStack.getLayers()) {
                    layer.onUpdate(timeStep);
                }

                glfwSwapBuffers(windowHandle);

                for (Layer layer : layerStack.getLayers()) {
                    layer.onUIRender();
                }
            }

            timeStep = getTime();

        }
    }

    public void pushLayer(Layer layer) {
        layerStack.pushLayer(layer);
    }

    public void pushOverlay(Layer layer) {
        layerStack.pushOverlay(layer);
    }

    public float getTime() {
        return (float) glfwGetTime();
    }

    public static Application getInstance() {
        return instance;
    }

    public long getWindowHandle() {
        return windowHandle;
    }

    public ApplicationSpecification getSpecification() {
        return specification;
    }
}
