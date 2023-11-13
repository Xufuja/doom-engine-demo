package dev.xfj;

import dev.xfj.application.Application;
import dev.xfj.input.Input;
import dev.xfj.input.KeyCodes;

import java.util.stream.IntStream;

import static dev.xfj.application.Application.*;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.lwjgl.opengl.GL41.*;

public class AppLayer implements Layer {
    private static final float[] COS = new float[360];
    private static final float[] SIN = new float[360];
    private static final Wall[] walls = new Wall[30];
    private static final Sector[] sectors = new Sector[30];
    private static final int NUMBER_SECTORS = 4;
    private static final int NUMBER_WALLS = 16;
    private static final int[] LOAD_SECTORS = {
            0, 4, 0, 40, 2, 3,
            4, 8, 0, 40, 4, 5,
            8, 12, 0, 40, 6, 7,
            12, 16, 0, 40, 0, 1
    };
    private static final int[] LOAD_WALLS = {
            0, 0, 32, 0, 0,
            32, 0, 32, 32, 1,
            32, 32, 0, 32, 0,
            0, 32, 0, 0, 1,

            64, 0, 96, 0, 2,
            96, 0, 96, 32, 3,
            96, 32, 64, 32, 2,
            64, 32, 64, 0, 3,

            64, 64, 96, 64, 4,
            96, 64, 96, 96, 5,
            96, 96, 64, 96, 4,
            64, 96, 64, 64, 5,

            0, 64, 32, 64, 6,
            32, 64, 32, 96, 7,
            32, 96, 0, 96, 6,
            0, 96, 0, 64, 7,
    };

    private Player player;

    static {
        for (int x = 0; x < 360; x++) {
            COS[x] = (float) cos(Math.toRadians(x));
            SIN[x] = (float) sin(Math.toRadians(x));
        }
        IntStream.range(0, walls.length).forEach(i -> walls[i] = new Wall());
        IntStream.range(0, sectors.length).forEach(i -> sectors[i] = new Sector());
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
        player.lookAngle = 0;

        int s;
        int w;
        int v1 = 0;
        int v2 = 0;

        for (s = 0; s < NUMBER_SECTORS; s++) {
            sectors[s].wallStart = LOAD_SECTORS[v1 + 0];
            sectors[s].wallEnd = LOAD_SECTORS[v1 + 1];
            sectors[s].z1 = LOAD_SECTORS[v1 + 2];
            sectors[s].z2 = LOAD_SECTORS[v1 + 3] - LOAD_SECTORS[v1 + 2];
            sectors[s].colorBottom = LOAD_SECTORS[v1 + 4];
            sectors[s].colorTop = LOAD_SECTORS[v1 + 5];
            v1 += 6;

            for (w = sectors[s].wallStart; w < sectors[s].wallEnd; w++) {
                walls[w].x1 = LOAD_WALLS[v2 + 0];
                walls[w].y1 = LOAD_WALLS[v2 + 1];
                walls[w].x2 = LOAD_WALLS[v2 + 2];
                walls[w].y2 = LOAD_WALLS[v2 + 3];
                walls[w].c = LOAD_WALLS[v2 + 4];
                v2 += 5;
            }
        }
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
            player.lookAngle -= 1;
        }
        if (Input.isKeyDown(KeyCodes.D) && Input.isKeyDown(KeyCodes.M)) {
            player.lookAngle += 1;
        }
        if (Input.isKeyDown(KeyCodes.W) && Input.isKeyDown(KeyCodes.M)) {
            player.z -= 4;
        }
        if (Input.isKeyDown(KeyCodes.S) && Input.isKeyDown(KeyCodes.M)) {
            player.z += 4;
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
        int s;
        int w;
        int loop;
        int[] worldX = new int[4];
        int[] worldY = new int[4];
        int[] worldZ = new int[4];

        float cos = COS[player.angle];
        float sin = SIN[player.angle];

        for (s = 0; s < NUMBER_SECTORS - 1; s++) {
            for (w = 0; w < NUMBER_SECTORS - s - 1; w++) {
                if (sectors[w].distance < sectors[w + 1].distance) {
                    Sector st = sectors[w];
                    sectors[w] = sectors[w + 1];
                    sectors[w + 1] = st;
                }
            }
        }

        for (s = 0; s < NUMBER_SECTORS; s++) {
            sectors[s].distance = 0;
            if (player.z < sectors[s].z1) {
                sectors[s].surface = 1;
            } else if (player.z > sectors[s].z2) {
                sectors[s].surface = 2;
            } else {
                sectors[s].surface = 0;
            }

            for (loop = 0; loop < 2; loop++) {
                for (w = sectors[s].wallStart; w < sectors[s].wallEnd; w++) {
                    int x1 = walls[w].x1 - player.x;
                    int y1 = walls[w].y1 - player.y;
                    int x2 = walls[w].x2 - player.x;
                    int y2 = walls[w].y2 - player.y;
                    if (loop == 0) {
                        int swp = x1;
                        x1 = x2;
                        x2 = swp;
                        swp = y1;
                        y1 = y2;
                        y2 = swp;
                    }

                    worldX[0] = (int) (x1 * cos + y1 * sin);
                    worldX[1] = (int) (x2 * cos + y2 * sin);
                    worldX[2] = worldX[0];
                    worldX[3] = worldX[1];

                    worldY[0] = (int) (y1 * cos + x1 * sin);
                    worldY[1] = (int) (y2 * cos + x2 * sin);
                    worldY[2] = worldY[0];
                    worldY[3] = worldY[1];

                    sectors[s].distance += distance(0, 0, (worldX[0] + worldX[1]) / 2, (worldY[0] + worldY[1]) / 2);

                    worldZ[0] = (int) (sectors[s].z1 - player.z + ((player.lookAngle * worldY[0])) / 32.0f);
                    worldZ[1] = (int) (sectors[s].z1 - player.z + ((player.lookAngle * worldY[1])) / 32.0f);
                    worldZ[2] = worldZ[0] + sectors[s].z2;
                    worldZ[3] = worldZ[1] + sectors[s].z2;

                    if (worldY[0] < 1 && worldY[1] < 1) {
                        continue;
                    }

                    if (worldY[0] < 1) {
                        int[] bottomLine = clipBehindPlayer(worldX[0], worldY[0], worldZ[0], worldX[1], worldY[1], worldZ[1]);
                        worldX[0] = bottomLine[0];
                        worldY[0] = bottomLine[1];
                        worldZ[0] = bottomLine[2];

                        int[] topLine = clipBehindPlayer(worldX[2], worldY[2], worldZ[2], worldX[3], worldY[3], worldZ[3]);
                        worldX[2] = topLine[0];
                        worldY[2] = topLine[1];
                        worldZ[2] = topLine[2];
                    }

                    if (worldY[1] < 1) {
                        int[] bottomLine = clipBehindPlayer(worldX[1], worldY[1], worldZ[1], worldX[0], worldY[0], worldZ[0]);
                        worldX[1] = bottomLine[0];
                        worldY[1] = bottomLine[1];
                        worldZ[1] = bottomLine[2];

                        int[] topLine = clipBehindPlayer(worldX[3], worldY[3], worldZ[3], worldX[2], worldY[2], worldZ[2]);
                        worldX[3] = topLine[0];
                        worldY[3] = topLine[1];
                        worldZ[3] = topLine[2];
                    }

                    worldX[0] = worldX[0] * 200 / worldY[0] + SCREEN_WIDTH_HALF;
                    worldY[0] = worldZ[0] * 200 / worldY[0] + SCREEN_HEIGHT_HALF;

                    worldX[1] = worldX[1] * 200 / worldY[1] + SCREEN_WIDTH_HALF;
                    worldY[1] = worldZ[1] * 200 / worldY[1] + SCREEN_HEIGHT_HALF;

                    worldX[2] = worldX[2] * 200 / worldY[2] + SCREEN_WIDTH_HALF;
                    worldY[2] = worldZ[2] * 200 / worldY[2] + SCREEN_HEIGHT_HALF;

                    worldX[3] = worldX[3] * 200 / worldY[3] + SCREEN_WIDTH_HALF;
                    worldY[3] = worldZ[3] * 200 / worldY[3] + SCREEN_HEIGHT_HALF;

                    drawWall(worldX[0], worldX[1], worldY[0], worldY[1], worldY[2], worldY[3], walls[w].c, s);
                }
                sectors[s].distance /= (sectors[s].wallEnd - sectors[s].wallStart);
                sectors[s].surface *= -1;
            }
        }
    }

    private void drawWall(int x1, int x2, int b1, int b2, int t1, int t2, int c, int s) {
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

            if (sectors[s].surface == 1) {
                sectors[s].surf[x] = y1;
                continue;
            }

            if (sectors[s].surface == 2) {
                sectors[s].surf[x] = y2;
                continue;
            }

            if (sectors[s].surface == -1) {
                for (y = sectors[s].surf[x]; y < y1; y++) {
                    drawPixel(x, y, sectors[s].colorBottom);
                }
            }

            if (sectors[s].surface == -2) {
                for (y = y2; y < sectors[s].surf[x]; y++) {
                    drawPixel(x, y, sectors[s].colorTop);
                }
            }

            for (y = y1; y < y2; y++) {
                drawPixel(x, y, c);
            }
        }
    }

    private int[] clipBehindPlayer(int x1, int y1, int z1, int x2, int y2, int z2) {
        float da = y1;
        float db = y2;
        float d = da - db;
        if (d == 0) {
            d = 1;
        }
        float s = da / (da - db);
        x1 = (int) (x1 + s * (x2 - (x1)));
        y1 = (int) (y1 + s * (y2 - (y1)));

        if (y1 == 0) {
            y1 = 1;
        }

        z1 = (int) (z1 + s * (z2 - (z1)));

        return new int[]{x1, y1, z1};
    }

    private int distance(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

}
