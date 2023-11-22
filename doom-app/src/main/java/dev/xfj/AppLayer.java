package dev.xfj;

import dev.xfj.application.Application;
import dev.xfj.events.Event;
import dev.xfj.events.EventDispatcher;
import dev.xfj.events.key.KeyPressedEvent;
import dev.xfj.input.Input;
import dev.xfj.input.KeyCodes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import static dev.xfj.application.Application.*;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.lwjgl.opengl.GL41.*;

public class AppLayer implements Layer {
    private static final float[] COS = new float[360];
    private static final float[] SIN = new float[360];
    private static final Wall[] WALLS = new Wall[256];
    private static final Sector[] SECTORS = new Sector[128];
    private static final Texture[] TEXTURES = new Texture[64];
    private static int numberSectors = 4;
    private static int numberWalls = 16;

    private Player player;

    static {
        for (int x = 0; x < 360; x++) {
            COS[x] = (float) cos(Math.toRadians(x));
            SIN[x] = (float) sin(Math.toRadians(x));
        }

        IntStream.range(0, WALLS.length).forEach(i -> WALLS[i] = new Wall());
        IntStream.range(0, SECTORS.length).forEach(i -> SECTORS[i] = new Sector());
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

    @Override
    public void onEvent(Event event) {
        EventDispatcher eventDispatcher = new EventDispatcher(event);
        eventDispatcher.dispatch(KeyPressedEvent.class, this::onKeyPressed);
    }

    private void load() {
        try {
            List<String> lines = Files.readAllLines(Path.of("level.h"));
            numberSectors = Integer.parseInt(lines.get(0));

            for (int i = 0; i < numberSectors; i++) {
                String[] line = lines.get(i + 1).split(" ");
                SECTORS[i].wallStart = Integer.parseInt(line[0]);
                SECTORS[i].wallEnd = Integer.parseInt(line[1]);
                SECTORS[i].z1 = Integer.parseInt(line[2]);
                SECTORS[i].z2 = Integer.parseInt(line[3]);
                SECTORS[i].st = Integer.parseInt(line[4]);
                SECTORS[i].ss = Integer.parseInt(line[5]);
            }

            numberWalls = Integer.parseInt(lines.get(numberSectors + 1));

            for (int i = 0; i < numberWalls; i++) {
                String[] line = lines.get(i + numberSectors + 2).split(" ");
                WALLS[i].x1 = Integer.parseInt(line[0]);
                WALLS[i].y1 = Integer.parseInt(line[1]);
                WALLS[i].x2 = Integer.parseInt(line[2]);
                WALLS[i].y2 = Integer.parseInt(line[3]);
                WALLS[i].wt = Integer.parseInt(line[4]);
                WALLS[i].u = Integer.parseInt(line[5]);
                WALLS[i].v = Integer.parseInt(line[6]);
                WALLS[i].shade = Integer.parseInt(line[7]);

            }
            String[] playerData = lines.get(numberSectors + numberWalls + 3).split(" ");
            player.x = Integer.parseInt(playerData[0]);
            player.y = Integer.parseInt(playerData[1]);
            player.z = Integer.parseInt(playerData[2]);
            player.angle = Integer.parseInt(playerData[3]);
            player.lookAngle = Integer.parseInt(playerData[4]);
        } catch (Exception e) {
            throw new RuntimeException();
        }
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

        for (s = 0; s < numberSectors - 1; s++) {
            for (w = 0; w < numberSectors - s - 1; w++) {
                if (SECTORS[w].distance < SECTORS[w + 1].distance) {
                    Sector st = SECTORS[w];
                    SECTORS[w] = SECTORS[w + 1];
                    SECTORS[w + 1] = st;
                }
            }
        }

        for (s = 0; s < numberSectors; s++) {
            SECTORS[s].distance = 0;
            if (player.z < SECTORS[s].z1) {
                SECTORS[s].surface = 1;
            } else if (player.z > SECTORS[s].z2) {
                SECTORS[s].surface = 2;
            } else {
                SECTORS[s].surface = 0;
            }

            for (loop = 0; loop < 2; loop++) {
                for (w = SECTORS[s].wallStart; w < SECTORS[s].wallEnd; w++) {
                    int x1 = WALLS[w].x1 - player.x;
                    int y1 = WALLS[w].y1 - player.y;
                    int x2 = WALLS[w].x2 - player.x;
                    int y2 = WALLS[w].y2 - player.y;
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

                    SECTORS[s].distance += distance(0, 0, (worldX[0] + worldX[1]) / 2, (worldY[0] + worldY[1]) / 2);

                    worldZ[0] = (int) (SECTORS[s].z1 - player.z + ((player.lookAngle * worldY[0])) / 32.0f);
                    worldZ[1] = (int) (SECTORS[s].z1 - player.z + ((player.lookAngle * worldY[1])) / 32.0f);
                    worldZ[2] = (int) (SECTORS[s].z2 - player.z + ((player.lookAngle * worldY[0])) / 32.0f);
                    worldZ[3] = (int) (SECTORS[s].z2 - player.z + ((player.lookAngle * worldY[1])) / 32.0f);

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

                    drawWall(worldX[0], worldX[1], worldY[0], worldY[1], worldY[2], worldY[3], WALLS[w].c, s);
                }
                if ((SECTORS[s].wallEnd - SECTORS[s].wallStart) != 0) {
                    SECTORS[s].distance /= (SECTORS[s].wallEnd - SECTORS[s].wallStart);
                    SECTORS[s].surface *= -1;
                }
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

            if (SECTORS[s].surface == 1) {
                SECTORS[s].surf[x] = y1;
                continue;
            }

            if (SECTORS[s].surface == 2) {
                SECTORS[s].surf[x] = y2;
                continue;
            }

            if (SECTORS[s].surface == -1) {
                for (y = SECTORS[s].surf[x]; y < y1; y++) {
                    drawPixel(x, y, SECTORS[s].colorBottom);
                }
            }

            if (SECTORS[s].surface == -2) {
                for (y = y2; y < SECTORS[s].surf[x]; y++) {
                    drawPixel(x, y, SECTORS[s].colorTop);
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

    private boolean onKeyPressed(KeyPressedEvent event) {
        if (event.getKeyCode() == KeyCodes.ENTER) {
           load();
        }
        return false;
    }

}
