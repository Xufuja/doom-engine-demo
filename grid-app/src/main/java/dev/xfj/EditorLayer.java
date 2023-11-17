package dev.xfj;

import dev.xfj.application.Application;
import dev.xfj.events.Event;
import dev.xfj.events.EventDispatcher;
import dev.xfj.events.key.KeyPressedEvent;
import dev.xfj.events.mouse.MouseButtonPressedEvent;
import dev.xfj.events.mouse.MouseButtonReleasedEvent;
import dev.xfj.input.Input;
import dev.xfj.input.KeyCodes;
import dev.xfj.input.MouseButtonCodes;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Operation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import static dev.xfj.application.Application.PIXEL_SCALE;
import static dev.xfj.application.Application.SCREEN_HEIGHT;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.lwjgl.opengl.GL41.*;

public class EditorLayer implements Layer {
    private static final float[] COS = new float[360];
    private static final float[] SIN = new float[360];
    private static final Wall[] walls = new Wall[256];
    private static final Sector[] sectors = new Sector[128];
    private static final Texture[] textures = new Texture[64];
    private static final int[] T_NUMBERS = loadArray("textures\\tNumbers.txt");
    private static final int[] T_VIEW2D = loadArray("textures\\tView2D.txt");
    private static int NUMBER_TEXTURES = 19;
    private static int NUMBER_SECTORS = 0;
    private static int NUMBER_WALLS = 0;

    private Player player;
    private Grid grid;
    private int dark;

    static {
        for (int x = 0; x < 360; x++) {
            COS[x] = (float) cos(Math.toRadians(x));
            SIN[x] = (float) sin(Math.toRadians(x));
        }

        IntStream.range(0, walls.length).forEach(i -> walls[i] = new Wall());
        IntStream.range(0, sectors.length).forEach(i -> sectors[i] = new Sector());
        IntStream.range(0, textures.length).forEach(i -> textures[i] = new Texture());
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

        textures[0].name = loadArray("textures\\t00.txt");
        textures[0].h = 16;
        textures[0].w = 16;
        textures[1].name = loadArray("textures\\t01.txt");
        textures[1].h = 16;
        textures[1].w = 16;
        textures[2].name = loadArray("textures\\t02.txt");
        textures[2].h = 16;
        textures[2].w = 16;
        textures[3].name = loadArray("textures\\t03.txt");
        textures[3].h = 16;
        textures[3].w = 16;
        textures[4].name = loadArray("textures\\t04.txt");
        textures[4].h = 16;
        textures[4].w = 16;
        textures[5].name = loadArray("textures\\t05.txt");
        textures[5].h = 16;
        textures[5].w = 16;
        textures[6].name = loadArray("textures\\t06.txt");
        textures[6].h = 16;
        textures[6].w = 16;
        textures[7].name = loadArray("textures\\t07.txt");
        textures[7].h = 16;
        textures[7].w = 16;
        textures[8].name = loadArray("textures\\t08.txt");
        textures[8].h = 32;
        textures[8].w = 32;
        textures[9].name = loadArray("textures\\t09.txt");
        textures[9].h = 64;
        textures[9].w = 64;
        textures[10].name = loadArray("textures\\t10.txt");
        textures[10].h = 16;
        textures[10].w = 16;
        textures[11].name = loadArray("textures\\t11.txt");
        textures[11].h = 16;
        textures[11].w = 16;
        textures[12].name = loadArray("textures\\t12.txt");
        textures[12].h = 16;
        textures[12].w = 16;
        textures[13].name = loadArray("textures\\t13.txt");
        textures[13].h = 16;
        textures[13].w = 16;
        textures[14].name = loadArray("textures\\t14.txt");
        textures[14].h = 16;
        textures[14].w = 16;
        textures[15].name = loadArray("textures\\t15.txt");
        textures[15].h = 16;
        textures[15].w = 16;
        textures[16].name = loadArray("textures\\t16.txt");
        textures[16].h = 16;
        textures[16].w = 16;
        textures[17].name = loadArray("textures\\t17.txt");
        textures[17].h = 16;
        textures[17].w = 16;
        textures[18].name = loadArray("textures\\t18.txt");
        textures[18].h = 16;
        textures[18].w = 16;
        textures[19].name = loadArray("textures\\t19.txt");
        textures[19].h = 16;
        textures[19].w = 16;

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
        eventDispatcher.dispatch(KeyPressedEvent.class, this::onKeyPressed);
        eventDispatcher.dispatch(MouseButtonPressedEvent.class, this::onMouseButtonPressed);
        eventDispatcher.dispatch(MouseButtonReleasedEvent.class, this::onMouseButtonReleased);

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
        for (s = 0; s < NUMBER_SECTORS; s++) {
            for (w = sectors[s].ws; w < sectors[s].we; w++) {
                if (s == grid.selS - 1) //if this sector is selected
                {
                    //set sector to globals
                    sectors[grid.selS - 1].z1 = grid.z1;
                    sectors[grid.selS - 1].z2 = grid.z2;
                    sectors[grid.selS - 1].st = grid.st;
                    sectors[grid.selS - 1].ss = grid.ss;
                    //yellow select
                    if (grid.selW == 0) {
                        c = 80;
                    } //all walls yellow
                    else if (grid.selW + sectors[s].ws - 1 == w) {
                        c = 80;
                        walls[w].wt = grid.wt;
                        walls[w].u = grid.wu;
                        walls[w].v = grid.wv;
                    } //one wall selected
                    else {
                        c = 0;
                    } //grey walls
                } else {
                    c = 0;
                } //sector not selected, grey

                drawLine(walls[w].x1 / grid.scale, walls[w].y1 / grid.scale, walls[w].x2 / grid.scale, walls[w].y2 / grid.scale, 128 + c, 128 + c, 128 - c);
                drawPixel(walls[w].x1 / grid.scale, walls[w].y1 / grid.scale, 255, 255, 255);
                drawPixel(walls[w].x2 / grid.scale, walls[w].y2 / grid.scale, 255, 255, 255);
            }
        }

        //draw player
        int dx = (int) (SIN[player.angle] * 12);
        int dy = (int) (COS[player.angle] * 12);

        drawPixel(player.x / grid.scale, player.y / grid.scale, 0, 255, 0);
        drawPixel((player.x + dx) / grid.scale, (player.y + dy) / grid.scale, 0, 175, 0);

        //draw wall texture
        float tx = 0, tx_stp = (float) (textures[grid.wt].w / 15.0);
        float ty = 0, ty_stp = (float) (textures[grid.wt].h / 15.0);

        for (y = 0; y < 15; y++) {
            tx = 0;
            for (x = 0; x < 15; x++) {
                int x2 = (int) tx % textures[grid.wt].w;
                tx += tx_stp;//*grid.wu;
                int y2 = (int) ty % textures[grid.wt].h;
                int r = textures[grid.wt].name[(textures[grid.wt].h - y2 - 1) * 3 * textures[grid.wt].w + x2 * 3 + 0];
                int g = textures[grid.wt].name[(textures[grid.wt].h - y2 - 1) * 3 * textures[grid.wt].w + x2 * 3 + 1];
                int b = textures[grid.wt].name[(textures[grid.wt].h - y2 - 1) * 3 * textures[grid.wt].w + x2 * 3 + 2];
                drawPixel(x + 145, y + 105 - 8, r, g, b);
            }
            ty += ty_stp;//*grid.wv;
        }
        //draw surface texture
        tx = 0;
        tx_stp = (float) (textures[grid.st].w / 15.0);
        ty = 0;
        ty_stp = (float) (textures[grid.st].h / 15.0);

        for (y = 0; y < 15; y++) {
            tx = 0;
            for (x = 0; x < 15; x++) {
                int x2 = (int) tx % textures[grid.st].w;
                tx += tx_stp;//*grid.ss;
                int y2 = (int) ty % textures[grid.st].h;
                int r = textures[grid.st].name[(textures[grid.st].h - y2 - 1) * 3 * textures[grid.st].w + x2 * 3 + 0];
                int g = textures[grid.st].name[(textures[grid.st].h - y2 - 1) * 3 * textures[grid.st].w + x2 * 3 + 1];
                int b = textures[grid.st].name[(textures[grid.st].h - y2 - 1) * 3 * textures[grid.st].w + x2 * 3 + 2];
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
        //todo
    }

    private void load() {
        try {
            List<String> lines = Files.readAllLines(Path.of("level.h"));
            NUMBER_SECTORS = Integer.parseInt(lines.get(0));

            for (int i = 0; i < NUMBER_SECTORS; i++) {
                String[] line = lines.get(i + 1).split(" ");
                sectors[i].ws = Integer.parseInt(line[0]);
                sectors[i].we = Integer.parseInt(line[1]);
                sectors[i].z1 = Integer.parseInt(line[2]);
                sectors[i].z2 = Integer.parseInt(line[3]);
                sectors[i].st = Integer.parseInt(line[4]);
                sectors[i].ss = Integer.parseInt(line[5]);
            }

            NUMBER_WALLS = Integer.parseInt(lines.get(NUMBER_SECTORS + 1));

            for (int i = 0; i < NUMBER_WALLS; i++) {
                String[] line = lines.get(i + 1).split(" ");
                walls[i].x1 = Integer.parseInt(line[0]);
                walls[i].y1 = Integer.parseInt(line[1]);
                walls[i].x2 = Integer.parseInt(line[2]);
                walls[i].y2 = Integer.parseInt(line[3]);
                walls[i].wt = Integer.parseInt(line[4]);
                walls[i].u = Integer.parseInt(line[5]);
                walls[i].v = Integer.parseInt(line[6]);
            }
            String[] playerData = lines.get(NUMBER_SECTORS + NUMBER_WALLS + 1).split(" ");
            player.x = Integer.parseInt(playerData[0]);
            player.y = Integer.parseInt(playerData[1]);
            player.z = Integer.parseInt(playerData[2]);
            player.angle = Integer.parseInt(playerData[3]);
            player.lookAngle = Integer.parseInt(playerData[4]);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private boolean onKeyPressed(KeyPressedEvent event) {
        if (event.isRepeat()) {
            return false;
        }


        switch (event.getKeyCode()) {

        }
        return false;
    }

    private boolean onMouseButtonPressed(MouseButtonPressedEvent event) {
        int x = (int) Input.getMousePosition().x;
        int y = (int) Input.getMousePosition().y;

        int s;
        int w;
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
                            grid.wt = NUMBER_TEXTURES;
                        }
                    } else {
                        grid.wt += 1;
                        if (grid.wt > NUMBER_TEXTURES) {
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
                            grid.st = NUMBER_TEXTURES;
                        }
                    } else {
                        grid.st += 1;
                        if (grid.st > NUMBER_TEXTURES) {
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
                            grid.selS = NUMBER_SECTORS;
                        }
                    } else {
                        dark = 13;
                        grid.selS += 1;
                        if (grid.selS > NUMBER_SECTORS) {
                            grid.selS = 0;
                        }
                    }
                    s = grid.selS - 1;
                    grid.z1 = sectors[s].z1; //sector bottom height
                    grid.z2 = sectors[s].z2; //sector top height
                    grid.st = sectors[s].st; //surface texture
                    grid.ss = sectors[s].ss; //surface scale
                    grid.wt = walls[sectors[s].ws].wt;
                    grid.wu = walls[sectors[s].ws].u;
                    grid.wv = walls[sectors[s].ws].v;
                    if (grid.selS == 0) {
                        initGlobals();
                    } //defaults
                }
                //select sector's walls
                int snw = sectors[grid.selS - 1].we - sectors[grid.selS - 1].ws; //sector's number of walls
                if (y > 386 && y < 416) {
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
                        grid.wt = walls[sectors[grid.selS - 1].ws + grid.selW - 1].wt; //printf("ws,%i,%i\n",grid.wt, 1 );
                        grid.wu = walls[sectors[grid.selS - 1].ws + grid.selW - 1].u;
                        grid.wv = walls[sectors[grid.selS - 1].ws + grid.selW - 1].v;
                    }
                }
                //delete
                if (y > 416 && y < 448) {
                    dark = 16;
                    if (grid.selS > 0) {
                        int d = grid.selS - 1;                             //delete this one
                        //printf("%i before:%i,%i\n",d, NUMBER_SECTORS,NUMBER_WALLS);
                        NUMBER_WALLS -= (sectors[d].we - sectors[d].ws);                 //first subtract number of walls
                        for (x = d; x < NUMBER_SECTORS; x++) {
                            sectors[x] = sectors[x + 1];
                        }       //remove from array
                        NUMBER_SECTORS -= 1;                                 //1 less sector
                        grid.selS = 0;
                        grid.selW = 0;                         //deselect
                        //printf("after:%i,%i\n\n",NUMBER_SECTORS,NUMBER_WALLS);
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
                    sectors[NUMBER_SECTORS].ws = NUMBER_WALLS;                                   //clear wall start
                    sectors[NUMBER_SECTORS].we = NUMBER_WALLS + 1;                                 //add 1 to wall end
                    sectors[NUMBER_SECTORS].z1 = grid.z1;
                    sectors[NUMBER_SECTORS].z2 = grid.z2;
                    sectors[NUMBER_SECTORS].st = grid.st;
                    sectors[NUMBER_SECTORS].ss = grid.ss;
                    walls[NUMBER_WALLS].x1 = grid.mx * grid.scale;
                    walls[NUMBER_WALLS].y1 = grid.my * grid.scale;  //x1,y1
                    walls[NUMBER_WALLS].x2 = grid.mx * grid.scale;
                    walls[NUMBER_WALLS].y2 = grid.my * grid.scale;  //x2,y2
                    walls[NUMBER_WALLS].wt = grid.wt;
                    walls[NUMBER_WALLS].u = grid.wu;
                    walls[NUMBER_WALLS].v = grid.wv;
                    NUMBER_WALLS += 1;                                              //add 1 wall
                    NUMBER_SECTORS += 1;                                              //add this sector
                    grid.addSect = 3;                                             //go to point 2
                }

                //add point 2
                else if (grid.addSect == 3) {
                    if (sectors[NUMBER_SECTORS - 1].ws == NUMBER_WALLS - 1 && grid.mx * grid.scale <= walls[sectors[NUMBER_SECTORS - 1].ws].x1) {
                        NUMBER_WALLS -= 1;
                        NUMBER_SECTORS -= 1;
                        grid.addSect = 0;
                        System.out.println("walls must be counter clockwise\n");
                        return false;
                    }

                    //point 2
                    walls[NUMBER_WALLS - 1].x2 = grid.mx * grid.scale;
                    walls[NUMBER_WALLS - 1].y2 = grid.my * grid.scale; //x2,y2
                    //automatic shading
                    float ang = (float) Math.atan2(walls[NUMBER_WALLS - 1].y2 - walls[NUMBER_WALLS - 1].y1, walls[NUMBER_WALLS - 1].x2 - walls[NUMBER_WALLS - 1].x1);
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
                    walls[NUMBER_WALLS - 1].shade = shade;

                    //check if sector is closed
                    if (walls[NUMBER_WALLS - 1].x2 == walls[sectors[NUMBER_SECTORS - 1].ws].x1 && walls[NUMBER_WALLS - 1].y2 == walls[sectors[NUMBER_SECTORS - 1].ws].y1) {
                        walls[NUMBER_WALLS - 1].wt = grid.wt;
                        walls[NUMBER_WALLS - 1].u = grid.wu;
                        walls[NUMBER_WALLS - 1].v = grid.wv;
                        grid.addSect = 0;
                    }
                    //not closed, add new wall
                    else {
                        //init next wall
                        sectors[NUMBER_SECTORS - 1].we += 1;                                      //add 1 to wall end
                        walls[NUMBER_WALLS].x1 = grid.mx * grid.scale;
                        walls[NUMBER_WALLS].y1 = grid.my * grid.scale;  //x1,y1
                        walls[NUMBER_WALLS].x2 = grid.mx * grid.scale;
                        walls[NUMBER_WALLS].y2 = grid.my * grid.scale;  //x2,y2
                        walls[NUMBER_WALLS - 1].wt = grid.wt;
                        walls[NUMBER_WALLS - 1].u = grid.wu;
                        walls[NUMBER_WALLS - 1].v = grid.wv;
                        walls[NUMBER_WALLS].shade = 0;
                        NUMBER_WALLS += 1;                                              //add 1 wall
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
            for (s = 0; s < NUMBER_SECTORS; s++) {
                for (w = sectors[s].ws; w < sectors[s].we; w++) {
                    int x1 = walls[w].x1, y1 = walls[w].y1;
                    int x2 = walls[w].x2, y2 = walls[w].y2;
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

    private static int[] loadArray(String path) {
        try {
            return Files.readAllLines(Path.of(path)).stream().mapToInt(Integer::parseInt).toArray();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
