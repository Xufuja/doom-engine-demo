package dev.xfj;

import dev.xfj.application.Application;
import dev.xfj.events.Event;
import dev.xfj.events.EventDispatcher;
import dev.xfj.events.mouse.MouseButtonPressedEvent;
import dev.xfj.events.mouse.MouseButtonReleasedEvent;
import dev.xfj.events.mouse.MouseMovedEvent;
import dev.xfj.input.Input;
import dev.xfj.input.KeyCodes;
import dev.xfj.input.MouseButtonCodes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.IntStream;

import static dev.xfj.application.Application.PIXEL_SCALE;
import static dev.xfj.application.Application.SCREEN_HEIGHT;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.joml.Math.atan2;
import static org.lwjgl.opengl.GL41.*;

public class EditorLayer implements Layer {
    private static final float[] COS = new float[360];
    private static final float[] SIN = new float[360];
    private static final Wall[] WALLS = new Wall[256];
    private static final Sector[] SECTORS = new Sector[128];
    private static final Texture[] TEXTURES = new Texture[64];
    private static final int[] T_NUMBERS = loadArray("textures\\tNumbers.txt");
    private static final int[] T_VIEW2D = loadArray("textures\\tView2D.txt");

    private static int numberTextures = 19;
    private static int numberSectors = 0;
    private static int numberWalls = 0;

    private Player player;
    private Grid grid;
    private int dark;

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
        player.x = 32 * 9;
        player.y = 48;
        player.z = 30;
        player.angle = 0;
        player.lookAngle = 0;
        grid = new Grid();
        initGlobals();

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

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_BLEND);
    }

    @Override
    public void onDetach() {

    }

    @Override
    public void onUpdate(float ts) {
        int x;
        int y;

        movePlayer();
        draw2D();
        darken();

    }

    @Override
    public void onUIRender() {

    }

    @Override
    public void onEvent(Event event) {
        EventDispatcher eventDispatcher = new EventDispatcher(event);
        eventDispatcher.dispatch(MouseButtonPressedEvent.class, this::onMouseButtonPressed);
        eventDispatcher.dispatch(MouseButtonReleasedEvent.class, this::onMouseButtonReleased);
        eventDispatcher.dispatch(MouseMovedEvent.class, this::onMouseMoved);
    }

    private void initGlobals()           //define grid globals
    {
        grid.scale = 4;                //scale down grid
        grid.selS = 0;
        grid.selW = 0;       //select sector, walls
        grid.z1 = 0;
        grid.z2 = 40;        //sector bottom top height
        grid.st = 1;
        grid.ss = 4;         //sector texture, scale
        grid.wt = 0;
        grid.wu = 1;
        grid.wv = 1; //wall texture, u,v
    }

    private void drawPixel(int x, int y, int r, int g, int b) {
        glColor3ub((byte) r, (byte) g, (byte) b);
        glBegin(GL_POINTS);
        glVertex2i(x * PIXEL_SCALE + 2, y * PIXEL_SCALE + 2);
        glEnd();
    }

    private void drawLine(float x1, float y1, float x2, float y2, int r, int g, int b) {
        int n;
        float x = x2 - x1;
        float y = y2 - y1;
        float max = Math.abs(x);

        if (Math.abs(y) > max) {
            max = Math.abs(y);
        }

        x /= max;
        y /= max;

        for (n = 0; n < max; n++) {
            drawPixel((int) x1, (int) y1, r, g, b);
            x1 += x;
            y1 += y;
        }
    }

    private void drawNumber(int nx, int ny, int n) {
        int x;
        int y;

        for (y = 0; y < 5; y++) {
            int y2 = ((5 - y - 1) + 5 * n) * 3 * 12;
            for (x = 0; x < 12; x++) {
                int x2 = x * 3;

                if (T_NUMBERS[y2 + x2] == 0) {
                    continue;
                }

                drawPixel(x + nx, y + ny, 255, 255, 255);
            }
        }
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

    private void draw2D() {
        int s;
        int w;
        int x;
        int y;
        int c;
        //draw background color
        for (y = 0; y < 120; y++) {
            int y2 = (SCREEN_HEIGHT - y - 1) * 3 * 160; //invert height, x3 for rgb, x15 for texture width
            for (x = 0; x < 160; x++) {
                int pixel = x * 3 + y2;
                int r = T_VIEW2D[pixel + 0];
                int g = T_VIEW2D[pixel + 1];
                int b = T_VIEW2D[pixel + 2];
                if (grid.addSect > 0 && y > 48 - 8 && y < 56 - 8 && x > 144) {
                    r = r >> 1;
                    g = g >> 1;
                    b = b >> 1;
                } //darken sector button
                drawPixel(x, y, r, g, b);
            }
        }

        //draw sectors
        for (s = 0; s < numberSectors; s++) {
            for (w = SECTORS[s].ws; w < SECTORS[s].we; w++) {
                if (s == grid.selS - 1) //if this sector is selected
                {
                    //set sector to globals
                    SECTORS[grid.selS - 1].z1 = grid.z1;
                    SECTORS[grid.selS - 1].z2 = grid.z2;
                    SECTORS[grid.selS - 1].st = grid.st;
                    SECTORS[grid.selS - 1].ss = grid.ss;
                    //yellow select
                    if (grid.selW == 0) {
                        c = 80;
                    } //all walls yellow
                    else if (grid.selW + SECTORS[s].ws - 1 == w) {
                        c = 80;
                        WALLS[w].wt = grid.wt;
                        WALLS[w].u = grid.wu;
                        WALLS[w].v = grid.wv;
                    } //one wall selected
                    else {
                        c = 0;
                    } //grey walls
                } else {
                    c = 0;
                } //sector not selected, grey

                drawLine(WALLS[w].x1 / grid.scale, WALLS[w].y1 / grid.scale, WALLS[w].x2 / grid.scale, WALLS[w].y2 / grid.scale, 128 + c, 128 + c, 128 - c);
                drawPixel(WALLS[w].x1 / grid.scale, WALLS[w].y1 / grid.scale, 255, 255, 255);
                drawPixel(WALLS[w].x2 / grid.scale, WALLS[w].y2 / grid.scale, 255, 255, 255);
            }
        }

        //draw player
        int dx = (int) (SIN[player.angle] * 12);
        int dy = (int) (COS[player.angle] * 12);

        drawPixel(player.x / grid.scale, player.y / grid.scale, 0, 255, 0);
        drawPixel((player.x + dx) / grid.scale, (player.y + dy) / grid.scale, 0, 175, 0);

        //draw wall texture
        float tx = 0, tx_stp = (float) (TEXTURES[grid.wt].w / 15.0);
        float ty = 0, ty_stp = (float) (TEXTURES[grid.wt].h / 15.0);

        for (y = 0; y < 15; y++) {
            tx = 0;
            for (x = 0; x < 15; x++) {
                int x2 = (int) tx % TEXTURES[grid.wt].w;
                tx += tx_stp;//*grid.wu;
                int y2 = (int) ty % TEXTURES[grid.wt].h;
                int r = TEXTURES[grid.wt].name[(TEXTURES[grid.wt].h - y2 - 1) * 3 * TEXTURES[grid.wt].w + x2 * 3 + 0];
                int g = TEXTURES[grid.wt].name[(TEXTURES[grid.wt].h - y2 - 1) * 3 * TEXTURES[grid.wt].w + x2 * 3 + 1];
                int b = TEXTURES[grid.wt].name[(TEXTURES[grid.wt].h - y2 - 1) * 3 * TEXTURES[grid.wt].w + x2 * 3 + 2];
                drawPixel(x + 145, y + 105 - 8, r, g, b);
            }
            ty += ty_stp;//*grid.wv;
        }
        //draw surface texture
        tx = 0;
        tx_stp = (float) (TEXTURES[grid.st].w / 15.0);
        ty = 0;
        ty_stp = (float) (TEXTURES[grid.st].h / 15.0);

        for (y = 0; y < 15; y++) {
            tx = 0;
            for (x = 0; x < 15; x++) {
                int x2 = (int) tx % TEXTURES[grid.st].w;
                tx += tx_stp;//*grid.ss;
                int y2 = (int) ty % TEXTURES[grid.st].h;
                int r = TEXTURES[grid.st].name[(TEXTURES[grid.st].h - y2 - 1) * 3 * TEXTURES[grid.st].w + x2 * 3 + 0];
                int g = TEXTURES[grid.st].name[(TEXTURES[grid.st].h - y2 - 1) * 3 * TEXTURES[grid.st].w + x2 * 3 + 1];
                int b = TEXTURES[grid.st].name[(TEXTURES[grid.st].h - y2 - 1) * 3 * TEXTURES[grid.st].w + x2 * 3 + 2];
                drawPixel(x + 145, y + 105 - 24 - 8, r, g, b);
            }
            ty += ty_stp;//*grid.ss;
        }
        //draw numbers
        drawNumber(140, 90, grid.wu);   //wall u
        drawNumber(148, 90, grid.wv);   //wall v
        drawNumber(148, 66, grid.ss);   //surface v
        drawNumber(148, 58, grid.z2);   //top height
        drawNumber(148, 50, grid.z1);   //bottom height
        drawNumber(148, 26, grid.selS); //sector number
        drawNumber(148, 18, grid.selW); //wall number
    }

    private void darken() {
        int x;
        int y;
        int xs = 0;
        int xe = 0;
        int ys = 0;
        int ye = 0;

        if (dark == 0) {
            return;
        }             //no buttons were clicked
        if (dark == 1) {
            xs = 0;
            xe = 15;
            ys = 0 / grid.scale;
            ye = 32 / grid.scale;
        } //save button
        if (dark == 2) {
            xs = 0;
            xe = 3;
            ys = 96 / grid.scale;
            ye = 128 / grid.scale;
        } //u left
        if (dark == 3) {
            xs = 4;
            xe = 8;
            ys = 96 / grid.scale;
            ye = 128 / grid.scale;
        } //u right
        if (dark == 4) {
            xs = 7;
            xe = 11;
            ys = 96 / grid.scale;
            ye = 128 / grid.scale;
        } //v left
        if (dark == 5) {
            xs = 11;
            xe = 15;
            ys = 96 / grid.scale;
            ye = 128 / grid.scale;
        } //u right
        if (dark == 6) {
            xs = 0;
            xe = 8;
            ys = 192 / grid.scale;
            ye = 224 / grid.scale;
        } //u left
        if (dark == 7) {
            xs = 8;
            xe = 15;
            ys = 192 / grid.scale;
            ye = 224 / grid.scale;
        } //u right
        if (dark == 8) {
            xs = 0;
            xe = 7;
            ys = 224 / grid.scale;
            ye = 256 / grid.scale;
        } //Top left
        if (dark == 9) {
            xs = 7;
            xe = 15;
            ys = 224 / grid.scale;
            ye = 256 / grid.scale;
        } //Top right
        if (dark == 10) {
            xs = 0;
            xe = 7;
            ys = 256 / grid.scale;
            ye = 288 / grid.scale;
        } //Bot left
        if (dark == 11) {
            xs = 7;
            xe = 15;
            ys = 256 / grid.scale;
            ye = 288 / grid.scale;
        } //Bot right
        if (dark == 12) {
            xs = 0;
            xe = 7;
            ys = 352 / grid.scale;
            ye = 386 / grid.scale;
        } //sector left
        if (dark == 13) {
            xs = 7;
            xe = 15;
            ys = 352 / grid.scale;
            ye = 386 / grid.scale;
        } //sector right
        if (dark == 14) {
            xs = 0;
            xe = 7;
            ys = 386 / grid.scale;
            ye = 416 / grid.scale;
        } //wall left
        if (dark == 15) {
            xs = 7;
            xe = 15;
            ys = 386 / grid.scale;
            ye = 416 / grid.scale;
        } //wall right
        if (dark == 16) {
            xs = 0;
            xe = 15;
            ys = 416 / grid.scale;
            ye = 448 / grid.scale;
        } //delete
        if (dark == 17) {
            xs = 0;
            xe = 15;
            ys = 448 / grid.scale;
            ye = 480 / grid.scale;
        } //load

        for (y = ys; y < ye; y++) {
            for (x = xs; x < xe; x++) {
                glColor4f(0, 0, 0, 0.4f);
                glBegin(GL_POINTS);
                glVertex2i(x * PIXEL_SCALE + 2 + 580, (120 - y) * PIXEL_SCALE);
                glEnd();
            }
        }
    }

    private void save() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(numberSectors).append("\r\n");

        for (int i = 0; i < numberSectors; i++) {
            stringBuilder.append(SECTORS[i].ws).append(" ");
            stringBuilder.append(SECTORS[i].we).append(" ");
            stringBuilder.append(SECTORS[i].z1).append(" ");
            stringBuilder.append(SECTORS[i].z2).append(" ");
            stringBuilder.append(SECTORS[i].st).append(" ");
            stringBuilder.append(SECTORS[i].ss);
            stringBuilder.append("\r\n");
        }

        stringBuilder.append(numberWalls).append("\r\n");

        for (int i = 0; i < numberWalls; i++) {
            stringBuilder.append(WALLS[i].x1).append(" ");
            stringBuilder.append(WALLS[i].y1).append(" ");
            stringBuilder.append(WALLS[i].x2).append(" ");
            stringBuilder.append(WALLS[i].y2).append(" ");
            stringBuilder.append(WALLS[i].wt).append(" ");
            stringBuilder.append(WALLS[i].u).append(" ");
            stringBuilder.append(WALLS[i].v).append(" ");
            stringBuilder.append(WALLS[i].shade);
            stringBuilder.append("\r\n");
        }
        stringBuilder.append("\r\n");

        stringBuilder.append(player.x).append(" ");
        stringBuilder.append(player.y).append(" ");
        stringBuilder.append(player.z).append(" ");
        stringBuilder.append(player.angle).append(" ");
        stringBuilder.append(player.lookAngle);

        try {
            Files.writeString(Path.of("level.h"), stringBuilder, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void load() {
        try {
            List<String> lines = Files.readAllLines(Path.of("level.h"));
            numberSectors = Integer.parseInt(lines.get(0));

            for (int i = 0; i < numberSectors; i++) {
                String[] line = lines.get(i + 1).split(" ");
                SECTORS[i].ws = Integer.parseInt(line[0]);
                SECTORS[i].we = Integer.parseInt(line[1]);
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

    private boolean onMouseButtonPressed(MouseButtonPressedEvent event) {
        int x = (int) Input.getMousePosition().x;
        int y = (int) Input.getMousePosition().y;

        int s, w;
        //round mouse x,y
        grid.mx = x / PIXEL_SCALE;
        grid.my = SCREEN_HEIGHT - y / PIXEL_SCALE;
        grid.mx = ((grid.mx + 4) >> 3) << 3;
        grid.my = ((grid.my + 4) >> 3) << 3; //nearest 8th 

        if (event.getMouseButton() == MouseButtonCodes.BUTTON_LEFT) {
            //2D view buttons only
            if (x > 580) {
                //2d 3d view buttons
                if (y > 0 && y < 32) {
                    save();
                    dark = 1;
                }
                //wall texture
                if (y > 32 && y < 96) {
                    if (x < 610) {
                        grid.wt -= 1;
                        if (grid.wt < 0) {
                            grid.wt = numberTextures;
                        }
                    } else {
                        grid.wt += 1;
                        if (grid.wt > numberTextures) {
                            grid.wt = 0;
                        }
                    }
                }
                //wall uv
                if (y > 96 && y < 128) {
                    if (x < 595) {
                        dark = 2;
                        grid.wu -= 1;
                        if (grid.wu < 1) {
                            grid.wu = 1;
                        }
                    } else if (x < 610) {
                        dark = 3;
                        grid.wu += 1;
                        if (grid.wu > 9) {
                            grid.wu = 9;
                        }
                    } else if (x < 625) {
                        dark = 4;
                        grid.wv -= 1;
                        if (grid.wv < 1) {
                            grid.wv = 1;
                        }
                    } else if (x < 640) {
                        dark = 5;
                        grid.wv += 1;
                        if (grid.wv > 9) {
                            grid.wv = 9;
                        }
                    }
                }
                //surface texture
                if (y > 128 && y < 192) {
                    if (x < 610) {
                        grid.st -= 1;
                        if (grid.st < 0) {
                            grid.st = numberTextures;
                        }
                    } else {
                        grid.st += 1;
                        if (grid.st > numberTextures) {
                            grid.st = 0;
                        }
                    }
                }
                //surface uv
                if (y > 192 && y < 222) {
                    if (x < 610) {
                        dark = 6;
                        grid.ss -= 1;
                        if (grid.ss < 1) {
                            grid.ss = 1;
                        }
                    } else {
                        dark = 7;
                        grid.ss += 1;
                        if (grid.ss > 9) {
                            grid.ss = 9;
                        }
                    }
                }
                //top height
                if (y > 222 && y < 256) {
                    if (x < 610) {
                        dark = 8;
                        grid.z2 -= 5;
                        if (grid.z2 == grid.z1) {
                            grid.z1 -= 5;
                        }
                    } else {
                        dark = 9;
                        grid.z2 += 5;
                    }
                }
                //bot height
                if (y > 256 && y < 288) {
                    if (x < 610) {
                        dark = 10;
                        grid.z1 -= 5;
                    } else {
                        dark = 11;
                        grid.z1 += 5;
                        if (grid.z1 == grid.z2) {
                            grid.z2 += 5;
                        }
                    }
                }
                //add sector
                if (y > 288 && y < 318) {
                    grid.addSect += 1;
                    grid.selS = 0;
                    grid.selW = 0;
                    if (grid.addSect > 1) {
                        grid.addSect = 0;
                    }
                }
                //limit
                if (grid.z1 < 0) {
                    grid.z1 = 0;
                }
                if (grid.z1 > 145) {
                    grid.z1 = 145;
                }
                if (grid.z2 < 5) {
                    grid.z2 = 5;
                }
                if (grid.z2 > 150) {
                    grid.z2 = 150;
                }

                //select sector
                if (y > 352 && y < 386) {
                    grid.selW = 0;
                    if (x < 610) {
                        dark = 12;
                        grid.selS -= 1;
                        if (grid.selS < 0) {
                            grid.selS = numberSectors;
                        }
                    } else {
                        dark = 13;
                        grid.selS += 1;
                        if (grid.selS > numberSectors) {
                            grid.selS = 0;
                        }
                    }

                    int ss = grid.selS - 1;
                    grid.z1 = SECTORS[ss].z1; //sector bottom height
                    grid.z2 = SECTORS[ss].z2; //sector top height
                    grid.st = SECTORS[ss].st; //surface texture
                    grid.ss = SECTORS[ss].ss; //surface scale
                    grid.wt = WALLS[SECTORS[ss].ws].wt;
                    grid.wu = WALLS[SECTORS[ss].ws].u;
                    grid.wv = WALLS[SECTORS[ss].ws].v;

                    if (grid.selS == 0) {
                        initGlobals();
                    } //defaults 
                }
                //select sector's walls
                if (y > 386 && y < 416) {
                    int snw = SECTORS[grid.selS - 1].we - SECTORS[grid.selS - 1].ws; //sector's number of walls

                    if (x < 610) //select sector wall left
                    {
                        dark = 14;
                        grid.selW -= 1;
                        if (grid.selW < 0) {
                            grid.selW = snw;
                        }
                    } else //select sector wall right
                    {
                        dark = 15;
                        grid.selW += 1;
                        if (grid.selW > snw) {
                            grid.selW = 0;
                        }
                    }
                    if (grid.selW > 0) {
                        grid.wt = WALLS[SECTORS[grid.selS - 1].ws + grid.selW - 1].wt; //printf("ws,%i,%i\n",grid.wt, 1 );
                        grid.wu = WALLS[SECTORS[grid.selS - 1].ws + grid.selW - 1].u;
                        grid.wv = WALLS[SECTORS[grid.selS - 1].ws + grid.selW - 1].v;
                    }
                }
                //delete
                if (y > 416 && y < 448) {
                    dark = 16;
                    if (grid.selS > 0) {
                        int d = grid.selS - 1;                             //delete this one
                        //printf("%i before:%i,%i\n",d, numberSectors,numberWalls);
                        numberWalls -= (SECTORS[d].we - SECTORS[d].ws);                 //first subtract number of walls
                        for (x = d; x < numberSectors; x++) {
                            SECTORS[x] = SECTORS[x + 1];
                        }       //remove from array
                        numberSectors -= 1;                                 //1 less sector
                        grid.selS = 0;
                        grid.selW = 0;                         //deselect
                        //printf("after:%i,%i\n\n",numberSectors,numberWalls);
                    }
                }

                //load
                if (y > 448 && y < 480) {
                    dark = 17;
                    load();
                }
            }

            //clicked on grid
            else {
                //init new sector
                if (grid.addSect == 1) {
                    SECTORS[numberSectors].ws = numberWalls;                                   //clear wall start
                    SECTORS[numberSectors].we = numberWalls + 1;                                 //add 1 to wall end
                    SECTORS[numberSectors].z1 = grid.z1;
                    SECTORS[numberSectors].z2 = grid.z2;
                    SECTORS[numberSectors].st = grid.st;
                    SECTORS[numberSectors].ss = grid.ss;
                    WALLS[numberWalls].x1 = grid.mx * grid.scale;
                    WALLS[numberWalls].y1 = grid.my * grid.scale;  //x1,y1 
                    WALLS[numberWalls].x2 = grid.mx * grid.scale;
                    WALLS[numberWalls].y2 = grid.my * grid.scale;  //x2,y2
                    WALLS[numberWalls].wt = grid.wt;
                    WALLS[numberWalls].u = grid.wu;
                    WALLS[numberWalls].v = grid.wv;
                    numberWalls += 1;                                              //add 1 wall
                    numberSectors += 1;                                              //add this sector
                    grid.addSect = 3;                                             //go to point 2
                }

                //add point 2
                else if (grid.addSect == 3) {
                    if (SECTORS[numberSectors - 1].ws == numberWalls - 1 && grid.mx * grid.scale <= WALLS[SECTORS[numberSectors - 1].ws].x1) {
                        numberWalls -= 1;
                        numberSectors -= 1;
                        grid.addSect = 0;
                        System.out.println("walls must be counter clockwise\n");
                        return false;
                    }

                    //point 2
                    WALLS[numberWalls - 1].x2 = grid.mx * grid.scale;
                    WALLS[numberWalls - 1].y2 = grid.my * grid.scale; //x2,y2
                    //automatic shading 
                    float ang = atan2(WALLS[numberWalls - 1].y2 - WALLS[numberWalls - 1].y1, WALLS[numberWalls - 1].x2 - WALLS[numberWalls - 1].x1);
                    ang = (float) ((ang * 180) / Math.PI);      //radians to degrees
                    if (ang < 0) {
                        ang += 360;
                    }    //correct negative
                    int shade = (int) ang;           //shading goes from 0-90-0-90-0
                    if (shade > 180) {
                        shade = 180 - (shade - 180);
                    }
                    if (shade > 90) {
                        shade = 90 - (shade - 90);
                    }
                    WALLS[numberWalls - 1].shade = shade;

                    //check if sector is closed
                    if (WALLS[numberWalls - 1].x2 == WALLS[SECTORS[numberSectors - 1].ws].x1 && WALLS[numberWalls - 1].y2 == WALLS[SECTORS[numberSectors - 1].ws].y1) {
                        WALLS[numberWalls - 1].wt = grid.wt;
                        WALLS[numberWalls - 1].u = grid.wu;
                        WALLS[numberWalls - 1].v = grid.wv;
                        grid.addSect = 0;
                    }
                    //not closed, add new wall
                    else {
                        //init next wall
                        SECTORS[numberSectors - 1].we += 1;                                      //add 1 to wall end
                        WALLS[numberWalls].x1 = grid.mx * grid.scale;
                        WALLS[numberWalls].y1 = grid.my * grid.scale;  //x1,y1 
                        WALLS[numberWalls].x2 = grid.mx * grid.scale;
                        WALLS[numberWalls].y2 = grid.my * grid.scale;  //x2,y2
                        WALLS[numberWalls - 1].wt = grid.wt;
                        WALLS[numberWalls - 1].u = grid.wu;
                        WALLS[numberWalls - 1].v = grid.wv;
                        WALLS[numberWalls].shade = 0;
                        numberWalls += 1;                                              //add 1 wall
                    }
                }
            }
        }

        //clear variables to move point
        for (w = 0; w < 4; w++) {
            grid.move[w] = -1;
        }

        if (grid.addSect == 0 && event.getMouseButton() == MouseButtonCodes.BUTTON_RIGHT) {
            //move point hold id 
            for (s = 0; s < numberSectors; s++) {
                for (w = SECTORS[s].ws; w < SECTORS[s].we; w++) {
                    int x1 = WALLS[w].x1, y1 = WALLS[w].y1;
                    int x2 = WALLS[w].x2, y2 = WALLS[w].y2;
                    int mx = grid.mx * grid.scale, my = grid.my * grid.scale;
                    if (mx < x1 + 3 && mx > x1 - 3 && my < y1 + 3 && my > y1 - 3) {
                        grid.move[0] = w;
                        grid.move[1] = 1;
                    }
                    if (mx < x2 + 3 && mx > x2 - 3 && my < y2 + 3 && my > y2 - 3) {
                        grid.move[2] = w;
                        grid.move[3] = 2;
                    }
                }
            }
        }

        return false;
    }

    private boolean onMouseButtonReleased(MouseButtonReleasedEvent event) {
        if (event.getMouseButton() == MouseButtonCodes.BUTTON_LEFT) {
            dark = 0;
        }

        return false;
    }

    private boolean onMouseMoved(MouseMovedEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        System.out.println(x + " " + y);

        if (x < 580 && grid.addSect == 0 && grid.move[0] > -1) {
            int Aw = grid.move[0];
            int Ax = grid.move[1];
            int Bw = grid.move[2];
            int Bx = grid.move[3];

            if (Ax == 1) {
                WALLS[Aw].x1 = ((x + 16) >> 5) << 5;
                WALLS[Aw].y1 = ((Application.GL_SCREEN_HEIGHT - y + 16) >> 5) << 5;
            }

            if (Ax == 2) {
                WALLS[Aw].x2 = ((x + 16) >> 5) << 5;
                WALLS[Aw].y2 = ((Application.GL_SCREEN_HEIGHT - y + 16) >> 5) << 5;
            }

            if (Bx == 1) {
                WALLS[Bw].x1 = ((x + 16) >> 5) << 5;
                WALLS[Bw].y1 = ((Application.GL_SCREEN_HEIGHT - y + 16) >> 5) << 5;
            }

            if (Bx == 2) {
                WALLS[Bw].x2 = ((x + 16) >> 5) << 5;
                WALLS[Bw].y2 = ((Application.GL_SCREEN_HEIGHT - y + 16) >> 5) << 5;
            }
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
