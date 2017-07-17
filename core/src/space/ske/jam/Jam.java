package space.ske.jam;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.DistanceFieldFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;
import space.ske.jam.entity.*;

import java.util.Comparator;

public class Jam extends ApplicationAdapter {
    public static Jam i;

    public static boolean debug = false;

    private World world;

    private Array<Entity> entities = new SnapshotArray<Entity>(Entity.class);

    private OrthographicCamera camera;
    private OrthographicCamera screenCamera = new OrthographicCamera();
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private TileType[][] tiles;
    private float timer;
    private Array<BGRect> bgRects = new Array<BGRect>(BGRect.class);
    private float bgTimer;

    private String title;

    private int map = 0;

    private ShaderProgram dfShader;

    private float titleTimer = -1;
    private boolean isTitleDisappearing;

    private boolean isContinuing;
    private float continueTimer;
    private Player player;

    private float screenWipeTimer;

    private String interText = "";
    private SpringingContext2D cameraSpring = new SpringingContext2D(2f, 0.8f);

    private float screenshake;

    @Override
    public void create() {
        i = this;

//        setupWorld();

        camera = new OrthographicCamera();
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        dfShader = DistanceFieldFont.createDistanceFieldShader();

//        loadMap(map);
    }

    private void setupWorld() {
        world = new World(new Vector2(0, -30), true);
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Object fau = contact.getFixtureA().getBody().getUserData();
                if (fau instanceof Entity) {
                    if (contact.getFixtureB().getBody().getUserData() instanceof Entity) {
                        ((Entity) fau).collide((Entity) contact.getFixtureB().getBody().getUserData(), contact.getFixtureA());
                    }

                    if (contact.getFixtureB().getUserData() instanceof TileType) {
                        ((Entity) fau).collide((TileType) contact.getFixtureB().getUserData(), contact.getFixtureA());
                    }
                }

                Object fbu = contact.getFixtureB().getBody().getUserData();
                if (fbu instanceof Entity) {
                    if (contact.getFixtureA().getBody().getUserData() instanceof Entity) {
                        ((Entity) fbu).collide((Entity) contact.getFixtureA().getBody().getUserData(), contact.getFixtureB());
                    }

                    if (contact.getFixtureA().getUserData() instanceof TileType) {
                        ((Entity) fbu).collide((TileType) contact.getFixtureA().getUserData(), contact.getFixtureB());
                    }
                }
            }

            @Override
            public void endContact(Contact contact) {
                Object fau = contact.getFixtureA().getBody().getUserData();
                if (fau instanceof Entity) {
                    if (contact.getFixtureB().getBody().getUserData() instanceof Entity) {
                        ((Entity) fau).stopCollide((Entity) contact.getFixtureB().getBody().getUserData(), contact.getFixtureA());
                    }

                    if (contact.getFixtureB().getUserData() instanceof TileType) {
                        ((Entity) fau).stopCollide((TileType) contact.getFixtureB().getUserData(), contact.getFixtureA());
                    }
                }

                Object fbu = contact.getFixtureB().getBody().getUserData();
                if (fbu instanceof Entity) {
                    if (contact.getFixtureA().getBody().getUserData() instanceof Entity) {
                        ((Entity) fbu).stopCollide((Entity) contact.getFixtureA().getBody().getUserData(), contact.getFixtureB());
                    }

                    if (contact.getFixtureA().getUserData() instanceof TileType) {
                        ((Entity) fbu).stopCollide((TileType) contact.getFixtureA().getUserData(), contact.getFixtureB());
                    }
                }
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });
    }

    public void loadMap(int id) {
        title = null;
        String path = "levels/" + id + ".tmx";

        TiledMap map = new TmxMapLoader().load(path);
        title = (String) map.getProperties().get("title");

        for (MapLayer mapLayer : map.getLayers()) {
            for (MapObject mapObject : mapLayer.getObjects()) {
                if (matchesType(mapObject, "spawn")) {
                    player = new Player();
                    add(mapObject, player);
                } else if (matchesType(mapObject, "ball")) {
                    Ball b = new Ball();
                    add(mapObject, b);
                } else if (matchesType(mapObject, "barrier")) {
                    PolylineMapObject pmo = (PolylineMapObject) mapObject;
                    float[] vs = pmo.getPolyline().getTransformedVertices();
                    for (int i = 0; i < (vs.length / 2) - 1; i++) {
                        Vector2 from = new Vector2(vs[i * 2], vs[i * 2 + 1]).scl(1 / 32f);
                        Vector2 to = new Vector2(vs[i * 2 + 2], vs[i * 2 + 3]).scl(1 / 32f);

                        Barrier b = new Barrier(from, to);
                        entities.add(b);
                    }
                } else if (matchesType(mapObject, "thanks")) {
                    entities.add(new Thanks());
                }
            }

            if (mapLayer instanceof TiledMapTileLayer) {
                TiledMapTileLayer tmtl = (TiledMapTileLayer) mapLayer;

                tiles = new TileType[tmtl.getWidth()][tmtl.getHeight()];
                for (int x = 0; x < tmtl.getWidth(); x++) {
                    for (int y = 0; y < tmtl.getHeight(); y++) {
                        TiledMapTileLayer.Cell c = tmtl.getCell(x, y);
                        if (c != null) {
                            if (matchesType(c, "tile")) {
                                tiles[x][y] = TileType.TILE;
                            } else if (matchesType(c, "goal")) {
                                tiles[x][y] = TileType.GOAL;
                            } else if (matchesType(c, "damage")) {
                                tiles[x][y] = TileType.DAMAGE;
                            } else if (matchesType(c, "brick")) {
                                tiles[x][y] = TileType.BRICK;
                            } else {
                                tiles[x][y] = TileType.EMPTY;
                            }
                        } else {
                            tiles[x][y] = TileType.EMPTY;
                        }
                    }
                }

                Body body = world.createBody(new BodyDef());
                for (int x = 0; x < tmtl.getWidth(); x++) {
                    for (int y = 0; y < tmtl.getHeight(); y++) {
                        if (tiles[x][y] != TileType.EMPTY) {
                            TileType type = tiles[x][y];

                            if (x == 0 || tiles[x - 1][y] == TileType.EMPTY) {
                                EdgeShape es = new EdgeShape();
                                es.set(x, y, x, y + 1);
                                body.createFixture(es, 0).setUserData(type);
                            }

                            if (x == tmtl.getWidth() - 1 || tiles[x + 1][y] == TileType.EMPTY) {
                                EdgeShape es = new EdgeShape();
                                es.set(x + 1, y, x + 1, y + 1);
                                body.createFixture(es, 0).setUserData(type);
                            }

                            if (y == 0 || tiles[x][y - 1] == TileType.EMPTY) {
                                EdgeShape es = new EdgeShape();
                                es.set(x, y, x + 1, y);
                                body.createFixture(es, 0).setUserData(type);
                            }

                            if (y == tmtl.getHeight() - 1 || tiles[x][y + 1] == TileType.EMPTY) {
                                EdgeShape es = new EdgeShape();
                                es.set(x, y + 1, x + 1, y + 1);
                                body.createFixture(es, 0).setUserData(type);
                            }
                        }
                    }
                }
            }
        }

        updateCameraToPosition();
    }

    private void updateCameraToPosition() {
        Vector2 pp = player.getBody().getPosition();

        int buffer = 4;

        if (camera.viewportWidth >= tiles.length) {
            cameraSpring.getTarget().x = tiles.length / 2f;
        } else {
            cameraSpring.getTarget().x = MathUtils.clamp(pp.x, camera.viewportWidth / 2 - buffer, tiles.length - camera.viewportWidth / 2 + buffer);
        }

        if (camera.viewportHeight >= tiles[0].length) {
            cameraSpring.getTarget().y = tiles[0].length / 2f;

        } else {
            cameraSpring.getTarget().y = MathUtils.clamp(pp.y, camera.viewportHeight / 2 - buffer, tiles[0].length - camera.viewportHeight / 2 + buffer);
        }
        cameraSpring.update(Gdx.graphics.getDeltaTime());
        camera.position.set(cameraSpring.getPosition(), 0);

        if (screenshake > 0) {
            screenshake -= Gdx.graphics.getDeltaTime();
            camera.position.add(MathUtils.random(-screenshake, screenshake), MathUtils.random(-screenshake, screenshake), 0);
        }

        camera.update();
    }

    private boolean matchesType(MapObject mapObject, String type) {
        return type.equalsIgnoreCase((String) mapObject.getProperties().get("type"));
    }


    private boolean matchesType(TiledMapTileLayer.Cell c, String type) {
        return type.equalsIgnoreCase((String) c.getTile().getProperties().get("type"));
    }

    private void add(MapObject mapObject, Entity entity) {
        Vector2 center = new Vector2();
        ((RectangleMapObject) mapObject).getRectangle().getCenter(center);
        center.scl(1 / 32f);
        entity.getBody().setTransform(center, 0);
        entities.add(entity);
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            beginRestart();
        }

        if (debug) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
                nextLevel();
            }
        }
        timer += Gdx.graphics.getDeltaTime();

        if (map > 0 && world != null) {
            world.step(Gdx.graphics.getDeltaTime(), 5, 5);

            Entity[] entities = ((SnapshotArray<Entity>) this.entities).begin();
            for (int index = 0; index < this.entities.size; index++) {
                Entity entity = entities[index];
                entity.update(Gdx.graphics.getDeltaTime());
            }
            ((SnapshotArray<Entity>) this.entities).end();

            updateCameraToPosition();
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(screenCamera.combined);
        batch.setShader(dfShader);
        batch.begin();
        Assets.italicFont.getData().setScale(1.5f);
        Assets.italicFont.draw(batch, interText, Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.6f, Gdx.graphics.getWidth() * 0.8f, Align.center, true);
        Assets.italicFont.getData().setScale(1);
        batch.end();

        boolean scissor;
        if (screenWipeTimer <= 0) {
            float i = 1 + screenWipeTimer * 2;
            i = Interpolation.exp5.apply(i);
            scissor = ScissorStack.pushScissors(new Rectangle(0, 0, i * Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        } else {
            float st = this.screenWipeTimer - 1;
            float i = st * 2;
            i = Interpolation.exp5.apply(i);
            scissor = ScissorStack.pushScissors(new Rectangle(Math.max(0, i * Gdx.graphics.getWidth()), 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        }

        if (scissor) {
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            drawBackground(shapeRenderer);

            if (map == 0 || world == null) {

                float yMul = Interpolation.exp5.apply(Math.min(timer, 1));
                float scale = MathUtils.sin(timer * 3) * 0.3f + 2.5f;
                scale *= 0.5f + yMul * 0.5f;
                float y = Gdx.graphics.getHeight() * 0.7f + scale * Assets.normalFont.getData().xHeight / 2;
                y = MathUtils.lerp(Gdx.graphics.getHeight(), y, yMul);

                shapeRenderer.setProjectionMatrix(screenCamera.combined);
                shapeRenderer.setColor(Color.CHARTREUSE);
                shapeRenderer.getColor().a = 0.5f;
                float an = MathUtils.sin(timer) * 10;
//            shapeRenderer.rotate(0, 0, 1, an);
                float ww = 550;
                int hh = 150;
                shapeRenderer.rect(Gdx.graphics.getWidth() / 2 - ww / 2, y - Assets.normalFont.getData().xHeight * scale * 2.75f, ww / 2, hh / 2, ww, hh, 1, 1, an);

                shapeRenderer.end();
                batch.begin();
                Assets.normalFont.setUseIntegerPositions(false);
                Assets.normalFont.getData().setScale(scale);

                Assets.normalFont.draw(batch, "Synergy", 0, y, Gdx.graphics.getWidth(), Align.center, false);
                Assets.normalFont.getData().setScale(1);

                Assets.normalFont.draw(batch, "Click anywhere to start", 0, Gdx.graphics.getHeight() * 0.25f * yMul, Gdx.graphics.getWidth(), Align.center, false);
                batch.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

                if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && map == 0) {
                    nextLevel();
                    continueTimer = 1;
                    screenWipeTimer = 1f;
                }
            } else {
                drawTiles(shapeRenderer);

                Entity[] entities = ((SnapshotArray<Entity>) this.entities).begin();
                for (int index = 0; index < this.entities.size; index++) {
                    Entity entity = entities[index];
                    entity.render(shapeRenderer);
                }
                ((SnapshotArray<Entity>) this.entities).end();
            }
            shapeRenderer.end();

        }
        if (scissor) ScissorStack.popScissors();

        if (map > 0) {
            drawTitle();
        }
//        new Box2DDebugRenderer().render(world, camera.combined);

        if (isContinuing) {
            continueTimer -= Gdx.graphics.getDeltaTime();

            if (continueTimer < 0) {
                endRestart();
            }
        }

        if (screenWipeTimer <= 0 || isContinuing) {
            if (screenWipeTimer > 1 && screenWipeTimer - Gdx.graphics.getDeltaTime() <= 1) {
                Assets.whoosh.play();
            }
            screenWipeTimer += Gdx.graphics.getDeltaTime();
        } else {
            screenWipeTimer = 0;
        }
    }

    private void drawTitle() {
        if (title == null) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        titleTimer += Gdx.graphics.getDeltaTime();

        float titleTimer = this.titleTimer;

        float i = Interpolation.exp5.apply(Math.min(titleTimer * 2 - 0.75f * (isTitleDisappearing ? 1 : 0), 1));
        float i2 = Interpolation.exp5.apply(Math.min(titleTimer * 2 - 0.75f * (isTitleDisappearing ? 0 : 1), 1));
        if (isTitleDisappearing) i2++;

        shapeRenderer.setProjectionMatrix(screenCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.CHARTREUSE);
        shapeRenderer.getColor().a = 0.4f;
        float cutoffX = Math.max(Gdx.graphics.getWidth() * i, 0);
        if (!isTitleDisappearing) {
            shapeRenderer.rect(0, 0, cutoffX, 48);
        } else {
            shapeRenderer.rect(cutoffX, 0, Gdx.graphics.getWidth(), 48);
        }
        shapeRenderer.end();

        if (isTitleDisappearing) ScissorStack.pushScissors(new Rectangle(0, 0, Gdx.graphics.getWidth(), 48));
        batch.setShader(dfShader);
        batch.setProjectionMatrix(screenCamera.combined);
        batch.begin();

        DistanceFieldFont fnt = Assets.italicFont;

        GlyphLayout layout = new GlyphLayout(fnt, title);

        float finalY = 48 / 2 + layout.height / 2;
        float y = MathUtils.lerp(-8, finalY, i2);
        fnt.draw(batch, layout, Gdx.graphics.getWidth() / 2f - layout.width / 2f, y);
        batch.end();
        if (isTitleDisappearing) ScissorStack.popScissors();
    }

    private void drawBackground(ShapeRenderer sr) {
        simulateBG(Gdx.graphics.getDeltaTime());

        bgRects.sort(new Comparator<BGRect>() {
            @Override
            public int compare(BGRect bgRect, BGRect t1) {
                return bgRect.layer < t1.layer ? -1 : 1;
            }
        });
        for (BGRect br : bgRects) {
            Rectangle r = br.rect;
            sr.setColor(br.layer * 0.05f, br.layer * 0.05f, br.layer * 0.05f, 1);
            sr.rect(r.x, r.y, r.width, r.height);
        }
    }

    private void presimBG() {
        for (int i = 0; i < 1000; i++) {
            simulateBG(0.1f);
        }
    }

    private void simulateBG(float deltaTime) {
        bgTimer += deltaTime;
        while (bgTimer > 0.05f) {
            bgTimer -= 0.05f;

            Rectangle r = new Rectangle(0, 0, MathUtils.random(4, 10), MathUtils.random(2, 6));
            r.setCenter(camera.position.x - camera.viewportWidth / 2 - r.width / 2, camera.position.y - MathUtils.random(-camera.viewportHeight / 2 - r.height / 2, camera.viewportHeight / 2 + r.height / 2));
            bgRects.add(new BGRect(r, MathUtils.random(0.5f, 3f)));
        }

        for (BGRect bgRect : bgRects.toArray()) {
            bgRect.rect.x += deltaTime * 10 * (bgRect.layer * bgRect.layer);

            if (bgRect.rect.x > camera.position.x + camera.viewportWidth / 2 + bgRect.rect.width / 2) {
                bgRects.removeValue(bgRect, true);
            }
        }
    }

    private void drawTiles(ShapeRenderer shapeRenderer) {
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                TileType tile = tiles[x][y];
                if (tile == TileType.EMPTY) {
                    shapeRenderer.setColor(0.257f, 0.271f, 0.269f, 1f);
                    shapeRenderer.rect(x, y, 1, 1);
                }
            }
        }

        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                TileType tile = tiles[x][y];
                if (tile == TileType.TILE || tile == TileType.BRICK || tile == TileType.GOAL || tile == TileType.DAMAGE) {
                    shapeRenderer.setColor(0, 0, 0, 0.3f);
                    shapeRenderer.rect(x + 0.2f, y - 0.2f, 1, 1);
                }
            }
        }

        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                float pulse = MathUtils.sin(timer * 10) * 0.1f + 0.1f;
                TileType tile = tiles[x][y];
                if (tile == TileType.DAMAGE) {
                    shapeRenderer.setColor(0.906f, 0.298f, 0.235f, 1f);
                    shapeRenderer.rect(x - pulse / 2, y - pulse / 2, 1 + pulse, 1 + pulse);
                } else if (tile == TileType.GOAL) {
                    shapeRenderer.setColor(Color.CHARTREUSE);
                    shapeRenderer.rect(x - pulse / 2, y - pulse / 2, 1 + pulse, 1 + pulse);
                }
            }
        }

        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                TileType tile = tiles[x][y];
                if (tile == TileType.TILE) {
                    shapeRenderer.setColor(0.925f, 0.941f, 0.945f, 1f);
//                    shapeRenderer.setColor(0.227f, 0.286f, 0.275f, 1f);
                    shapeRenderer.rect(x, y, 1, 1);
                } else if (tile == TileType.BRICK) {
                    shapeRenderer.setColor(1, 1, 1, 0.5f);
                    shapeRenderer.rect(x, y, 1, 1);
                }
            }
        }
    }

    @Override
    public void dispose() {
    }

    public Array<Entity> getEntities() {
        return entities;
    }

    public World getWorld() {
        return world;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        camera.setToOrtho(false, width / 16f, height / 16f);
        screenCamera.setToOrtho(false, width, height);
        if (map > 0) updateCameraToPosition();

        bgRects.clear();
        presimBG();
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public void endRestart() {
        restartButNoEffectsPls();
        titleTimer = -0.5f;

        screenWipeTimer = -0.75f;
    }

    public void nextLevel() {
        map++;
        beginRestart();
    }

    public void beginRestart() {
        if (isContinuing) return;

        isTitleDisappearing = true;
        titleTimer = 0;

        isContinuing = true;
        continueTimer = 2.25f;

        interText = Array.with(
                "Tip: Fling yourself up using the ball",
                "Tip: Stick to the wall by holding the beam",
                "Tip: Glass is unbeamable",
                "Tip: Aim within the ball's outer circle to snap the beam",
                "Tip: Fling the ball into the wall to reach new areas",
                "Tip: Beaming the ball will stop it in its tracks!",
                "Tip: Line up a shot by beaming on the wall and quickly pulling the ball towards you"
        ).random();

        if (MathUtils.random(3) == 1 && map > 3) {
            interText = Array.with(
                    "Tip: Mark smells",
                    "Tip: Rate 5 stars",
                    "Tip: fedora",
                    "Tip: Has anyone really been far even as decided to use even go want to do look more like?"
            ).random();
        }
    }

    public TileType[][] getTiles() {
        return tiles;
    }

    public void restartButNoEffectsPls() {
        if (world != null) world.dispose();
        entities.clear();

        setupWorld();
        loadMap(map);

        isContinuing = false;
        isTitleDisappearing = false;
        continueTimer = 0;
    }

    public void setScreenshake(float screenshake) {
        this.screenshake = screenshake;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public OrthographicCamera getScreenCamera() {
        return screenCamera;
    }

    public ShaderProgram getDFShader() {
        return dfShader;
    }

    public static class BGRect {
        private Rectangle rect;
        private float layer;

        public BGRect(Rectangle rect, float layer) {
            this.rect = rect;
            this.layer = layer;
        }
    }
}
