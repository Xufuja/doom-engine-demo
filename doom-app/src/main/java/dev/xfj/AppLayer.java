package dev.xfj;

import dev.xfj.application.Application;
import dev.xfj.input.Input;
import dev.xfj.input.KeyCodes;

import static dev.xfj.application.Application.*;
import static org.lwjgl.opengl.GL41.*;

public class AppLayer implements Layer {
    private int tick;

    @Override
    public void onAttach() {
        glPointSize(PIXEL_SCALE);
        glOrtho(0, Application.getInstance().getSpecification().width, 0, Application.getInstance().getSpecification().height, -1, 1);
    }

    @Override
    public void onDetach() {
        System.out.println("Detach not implemented!");
    }

    @Override
    public void onUpdate(float ts) {
        int x;
        int y;

        clearBackground();
        movePlayer();
        draw3D();
    }

    @Override
    public void onUIRender() {
    }

    private void drawPixel(int x, int y, int color) {
        int[] rgb = new int[3];
        if (color == 0) {
            rgb[0] = 255;
            rgb[1] = 255;
            rgb[2] = 0;
        } //Yellow
        if (color == 1) {
            rgb[0] = 160;
            rgb[1] = 160;
            rgb[2] = 0;
        } //Yellow darker
        if (color == 2) {
            rgb[0] = 0;
            rgb[1] = 255;
            rgb[2] = 0;
        } //Green
        if (color == 3) {
            rgb[0] = 0;
            rgb[1] = 160;
            rgb[2] = 0;
        } //Green darker
        if (color == 4) {
            rgb[0] = 0;
            rgb[1] = 255;
            rgb[2] = 255;
        } //Cyan
        if (color == 5) {
            rgb[0] = 0;
            rgb[1] = 160;
            rgb[2] = 160;
        } //Cyan darker
        if (color == 6) {
            rgb[0] = 160;
            rgb[1] = 100;
            rgb[2] = 0;
        } //brown
        if (color == 7) {
            rgb[0] = 110;
            rgb[1] = 50;
            rgb[2] = 0;
        } //brown darker
        if (color == 8) {
            rgb[0] = 0;
            rgb[1] = 60;
            rgb[2] = 130;
        } //background
        glColor3ub((byte) rgb[0], (byte) rgb[1], (byte) rgb[2]);
        glBegin(GL_POINTS);
        glVertex2i(x * PIXEL_SCALE + 2, y * PIXEL_SCALE + 2);
        glEnd();
    }

    private void movePlayer() {
        //move up, down, left, right
        if (Input.isKeyDown(KeyCodes.A) && !Input.isKeyDown(KeyCodes.M)) {
            System.out.println("left\n");
        }
        if (Input.isKeyDown(KeyCodes.D) && !Input.isKeyDown(KeyCodes.M)) {
            System.out.println("right\n");
        }
        if (Input.isKeyDown(KeyCodes.W) && !Input.isKeyDown(KeyCodes.M)) {
            System.out.println("up\n");
        }
        if (Input.isKeyDown(KeyCodes.S) && !Input.isKeyDown(KeyCodes.M)) {
            System.out.println("down\n");
        }
        //strafe left, right
        if (Input.isKeyDown(KeyCodes.COMMA)) {
            System.out.println("strafe left\n");
        }
        if (Input.isKeyDown(KeyCodes.PERIOD)) {
            System.out.println("strafe right\n");
        }
        //move up, down, look up, look down
        if (Input.isKeyDown(KeyCodes.A) && Input.isKeyDown(KeyCodes.M)) {
            System.out.println("look up\n");
        }
        if (Input.isKeyDown(KeyCodes.D) && Input.isKeyDown(KeyCodes.M)) {
            System.out.println("look down\n");
        }
        if (Input.isKeyDown(KeyCodes.W) && Input.isKeyDown(KeyCodes.M)) {
            System.out.println("move up\n");
        }
        if (Input.isKeyDown(KeyCodes.S) && Input.isKeyDown(KeyCodes.M)) {
            System.out.println("move down\n");
        }
    }

    private void clearBackground() {
        int x;
        int y;

        for (y = 0; y < SCREEN_HEIGHT; y++) {
            for (x = 0; x < SCREEN_WIDTH; x++) {
                drawPixel(x, y, 8);
            } //clear background color
        }
    }

    private void draw3D() {
        int x;
        int y;
        int c = 0;

        for (y = 0; y < SCREEN_HEIGHT_HALF; y++) {
            for (x = 0; x < SCREEN_WIDTH_HALF; x++) {
                drawPixel(x, y, c);
                c += 1;
                if (c > 8) {
                    c = 0;
                }
            }
        }
        //frame rate
        tick += 1;
        if (tick > 20) {
            tick = 0;
        }
        drawPixel(SCREEN_WIDTH_HALF, SCREEN_HEIGHT_HALF + tick, 0);
    }

}
