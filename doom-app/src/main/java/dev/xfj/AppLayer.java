package dev.xfj;

import dev.xfj.application.Application;
import dev.xfj.input.Input;
import dev.xfj.input.KeyCodes;

import static dev.xfj.application.Application.*;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.lwjgl.opengl.GL41.*;

public class AppLayer implements Layer {
    private Player player;
    private static final float[] COS = new float[360];
    private static final float[] SIN = new float[360];

    static {
        for (int x = 0; x < 360; x++) {
            COS[x]= (float) cos(Math.toRadians(x));
            SIN[x]= (float) sin(Math.toRadians(x));
        }
    }

    @Override
    public void onAttach() {
        glPointSize(PIXEL_SCALE);
        glOrtho(0, Application.getInstance().getSpecification().width, 0, Application.getInstance().getSpecification().height, -1, 1);
        player = new Player();
        player.x = 70;
        player.y = -110;
        player.z = 20;
        player.angle = 0;
        player.upOrDown = 0;
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
            player.angle -= 4;
            if (player.angle < 0) {
                player.angle += 360;
            }
        }
        if (Input.isKeyDown(KeyCodes.D) && !Input.isKeyDown(KeyCodes.M)) {
            player.angle += 4;
            if (player.angle > 359) {
                player.angle -= 360;
            }
        }
        int deltaX = (int) (SIN[player.angle] * 10);
        int deltaY = (int) (COS[player.angle] * 10);

        if (Input.isKeyDown(KeyCodes.W) && !Input.isKeyDown(KeyCodes.M)) {
            player.x += deltaX;
            player.y += deltaY;
        }
        if (Input.isKeyDown(KeyCodes.S) && !Input.isKeyDown(KeyCodes.M)) {
            player.x -= deltaX;
            player.y -= deltaY;
        }
        //strafe left, right
        if (Input.isKeyDown(KeyCodes.COMMA)) {
            player.x += deltaY;
            player.y += deltaX;
        }
        if (Input.isKeyDown(KeyCodes.PERIOD)) {
            player.x -= deltaY;
            player.y -= deltaX;
        }
        //move up, down, look up, look down
        if (Input.isKeyDown(KeyCodes.A) && Input.isKeyDown(KeyCodes.M)) {
            player.upOrDown -= 1;
        }
        if (Input.isKeyDown(KeyCodes.D) && Input.isKeyDown(KeyCodes.M)) {
            player.upOrDown += 1;
        }
        if (Input.isKeyDown(KeyCodes.W) && Input.isKeyDown(KeyCodes.M)) {
            player.upOrDown -= 4;
        }
        if (Input.isKeyDown(KeyCodes.S) && Input.isKeyDown(KeyCodes.M)) {
            player.upOrDown += 4;
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
        int[] worldX = new int[4];
        int[] worldY = new int[4];
        int[] worldZ = new int[4];

        float cos = COS[player.angle];
        float sin = SIN[player.angle];

        int x1 = 40 - player.x;
        int y1 = 10 - player.y;
        int x2 = 40 - player.x;
        int y2 = 290 - player.y;

        worldX[0] = (int) (x1 * cos + y1 * sin);
        worldX[1] = (int) (x2 * cos + y2 * sin);
        worldX[2] = worldX[0];
        worldX[3] = worldX[1];

        worldY[0] = (int) (y1 * cos + x1 * sin);
        worldY[1] = (int) (y2 * cos + x2 * sin);
        worldY[2] = worldY[0];
        worldY[3] = worldY[1];

        worldZ[0] = (int) (0 - player.z + ((player.upOrDown * worldY[0])) / 32.0f);
        worldZ[1] = (int) (0 - player.z + ((player.upOrDown * worldY[1])) / 32.0f);
        worldZ[2] = worldZ[0] + 40;
        worldZ[3] = worldZ[1] + 40;

        worldX[0] = worldX[0] * 200 / worldY[0] + SCREEN_WIDTH_HALF;
        worldY[0] = worldZ[0] * 200 / worldY[0] + SCREEN_HEIGHT_HALF;

        worldX[1] = worldX[1] * 200 / worldY[1] + SCREEN_WIDTH_HALF;
        worldY[1] = worldZ[1] * 200 / worldY[1] + SCREEN_HEIGHT_HALF;

        worldX[2] = worldX[2] * 200 / worldY[2] + SCREEN_WIDTH_HALF;
        worldY[2] = worldZ[2] * 200 / worldY[2] + SCREEN_HEIGHT_HALF;

        worldX[3] = worldX[3] * 200 / worldY[3] + SCREEN_WIDTH_HALF;
        worldY[3] = worldZ[3] * 200 / worldY[3] + SCREEN_HEIGHT_HALF;

        //if (worldX[0] > 0 && worldX[0] < SCREEN_WIDTH && worldY[0] > 0 && worldY[0] < SCREEN_HEIGHT) {
        //    drawPixel(worldX[0], worldY[0], 0);
        //}

        //if (worldX[1] > 0 && worldX[1] < SCREEN_WIDTH && worldY[1] > 0 && worldY[1] < SCREEN_HEIGHT) {
        //    drawPixel(worldX[1], worldY[1], 0);
        //}

        drawWall(worldX[0], worldX[1], worldY[0], worldY[1], worldY[2], worldY[3]);
    }

    private void drawWall(int x1, int x2, int b1, int b2, int t1, int t2) {
        int x;
        int y;
        int dyb = b2 - b1;
        int dyt = t2 - t1;
        int dx = x2 - x1;

        if (dx == 0) {
            dx = 1;
        }

        int xs = x1;

        if (x1 < 1) {
            x1 = 1;
        }

        if (x2 < 1) {
            x2 = 1;
        }

        if (x1 > SCREEN_WIDTH - 1) {
            x1 = SCREEN_WIDTH - 1;
        }

        if (x2 > SCREEN_WIDTH - 1) {
            x2 = SCREEN_WIDTH - 1;
        }

        for (x = x1; x < x2; x++) {
            int y1 = (int) (dyb * (x - xs + 0.5f) / dx + b1);
            int y2 = (int) (dyt * (x - xs + 0.5f) / dx + t1);

            if (y1 < 1) {
                y1 = 1;
            }

            if (y2 < 1) {
                y2 = 1;
            }

            if (y1 > SCREEN_WIDTH - 1) {
                y1 = SCREEN_WIDTH - 1;
            }

            if (y2 > SCREEN_WIDTH - 1) {
                y2 = SCREEN_WIDTH - 1;
            }

            for (y = y1; y < y2; y++) {
                drawPixel(x, y, 0);
            }
        }
    }

}
