package dev.xfj.application;

import dev.xfj.Layer;
import dev.xfj.LayerStack;
import dev.xfj.events.Event;
import dev.xfj.events.EventDispatcher;
import dev.xfj.events.application.WindowCloseEvent;
import dev.xfj.events.application.WindowResizeEvent;
import dev.xfj.events.key.KeyPressedEvent;
import dev.xfj.events.key.KeyReleasedEvent;
import dev.xfj.events.key.KeyTypedEvent;
import dev.xfj.events.mouse.MouseButtonPressedEvent;
import dev.xfj.events.mouse.MouseButtonReleasedEvent;
import dev.xfj.events.mouse.MouseMovedEvent;
import dev.xfj.events.mouse.MouseScrolledEvent;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL41;
import org.lwjgl.opengl.GL45;

import java.util.ListIterator;

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
    private boolean minimized;
    private float timeStep;
    private float frameTime;
    private float lastFrameTime;
    private LayerStack layerStack;
    private EventCallBack.EventCallbackFn eventCallback;

    public Application(ApplicationSpecification specification) {
        this.specification = specification;
        this.minimized = false;
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
        eventCallback = this::onEvent;

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 5);

        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();

        glfwSetWindowSizeCallback(windowHandle, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                specification.width = width;
                specification.height = height;

                WindowResizeEvent event = new WindowResizeEvent(width, height);
                eventCallback.handle(event);
            }
        });

        glfwSetWindowCloseCallback(windowHandle, new GLFWWindowCloseCallback() {
            @Override
            public void invoke(long window) {
                WindowCloseEvent event = new WindowCloseEvent();
                eventCallback.handle(event);
            }
        });

        glfwSetKeyCallback(windowHandle, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scanCode, int action, int mods) {
                switch (action) {
                    case GLFW_PRESS -> {
                        KeyPressedEvent event = new KeyPressedEvent(key);
                        eventCallback.handle(event);
                    }
                    case GLFW_RELEASE -> {
                        KeyReleasedEvent event = new KeyReleasedEvent(key);
                        eventCallback.handle(event);
                    }
                    case GLFW_REPEAT -> {
                        KeyPressedEvent event = new KeyPressedEvent(key, true);
                        eventCallback.handle(event);
                    }
                }
            }
        });

        glfwSetCharCallback(windowHandle, new GLFWCharCallback() {
            @Override
            public void invoke(long window, int keyCode) {
                KeyTypedEvent event = new KeyTypedEvent(keyCode);
                eventCallback.handle(event);
            }
        });

        glfwSetMouseButtonCallback(windowHandle, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                switch (action) {
                    case GLFW_PRESS -> {
                        MouseButtonPressedEvent event = new MouseButtonPressedEvent(button);
                        eventCallback.handle(event);
                    }
                    case GLFW_RELEASE -> {
                        MouseButtonReleasedEvent event = new MouseButtonReleasedEvent(button);
                        eventCallback.handle(event);
                    }
                }
            }
        });

        glfwSetScrollCallback(windowHandle, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xOffset, double yOffset) {
                MouseScrolledEvent event = new MouseScrolledEvent((float) xOffset, (float) yOffset); //Why the cast?
                eventCallback.handle(event);
            }
        });

        glfwSetCursorPosCallback(windowHandle, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xPosition, double yPosition) {
                MouseMovedEvent event = new MouseMovedEvent((float) xPosition, (float) yPosition);
                eventCallback.handle(event);
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

    public void onEvent(Event event) {
        EventDispatcher eventDispatcher = new EventDispatcher(event);
        eventDispatcher.dispatch(WindowCloseEvent.class, this::onWindowClose);
        eventDispatcher.dispatch(WindowResizeEvent.class, this::onWindowResize);

        ListIterator<Layer> it = layerStack.getLayers().listIterator(layerStack.getLayers().size());
        while (it.hasPrevious()) {
            Layer layer = it.previous();

            if (event.isHandled()) {
                break;
            }

            layer.onEvent(event);
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

    private void close() {
        this.running = false;
    }

    private boolean onWindowClose(WindowCloseEvent windowCloseEvent) {
        close();
        return true;
    }

    private boolean onWindowResize(WindowResizeEvent windowResizeEvent) {
        if (windowResizeEvent.getWidth() == 0 || windowResizeEvent.getHeight() == 0) {
            minimized = true;
            return false;
        }

        minimized = false;
        GL45.glViewport(0, 0, windowResizeEvent.getWidth(), windowResizeEvent.getHeight());
        return false;
    }

    public long getWindowHandle() {
        return windowHandle;
    }

    public ApplicationSpecification getSpecification() {
        return specification;
    }
}
