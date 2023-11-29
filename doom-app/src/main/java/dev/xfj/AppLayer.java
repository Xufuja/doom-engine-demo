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
        IntStream.range(0, TEXTURES.length).forEach(i -> TEXTURES[i] = new Texture());
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

        TEXTURES[0].name = loadArray("textures\\t00.txt");
        TEXTURES[0].h = 16;
        TEXTURES[0].w = 16;
        TEXTURES[1].name = loadArray("textures\\t01.txt");
        TEXTURES[1].h = 16;
        TEXTURES[1].w = 16;
        TEXTURES[2].name = loadArray("textures\\t02.txt");
        TEXTURES[2].h = 16;
        TEXTURES[2].w = 16;
        TEXTURES[3].name = loadArray("textures\\t03.txt");
        TEXTURES[3].h = 16;
        TEXTURES[3].w = 16;
        TEXTURES[4].name = loadArray("textures\\t04.txt");
        TEXTURES[4].h = 16;
        TEXTURES[4].w = 16;
        TEXTURES[5].name = loadArray("textures\\t05.txt");
        TEXTURES[5].h = 16;
        TEXTURES[5].w = 16;
        TEXTURES[6].name = loadArray("textures\\t06.txt");
        TEXTURES[6].h = 16;
        TEXTURES[6].w = 16;
        TEXTURES[7].name = loadArray("textures\\t07.txt");
        TEXTURES[7].h = 16;
        TEXTURES[7].w = 16;
        TEXTURES[8].name = loadArray("textures\\t08.txt");
        TEXTURES[8].h = 32;
        TEXTURES[8].w = 32;
        TEXTURES[9].name = loadArray("textures\\t09.txt");
        TEXTURES[9].h = 64;
        TEXTURES[9].w = 64;
        TEXTURES[10].name = loadArray("textures\\t10.txt");
        TEXTURES[10].h = 16;
        TEXTURES[10].w = 16;
        TEXTURES[11].name = loadArray("textures\\t11.txt");
        TEXTURES[11].h = 16;
        TEXTURES[11].w = 16;
        TEXTURES[12].name = loadArray("textures\\t12.txt");
        TEXTURES[12].h = 16;
        TEXTURES[12].w = 16;
        TEXTURES[13].name = loadArray("textures\\t13.txt");
        TEXTURES[13].h = 16;
        TEXTURES[13].w = 16;
        TEXTURES[14].name = loadArray("textures\\t14.txt");
        TEXTURES[14].h = 16;
        TEXTURES[14].w = 16;
        TEXTURES[15].name = loadArray("textures\\t15.txt");
        TEXTURES[15].h = 16;
        TEXTURES[15].w = 16;
        TEXTURES[16].name = loadArray("textures\\t16.txt");
        TEXTURES[16].h = 16;
        TEXTURES[16].w = 16;
        TEXTURES[17].name = loadArray("textures\\t17.txt");
        TEXTURES[17].h = 16;
        TEXTURES[17].w = 16;
        TEXTURES[18].name = loadArray("textures\\t18.txt");
        TEXTURES[18].h = 16;
        TEXTURES[18].w = 16;
        TEXTURES[19].name = loadArray("textures\\t19.txt");
        TEXTURES[19].h = 16;
        TEXTURES[19].w = 16;
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
        //testTextures();
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

    private void drawPixel(int x, int y, int r, int g, int b) {
        glColor3ub((byte) r, (byte) g, (byte) b);
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
                drawPixel(x, y, 0, 60, 130);
            } //clear background color
        }
    }

    private void draw3D() {
        int x;
        int s;
        int w;
        int frontBack;
        int cycles;
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
                cycles = 2;

                for (x = 0; x < SCREEN_WIDTH; x++) {
                    SECTORS[s].surf[x] = SCREEN_HEIGHT;
                }
            } else if (player.z > SECTORS[s].z2) {
                SECTORS[s].surface = 2;
                cycles = 2;

                for (x = 0; x < SCREEN_WIDTH; x++) {
                    SECTORS[s].surf[x] = 0;
                }
            } else {
                SECTORS[s].surface = 0;
                cycles = 1;
            }

            for (frontBack = 0; frontBack < cycles; frontBack++) {
                for (w = SECTORS[s].wallStart; w < SECTORS[s].wallEnd; w++) {
                    int x1 = WALLS[w].x1 - player.x;
                    int y1 = WALLS[w].y1 - player.y;
                    int x2 = WALLS[w].x2 - player.x;
                    int y2 = WALLS[w].y2 - player.y;

                    if (frontBack == 1) {
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

                    drawWall(worldX[0], worldX[1], worldY[0], worldY[1], worldY[2], worldY[3], s, w, frontBack);
                }

                if ((SECTORS[s].wallEnd - SECTORS[s].wallStart) != 0) {
                    SECTORS[s].distance /= (SECTORS[s].wallEnd - SECTORS[s].wallStart);
                }
            }
        }
    }

    private void drawWall(int x1, int x2, int b1, int b2, int t1, int t2, int s, int w, int frontBack) {
        int x;
        int y;

        int wt = WALLS[w].wt;
        float ht = 0;
        float htStep = (float) TEXTURES[wt].w * WALLS[w].u / (float) (x2 - x1);

        int dyb = b2 - b1;
        int dyt = t2 - t1;
        int dx = x2 - x1;

        if (dx == 0) {
            dx = 1;
        }

        int xs = x1;

        if (x1 < 0) {
            ht -= htStep * x1;
            x1 = 0;
        }

        if (x2 < 0) {
            x2 = 0;
        }

        if (x1 > SCREEN_WIDTH) {
            x1 = SCREEN_WIDTH;
        }

        if (x2 > SCREEN_WIDTH) {
            x2 = SCREEN_WIDTH;
        }

        for (x = x1; x < x2; x++) {
            int y1 = (int) (dyb * (x - xs + 0.5f) / dx + b1);
            int y2 = (int) (dyt * (x - xs + 0.5f) / dx + t1);

            float vt = 0;
            float vtStep = (float) TEXTURES[wt].h * WALLS[w].v / (float) (y2 - y1);

            if (y1 < 0) {
                vt -= vtStep * y1;
                y1 = 0;
            }

            if (y2 < 0) {
                y2 = 0;
            }

            if (y1 > SCREEN_HEIGHT) {
                y1 = SCREEN_HEIGHT;
            }

            if (y2 > SCREEN_HEIGHT) {
                y2 = SCREEN_HEIGHT;
            }

            if (frontBack == 0) {
                if (SECTORS[s].surface == 1) {
                    SECTORS[s].surf[x] = y1;
                }

                if (SECTORS[s].surface == 2) {
                    SECTORS[s].surf[x] = y2;
                }

                for (y = y1; y < y2; y++) {
                    int pixel = (int) (TEXTURES[wt].h - ((int) vt % TEXTURES[wt].h) - 1) * 3 * TEXTURES[wt].w + ((int) ht % TEXTURES[wt].w) * 3;

                    int r = TEXTURES[wt].name[pixel + 0] - WALLS[w].shade / 2;
                    if (r < 0) {
                        r = 0;
                    }

                    int g = TEXTURES[wt].name[pixel + 1] - WALLS[w].shade / 2;
                    if (g < 0) {
                        g = 0;
                    }

                    int b = TEXTURES[wt].name[pixel + 2] - WALLS[w].shade / 2;
                    if (b < 0) {
                        b = 0;
                    }

                    drawPixel(x, y, r, g, b);
                    vt += vtStep;

                }
                ht += htStep;
            }

            if (frontBack == 1) {
                int xo = SCREEN_WIDTH / 2;
                int yo = SCREEN_HEIGHT / 2;
                float fov = 200.0f;
                int xx2 = x - xo;
                int wo = 0;

                if (SECTORS[s].surface == 1) {
                    y2 = SECTORS[s].surf[x];
                    wo = SECTORS[s].z1;
                }

                if (SECTORS[s].surface == 2) {
                    y1 = SECTORS[s].surf[x];
                    wo = SECTORS[s].z2;
                }

                float lookUpDown = -player.lookAngle * 6.2f;

                if (lookUpDown > SCREEN_HEIGHT) {
                    lookUpDown = SCREEN_HEIGHT;
                }

                float moveUpDown = (float) (player.z - wo) / (float) yo;

                if (moveUpDown == 0) {
                    moveUpDown = 0.001f;
                }

                int ys = y1 - yo;
                int ye = y2 - yo;

                for (y = ys; y < ye; y++) {
                    float z = y + lookUpDown;

                    if (z == 0) {
                        z = 0.0001f;
                    }

                    float fx = xx2 / z * moveUpDown;
                    float fy = fov / z * moveUpDown;
                    float rx = fx * SIN[player.angle] - fy * COS[player.angle] + (player.y / 60.0f);
                    float ry = fx * COS[player.angle] + fy * SIN[player.angle] - (player.x / 60.0f);

                    if (rx < 0) {
                        rx = -rx + 1;
                    }

                    if (ry < 0) {
                        ry = -ry + 1;
                    }

                    if ((int) rx % 2 == (int) ry % 2) {
                        drawPixel(xx2 + xo, y + yo, 255, 0, 0);
                    } else {
                        drawPixel(xx2 + xo, y + yo, 0, 255, 0);
                    }
                }
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

    private void testTextures() {
        int x;
        int y;
        int t = 4;

        for (y = 0; y < TEXTURES[t].h; y++) {
            for (x = 0; x < TEXTURES[t].w; x++) {
                int pixel = (TEXTURES[t].h - y - 1) * 3 * TEXTURES[t].w + x * 3;
                int r = TEXTURES[t].name[pixel + 0];
                int g = TEXTURES[t].name[pixel + 1];
                int b = TEXTURES[t].name[pixel + 2];
                drawPixel(x, y, r, g, b);
            }
        }
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

    private static int[] loadArray(String path) {
        try {
            return Files.readAllLines(Path.of(path)).stream().mapToInt(Integer::parseInt).toArray();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
